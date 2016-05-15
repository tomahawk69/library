package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.DataStatus;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.core.exceptions.LibrarySettingsException;
import org.library.core.utils.DateUtils;
import org.library.entities.FileInfo;
import org.library.entities.FileType;
import org.library.entities.Library;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

@Component
@Scope("singleton")
class LibraryManager {
    private static final String SETTINGS_FILE_NAME = ".registered_libraries";
    public static final String KEY_COUNT = "count";
    public static final String KEY_LAST_LIBRARY = "last";
    public static final String KEY_LIBRARY_PATH = "library";
    private static final String KEY_LIBRARY_COUNT = "library_count";
    private static final String KEY_LIBRARY_UPDATE_DATE = "library_update_date";

    public static final List<DataStatus> REFRESH_START_ALLOWED_STATUSES = Arrays.asList(DataStatus.IDLE);
    public static final List<DataStatus> REFRESH_END_ALLOWED_STATUSES = Arrays.asList(DataStatus.REFRESH);
    private static final List<DataStatus> REFRESH_CANCEL_ALLOWED_STATUSES = Arrays.asList(DataStatus.REFRESH);

    private final Logger LOGGER = LogManager.getLogger(LibraryManager.class);
    private final Map<Path, Library> libraries = new HashMap<>();
    private final SemaphoreService semaphoreService;
    private final String dataStorageType;
    private final String dataServiceType;

    private Library currentLibrary;
    private Lock currentLibraryLock = new ReentrantLock();
    private ExecutorService executor;

    private final FileService fileService;
    private final DataServiceFactory dataServiceFactory;

    @Autowired
    public LibraryManager(SemaphoreService semaphoreService, FileService fileService,
                          DataServiceFactory dataServiceFactory,
                          @Value("${library.data.service.type}") String dataServiceType,
                          @Value("${library.data.storage.type}") String dataStorageType) {
        this.semaphoreService = semaphoreService;
        this.fileService = fileService;
        this.dataServiceFactory = dataServiceFactory;
        this.executor = Executors.newFixedThreadPool(semaphoreService.getMaxAccessThreadsCount());
        this.dataServiceType = dataServiceType;
        this.dataStorageType = dataStorageType;
    }

    /**
     * Load libraries list from local file
     *
     * @throws IOException
     */
    @PostConstruct
    synchronized void loadSettings() throws IOException {
        LOGGER.info("Init settings begin");
        Path path = constructSettingsFilePath();
        if (Files.exists(path)) {
            try (InputStream inputStream = new FileInputStream(path.toFile())) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String lastLibraryPath = properties.getProperty(KEY_LAST_LIBRARY);
                if (properties.containsKey(KEY_COUNT)) {
                    int librariesCount = Integer.parseInt(properties.getProperty(KEY_COUNT));
                    while (librariesCount-- > 0) {
                        String libraryPath = properties.getProperty(KEY_LIBRARY_PATH + librariesCount);
                        Library library = registerLibrary(libraryPath);
                        if (libraryPath.equalsIgnoreCase(lastLibraryPath)) {
                            setCurrentLibrary(library);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        LOGGER.info("Init settings done");
        LOGGER.info("Last library: " + currentLibrary);
        LOGGER.info("Loaded libraries: " + libraries.size());
    }

    public void saveSettings() throws IOException {
        LOGGER.info("Save settings begin");
        Path path = constructSettingsFilePath();
        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            Properties properties = new Properties();
            int i = 0;
            for (Library library : libraries.values()) {
                properties.setProperty(KEY_LIBRARY_PATH + String.valueOf(i++), library.getPath());
                properties.setProperty(KEY_LIBRARY_COUNT, String.valueOf(library.getItemsCount()));
                properties.setProperty(KEY_LIBRARY_UPDATE_DATE, DateUtils.localDateTimeToString(library.getLastUpdateDate()));
                if (library.equals(currentLibrary)) {
                    properties.setProperty(KEY_LAST_LIBRARY, currentLibrary.getPath());
                }
                i++;
            }
            properties.setProperty(KEY_COUNT, String.valueOf(libraries.size()));
            properties.store(outputStream, null);
        }
        LOGGER.info("Save settings done");
    }

    private Path constructSettingsFilePath() {
        return Paths.get(SETTINGS_FILE_NAME);
    }

    public Library registerLibrary(String pathString) throws IOException, InterruptedException {
        Path path = Paths.get(pathString);
        Library result;
        synchronized (libraries) {
            result = libraries.get(path);
            if (result == null) {
                Library library = createAndAddLibrary(pathString);
                if (library != null) {
                    executor.submit(() -> initializeLibrary(library));
                    result = library;
                }
            }
        }
        return result;
    }

    public void initializeLibrary(Library library) {
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                library.getDataService().prepareDatabase();
                populateLibraryMetadata(library);
                setDataStatus(library, DataStatus.IDLE, null);
                LOGGER.info("Library initialized " + library);
            } catch (InterruptedException | LibraryDatabaseException e) {
                LOGGER.error("Cannot set library state", e);
            } finally {
                semaphoreService.releaseGlobalAccess();
            }
        }
    }

    private void populateLibraryMetadata(Library library) {
        library.setItemsCount(library.getDataService().getFileInfoCount());
        library.setLastUpdateDate(library.getDataService().getLastUpdateDate());
    }

    public Collection<Library> getLibraries() {
        return Collections.unmodifiableCollection(libraries.values());
    }

    Library getCurrentLibrary() {
        return currentLibrary;
    }

    void setCurrentLibrary(Library currentLibrary) throws LibrarySettingsException {
        LOGGER.debug("setCurrentLibrary to " + currentLibrary);
        currentLibraryLock.lock();
        try {
            this.currentLibrary = currentLibrary;
        } finally {
            currentLibraryLock.unlock();
        }
    }

    void setCurrentLibrary(final String path) throws LibrarySettingsException {
        LOGGER.debug("setCurrentLibrary to " + path);
        Library library;
        synchronized (libraries) {
            library = libraries.get(Paths.get(path));
            if (library == null) {
                try {
                    library = createAndAddLibrary(path);
                    setCurrentLibrary(library);
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    Library createAndAddLibrary(String stringPath) throws InterruptedException {
        Library result = null;
        Path path = Paths.get(stringPath);
        DataService dataService = dataServiceFactory.createDataService(dataServiceType, dataStorageType, path);
        if (dataService != null) {
            result = new Library(dataService, stringPath);
            setDataStatus(result, DataStatus.INITIALIZING, null);
            initializeLibrary(result);
            libraries.put(path, result);
        }
        return result;
    }

    public Map<String, Object> getDataStatus(Library library) {
        Map<String, Object> result = new HashMap<>();
        if (library != null) {
            result.put("status", library.getDataStatus());
            result.put("count", library.getItemsCount());
            result.put("updated", library.getUpdatedCount());
            return result;
        } else {
            result.put("status", DataStatus.NO_LIBRARY_SELECTED);
        }
        return result;
    }

    /**
     * Refresh the data for given library
     *
     * @param library library to refresh
     */
    public void refreshData(Library library) {
        if (library == null) {
            return;
        }
        if (checkAndSetDataStatus(library, DataStatus.REFRESH, REFRESH_START_ALLOWED_STATUSES)) {
            executor.submit(() -> refreshDataEx(library));
        }

    }

    /**
     * First acquire global thread
     *
     * @param library library to refresh
     */
    private void refreshDataEx(Library library) {
        LOGGER.debug("refreshDataEx started");
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                Future<List<Path>> filesGetFuture = executor.submit(() -> getFiles(Paths.get(library.getPath())));

                Future<List<FileInfo>> dbGetFuture = executor.submit(() -> getFileInfoList(library));

                List<Path> files = filesGetFuture.get();
                LOGGER.debug("files get " + files.size());
                List<FileInfo> infos = dbGetFuture.get();
                LOGGER.debug("infos get " + files.size());

                processFiles(library, files, infos);

            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e);
            } finally {
                semaphoreService.releaseGlobalAccess();
                checkAndSetDataStatus(library, DataStatus.IDLE, REFRESH_END_ALLOWED_STATUSES);
                LOGGER.debug("refreshDataEx ended");
            }
        }
    }

    private List<FileInfo> getFileInfoList(Library library) {
        List<FileInfo> result = Collections.emptyList();
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                library.getDataService();
            } finally {
                semaphoreService.acquireGlobalAccess();
            }
        }
        return result;
    }

