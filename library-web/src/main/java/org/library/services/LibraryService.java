package org.library.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.DataService;
import org.library.core.DataServiceFileImpl;
import org.library.core.FileUtils;
import org.library.entities.FileInfo;
import org.library.entities.FileInfoHelper;
import org.library.entities.FileUpdateOperation;
import org.library.web.entities.DataStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

@Component
@Scope("singleton")
public class LibraryService {
    private static final Logger LOGGER = LogManager.getLogger(LibraryService.class);
    private static final List<String> EXTENSIONS = Arrays.asList(".fb2", ".epub", ".zip");
    private final Path path;
    private DataStatus dataStatus = DataStatus.INITIALIZING;
    private StampedLock dataLock = new StampedLock();
    private List<Path> files;
    private Map<String, FileInfo> filesInfoMap;
    private ExecutorService executor;
    private List<Future<?>> futures = new ArrayList<>();
    private AtomicInteger threadCounter = new AtomicInteger();

    @Autowired
    private DataService dataService = new DataServiceFileImpl();

    @Autowired
    public LibraryService(@Value("${library.path}") String path, @Value("${threads.count}") int threadsCount) {
        this.path = Paths.get(path);
        executor = Executors.newFixedThreadPool(threadsCount);
        dataStatus = DataStatus.IDLE;
        filesInfoMap = new HashMap<>();
        dataService.setLibraryPath(this.path);
    }

    public void refreshData() {
        LOGGER.info("refresh data");
        Long stamp = dataLock.tryOptimisticRead();
        if (!dataLock.validate(stamp)) {
            stamp = dataLock.readLock();
        }
        try {
            if (dataStatus.getRefreshAllowed()) {
                if (setDataStatus(DataStatus.REFRESH, stamp)) {
                    refreshDataInt();
                }
            } else {
                dataLock.unlock(stamp);
            }
        } finally {
            if (dataLock.validate(stamp)) {
                dataLock.unlock(stamp);
            }
        }
    }

    private void refreshDataInt() {
        executor.submit(() -> {
            try {
                refreshListOfFiles();
            } catch (InterruptedException e) {
                LOGGER.debug("Thread is interrupted");
            } catch (ExecutionException e) {
                LOGGER.error("Execution exception");
            } catch (IOException e) {
                LOGGER.error("IOException", e);
            } finally {
                setDataStatus(DataStatus.IDLE, null);
            }
        });
    }

    private void refreshListOfFiles() throws ExecutionException, InterruptedException, IOException {
        LOGGER.info("Starting acquire files");
        if (files != null) {
            files.clear();
        }
        files = FileUtils.getFilesList(EXTENSIONS, true, path);
        LOGGER.info("Finishing acquire files");
        LOGGER.info("The count of files: " + files.size());
        updateFilesInfo();
    }

    private void updateFilesInfo() throws ExecutionException, InterruptedException {
        LOGGER.info("updateFilesInfo started");
        futures = new ArrayList<>();
        threadCounter.set(0);
        for (Path filePath : files) {
            if (dataStatus == DataStatus.REFRESH) {
                futures.add(executor.submit(() -> {
                    try {
                        proceedFileInfo(filePath);
                    } catch (IOException e) {
                        LOGGER.error(e);
                    } catch (NoSuchAlgorithmException e) {
                        LOGGER.error(e);
                    }
                }));
            } else {
                break;
            }
        }
        for (Future<?> future : futures) {
            if (dataStatus != DataStatus.REFRESH) {
                future.cancel(true);
            } else {
                future.get();
            }
        }
        LOGGER.debug(String.format("Finally updated %d from %d files", threadCounter.get(), files.size()));
    }

    private void proceedFileInfo(Path path) throws IOException, NoSuchAlgorithmException {
        String relativePath = FileUtils.constructRelativePath(this.path, path).toString();
        Long fileSize = FileUtils.getFileSize(path);
        LocalDateTime fileDate = FileUtils.getFileLastModifiedDate(path);
        FileInfo fileInfo = filesInfoMap.get(relativePath);
        if (fileInfo != null && !validateFileInfo(fileInfo, fileSize, fileDate)) {
            FileInfo rollbackCopy = FileInfoHelper.createFileInfoCopy(fileInfo);
            FileInfoHelper.updateFileInfo(this.path, fileInfo, fileSize, fileDate);
            addFileInfoToUpdate(fileInfo, rollbackCopy);
            LOGGER.debug("Updated " + fileInfo.getPath());
        } else {
            fileInfo = new FileInfo(relativePath);
            FileInfoHelper.updateFileInfo(this.path, fileInfo, fileSize, fileDate);
            filesInfoMap.put(fileInfo.getPath(), fileInfo);
            addFileInfoToInsert(fileInfo);
            LOGGER.debug("Added " + fileInfo.getPath());
        }
        int i = threadCounter.incrementAndGet();
        LOGGER.debug(String.format("Updated %d from %d files", i, files.size()));
    }

    private void addFileInfoToInsert(FileInfo fileInfo) {
        fileInfo.setUuid(UUID.randomUUID());
        new FileUpdateOperation(FileUpdateOperation.UpdateType.INSERT, fileInfo);
    }

    private void addFileInfoToUpdate(FileInfo fileInfo, FileInfo rollbackCopy) {
        new FileUpdateOperation(FileUpdateOperation.UpdateType.UPDATE, fileInfo, rollbackCopy);
    }

    public boolean validateFileInfo(FileInfo fileInfo, Long fileSize, LocalDateTime fileDate) {
        return fileSize.equals(fileInfo.getFileSize()) && fileDate.equals(fileInfo.getModifiedDate());
    }

    protected boolean setDataStatus(DataStatus dataStatus, Long stamp) {
        LOGGER.debug("Set data status to " + dataStatus);
        boolean result = false;
        if (stamp != null) {
            stamp = dataLock.tryConvertToWriteLock(stamp);
        } else {
            stamp = dataLock.tryWriteLock();
        }
        try {
            if (dataLock.validate(stamp)) {
                this.dataStatus = dataStatus;
                result = true;
            } else {
                LOGGER.error("Couldn't acquire lock");
            }
        } finally {
            if (dataLock.validate(stamp)) {
                dataLock.unlockWrite(stamp);
            }
            return result;
        }
    }

    public Map<String, Object> getDataStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", dataStatus);
        result.put("count", (files == null ? 0 : files.size()));
        result.put("updated", threadCounter.get());
        return result;
    }

    public void stopRefreshData() {
        setDataStatus(DataStatus.CANCELLING, null);
    }
}