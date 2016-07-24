package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.DataStatus;
import org.library.common.entities.FileInfo;
import org.library.common.entities.FileType;
import org.library.common.services.FileService;
import org.library.common.services.SemaphoreService;
import org.library.common.utils.FileInfoHelper;
import org.library.core.exceptions.LibraryDatabaseException;
import org.library.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class LibraryService {
    private final Logger LOGGER = LogManager.getLogger(LibraryService.class);
    private final Map<String, Library> libraries = new HashMap<>();

    private static final String SETTINGS_FILE_NAME = ".registered_libraries";
    private static final String KEY_COUNT = "count";
    private static final String KEY_LIBRARY_PATH = "library";

    private static final List<DataStatus> REFRESH_START_ALLOWED_STATUSES = Arrays.asList(DataStatus.IDLE);
    private static final List<DataStatus> REFRESH_END_ALLOWED_STATUSES = Arrays.asList(DataStatus.REFRESH);
    private static final List<DataStatus> REFRESH_CANCEL_ALLOWED_STATUSES = Arrays.asList(DataStatus.REFRESH);

    private final FileService fileService;
    private final DataServiceFactory dataServiceFactory;
    private final SemaphoreService semaphoreService;

    private final ExecutorService executor;

    @Autowired
    public LibraryService(SemaphoreService semaphoreService,
                          FileService fileService,
                          DataServiceFactory dataServiceFactory) {
        this.semaphoreService = semaphoreService;
        this.fileService = fileService;
        this.dataServiceFactory = dataServiceFactory;
        this.executor = Executors.newFixedThreadPool(semaphoreService.getMaxAccessThreadsCount());
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
                if (properties.containsKey(KEY_COUNT)) {
                    int librariesCount = Integer.parseInt(properties.getProperty(KEY_COUNT));
                    while (librariesCount-- > 0) {
                        String libraryPath = properties.getProperty(KEY_LIBRARY_PATH + librariesCount);
                        registerLibrary(libraryPath);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        LOGGER.info("Init settings done");
        LOGGER.info("Loaded libraries: " + libraries.size());
    }

    public void saveSettings() throws IOException {
        LOGGER.info("Save settings begin");
        Path path = constructSettingsFilePath();
        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            Properties properties = new Properties();
            int i = 0;
            for (Library library : libraries.values()) {
                properties.setProperty(KEY_LIBRARY_PATH + String.valueOf(i++), library.getLibraryMetadata().getPath());
                i++;
            }
            properties.setProperty(KEY_COUNT, String.valueOf(libraries.size()));
            properties.store(outputStream, null);
        }
        LOGGER.info("Save settings done");
    }

    private Library registerLibrary(String pathString) throws IOException, InterruptedException {
        Library result = null;
        synchronized (libraries) {
            Optional<Library> optional = libraries.values().parallelStream().filter(t -> t.getPath().equals(pathString)).findFirst();
            if (optional.isPresent()) {
                result = optional.get();
            } else {
                Library library = createAndAddLibrary(pathString);
                if (library != null) {
                    executor.submit(() -> initializeLibrary(library));
                    result = library;
                }
            }
        }
        return result;
    }

    private void initializeLibrary(Library library) {
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                library.initLibrary();
                LOGGER.info("Library initialized " + library);
            } finally {
                semaphoreService.releaseGlobalAccess();
            }
        }
    }

    public Collection<LibraryWeb> getLibraries() {
        return libraries.values().parallelStream().map(LibraryHelper::libraryToLibraryWebEntity).collect(Collectors.toList());
    }

    private Path constructSettingsFilePath() {
        return Paths.get(SETTINGS_FILE_NAME);
    }

    private Library createAndAddLibrary(String stringPath) throws InterruptedException {
        Library result = null;
        Path path = Paths.get(stringPath);
        DataService dataService = dataServiceFactory.createDataService(path);
        if (dataService != null) {
            result = new Library(dataService, constructLibraryMetadata(path));
            libraries.put(result.getUUID(), result);
        }
        return result;
    }

    public Map<String, Object> getDataStatus(String uuid) {
        Map<String, Object> result = new HashMap<>();
        Library library = libraries.get(uuid);
        if (library != null) {
            result.put("status", library.getDataStatus());
            result.put("count", library.getItemsCount());
            result.put("last_update", library.getLastUpdateDate());
            result.put("last_refresh", library.getRefreshDate());
            result.put("refresh_count", library.getRefreshItemsCount());
            result.put("refresh_proceed", library.getRefreshProceedCount());
            result.put("refresh_updated", library.getRefreshUpdatedCount());
            return result;
        } else {
            result.put("status", DataStatus.NO_LIBRARY_SELECTED);
        }
        return result;
    }

    private LibraryMetadata constructLibraryMetadata(Path path) {
        return new LibraryMetadata(path.toString());
    }

    /**
     * Refresh the data for given library
     *
     * @param uuid library key
     */
    public void refreshData(String uuid) {
        Library library = libraries.get(uuid);
        if (library == null) {
            return;
        }
        if (library.checkAndSetDataStatus(DataStatus.REFRESH, REFRESH_START_ALLOWED_STATUSES)) {
            executor.submit(() -> refreshDataEx(library));
        }
    }

    /**
     * Get list of files in the library path
     * Get list of file info in the database
     * Get new and updated items by comparing files against database data
     * Get deleted items by comparing database data against files
     * Save updated data
     * Set last refresh data
     * Populate metadata information
     *
     * @param library library to refresh
     */
    private void refreshDataEx(Library library) {
        LOGGER.debug("refreshDataEx started");
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                library.setRefreshItemsCount(0);
                library.resetRefreshProceedCount();
                library.resetRefreshUpdatedCount();

                Future<List<Path>> filesGetFuture = executor.submit(() -> getFiles(Paths.get(library.getPath())));
                Future<List<FileInfo>> dbGetFuture = executor.submit(() -> getFileInfoList(library));

                List<Path> files = filesGetFuture.get();
                LOGGER.debug("files get " + files.size());
                LocalDateTime localDateTime = LocalDateTime.now();

                Map<Path, FileInfo> fileInfoList = FileInfoHelper.listOfFileInfoToMap(Paths.get(library.getPath()), dbGetFuture.get());
                LOGGER.debug("db file info get " + fileInfoList.size());

                library.setRefreshItemsCount(files.size() + fileInfoList.size());

                processFiles(library, files, fileInfoList);

                library.getDataService().commitFileInfo();
                library.getDataService().updateLastRefreshDate(localDateTime);

                library.populateMetadata();

            } catch (InterruptedException | ExecutionException | IOException | LibraryDatabaseException e) {
                LOGGER.error(e);
            } finally {
                semaphoreService.releaseGlobalAccess();
                library.checkAndSetDataStatus(DataStatus.IDLE, REFRESH_END_ALLOWED_STATUSES);
                LOGGER.debug("refreshDataEx ended");
            }
        }
    }

    /**
     * Get file info list from passed library
     * Throws exception to inform caller that operation was unsuccessful
     *
     * @param library library to get information
     * @return list of file info
     * @throws LibraryDatabaseException
     */
    private List<FileInfo> getFileInfoList(Library library) throws LibraryDatabaseException {
        List<FileInfo> result = Collections.emptyList();
        if (semaphoreService.acquireGlobalAccess()) {
            try {
                result = library.getFileInfoList();
            } catch (LibraryDatabaseException e) {
                LOGGER.debug("getFileInfoList was unsuccessful", e);
                throw e;
            } finally {
                semaphoreService.acquireGlobalAccess();
            }
        }
        return result;
    }

    /**
     * Get the list of files
     * Throws an exception to inform caller that operation was unsuccessful
     * Required 1 file access approve
     *
     * @param path directory path
     * @return list of path
     * @throws IOException
     */
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

    /**
     * Proceed found files and compare them with existed values to get new/updated items
     * Next proceed with items from DB to get deleted values
     *
     * @param library proceed library
     * @param files   list of files paths
     * @param infos   map of path:fileInfo items from database
     */
    private void processFiles(Library library, final List<Path> files, final Map<Path, FileInfo> infos) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        CountDownLatch countDownLatch = new CountDownLatch(files.size());
        LOGGER.debug("Proceed new or updated files");
        for (Path path : files) {
            executorService.submit(() -> processFile(library, path, infos.get(path), countDownLatch));
        }
        LOGGER.debug("Proceed deleted files");
        for (Path path : infos.keySet()) {
            executorService.submit(() -> {
                if (semaphoreService.acquireGlobalAccess()) {
                    try {
                        if (!files.contains(path)) {
                            library.getDataService().deleteFileInfo(infos.get(path));
                            LOGGER.debug("Deleted " + path);
                        }
                    } finally {
                        semaphoreService.releaseGlobalAccess();
                        library.incrementRefreshProceedCount();
                    }
                }
            });
        }
        executorService.shutdown();
        LOGGER.debug("Starting to await for tasks finish");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOGGER.debug("Awaiting cancelled", e);
        }
        LOGGER.debug("Finished awaiting for tasks finish");
    }

    private void processFile(Library library, Path path, FileInfo fileInfo, CountDownLatch countDownLatch) {
        library.incrementRefreshProceedCount();

        boolean inserted = false;
        Path relativePath = Paths.get(library.getPath()).relativize(path);
        if (fileInfo == null) {
            fileInfo = new FileInfo(relativePath.toString());
            inserted = true;
        }
        try {
            if (semaphoreService.acquireFilesAccess()) {
                try {
                    if (fileService.checkFileInfoIsChangedAndUpdateIt(fileInfo, path)) {
                        if (inserted) {
                            fileInfo.setUuid(UUID.randomUUID());
                            library.insertFileInfo(fileInfo);
                            LOGGER.debug("Inserted " + path);
                        } else {
                            library.updateFileInfo(fileInfo);
                            LOGGER.debug("Updated " + path);
                        }
                        library.incrementRefreshUpdatedCount();
                    }
                } catch (IOException e) {
                    LOGGER.error("Can't proceed " + path, e);
                } finally {
                    semaphoreService.releaseFilesAccess();
                }
            }
        } finally {
            countDownLatch.countDown();
        }
    }

    /**
     * Set library dataStatus to CANCELLING if current dataStatus is REFRESH
     * Doing nothing if library is not found
     *
     * @param libraryUUID uuid of library
     */
    public void stopRefreshData(String libraryUUID) {
        Library library = libraries.get(libraryUUID);
        if (library == null) {
            return;
        }
        library.checkAndSetDataStatus(DataStatus.CANCELLING, REFRESH_CANCEL_ALLOWED_STATUSES);
    }

    public void addLibrary(Map<String, String> metaData) {

    }
}