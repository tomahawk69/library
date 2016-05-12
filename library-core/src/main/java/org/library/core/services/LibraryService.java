package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.DataStatus;
import org.library.core.utils.FileUtils;
import org.library.entities.FileInfo;
import org.library.entities.FileInfoHelper;
import org.library.entities.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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

@Service
@Scope("singleton")
public class LibraryService {
    private static final Logger LOGGER = LogManager.getLogger(LibraryService.class);
    private static final List<String> EXTENSIONS = FileType.getExtensions();
    private final Path path;
    private DataStatus dataStatus = DataStatus.INITIALIZING;
    private StampedLock dataLock = new StampedLock();
    private List<Path> files;
    private Map<String, FileInfo> filesInfoMap;
    private ExecutorService executor;
    private AtomicInteger threadCounter = new AtomicInteger();

    private DataService dataService;

    @Autowired
    public LibraryService(@Qualifier("dataServiceDBImpl") DataService dataService,
                            @Value("${library.path}") String path,
                            @Value("${threads.count}") int threadsCount) {
        this.path = Paths.get(path);
        executor = Executors.newFixedThreadPool(threadsCount);
        dataStatus = DataStatus.IDLE;
        filesInfoMap = new HashMap<>();
        this.dataService = dataService;
        this.dataService.setLibraryPath(this.path);
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
                commitOrRollback();
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

    /**
     *
     */
    private void commitOrRollback() {
        if (dataStatus.equals(DataStatus.REFRESH)) {
            dataService.commit();
        } else {
            dataService.rollback();
        }
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
        List<Future<?>> futures = new ArrayList<>();
        threadCounter.set(0);
        for (Path filePath : files) {
            if (dataStatus == DataStatus.REFRESH) {
                futures.add(executor.submit(() -> {
                    try {
                        proceedFileInfo(filePath);
                    } catch (IOException | NoSuchAlgorithmException e) {
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
        LOGGER.info(String.format("Finally updated %d from %d files", threadCounter.get(), files.size()));
    }

    private void proceedFileInfo(Path path) throws IOException, NoSuchAlgorithmException {
        String relativePath = FileUtils.constructRelativePath(this.path, path);
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
        dataService.insert(fileInfo);
    }

    private void addFileInfoToUpdate(FileInfo fileInfo, FileInfo rollbackCopy) {
        dataService.update(fileInfo, rollbackCopy);
    }

    private boolean validateFileInfo(FileInfo fileInfo, Long fileSize, LocalDateTime fileDate) {
        return fileSize.equals(fileInfo.getFileSize()) && fileDate.equals(fileInfo.getModifiedDate());
    }

    boolean setDataStatus(DataStatus dataStatus, Long stamp) {
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
        }
        return result;
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