    private List<Path> getFiles(Path path) throws IOException {
        List<Path> result;
        if (semaphoreService.acquireFilesAccess()) {
            try {
                result = fileService.getFilesList(FileType.getExtensions(), true, path);
            } finally {
                semaphoreService.releaseFilesAccess();
            }
        } else {
            result = Collections.emptyList();
        }
        return result;

    }

    private void processFiles(Library library, List<Path> files, List<FileInfo> infos) {

    }


    /**
     * @param library             library to process
     * @param dataStatus          desired DataStatus
     * @param allowedDataStatuses array of allowed DataStatuses (null if all are allowed)
     * @return
     */
    private boolean checkAndSetDataStatus(Library library, DataStatus dataStatus, List<DataStatus> allowedDataStatuses) {
        StampedLock lock = library.getDataStatusLock();
        Long stamp = lock.tryOptimisticRead();
        boolean result = false;
        try {
            if (!lock.validate(stamp)) {
                stamp = lock.tryReadLock(1, TimeUnit.SECONDS);
            }
            if ((allowedDataStatuses == null || allowedDataStatuses.contains(currentLibrary.getDataStatus()))) {
                result = setDataStatus(currentLibrary, dataStatus, stamp);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Cannot acquire lock for dataLock");
        } finally {
            if (lock.validate(stamp)) {
                lock.unlock(stamp);
            }
        }
        return result;
    }

    private boolean setDataStatus(Library library, DataStatus dataStatus, Long stamp) throws InterruptedException {
        LOGGER.debug("Set data status to " + dataStatus);
        boolean result = false;
        StampedLock lock = library.getDataStatusLock();
        if (stamp != null) {
            stamp = lock.tryConvertToWriteLock(stamp);
        } else {
            stamp = lock.tryWriteLock(1, TimeUnit.SECONDS);
        }
        try {
            if (lock.validate(stamp)) {
                library.setDataStatus(dataStatus);
                result = true;
            } else {
                LOGGER.error("Couldn't acquire lock");
            }
        } finally {
            if (lock.validate(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
        return result;
    }

    public void stopRefreshData(Library library) {
        checkAndSetDataStatus(library, DataStatus.CANCELLING, REFRESH_CANCEL_ALLOWED_STATUSES);
    }
}