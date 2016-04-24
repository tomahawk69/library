package org.library.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.FileUtils;
import org.library.core.FileUtilsImpl;
import org.library.entities.FileInfo;
import org.library.web.entities.DataStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;

@Component
@Scope("singleton")
public class LibraryService {
    private static final Logger LOGGER = LogManager.getLogger(LibraryService.class);
    private static final List<String> EXTENSIONS = Arrays.asList(".fb2", ".epub", ".zip");
    private FileUtils fileUtils = new FileUtilsImpl();
    private final Path path;
    private DataStatus dataStatus = DataStatus.INITIALIZING;
    private StampedLock dataLock = new StampedLock();
    private List<Path> files;
    private List<FileInfo> fileInfos;
    private ExecutorService executor;
    private List<Future<?>> futures = new ArrayList<>();

    public LibraryService() {
        // TODO Move in parameters
        path = Paths.get("D:\\projects\\fb2\\~lib");
        executor = Executors.newFixedThreadPool(25);
        dataStatus = DataStatus.IDLE;
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
        if (fileInfos != null) {
            fileInfos.clear();
        }
        fileInfos = Collections.synchronizedList(new ArrayList<>());
        files = fileUtils.getFilesList(EXTENSIONS, true, path);
        LOGGER.info("Finishing acquire files");
        LOGGER.info("The count of files: " + files.size());
        updateFilesInfo();
    }

    private void updateFilesInfo() throws ExecutionException, InterruptedException {
        LOGGER.info("updateFilesInfo started");
        futures = new ArrayList<>();
        for (Path path : files) {
            if (dataStatus == DataStatus.REFRESH) {
                FileInfo fileInfo = new FileInfo(path);
                futures.add(executor.submit(() -> {
                    try {
                        fileInfos.add(fileInfo);
                        fileInfo.setFileSize(fileUtils.getFileSize(fileInfo.getPath()));
                        fileInfo.setModifiedDate(fileUtils.getFileLastModifiedDate(fileInfo.getPath()));
                        fileInfo.setMd5Hash(fileUtils.getFileMD5Hash(fileInfo.getPath()));
                        LOGGER.debug("Added " + fileInfo.getPath());
                        LOGGER.debug(String.format("Updated %d from %d files", fileInfos.size(), files.size()));
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
        LOGGER.debug(String.format("Finally updated %d from %d files", fileInfos.size(), files.size()));
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
        result.put("updated", (fileInfos == null ? 0 : fileInfos.size()));
        return result;
    }

    public void stopRefreshData() {
        setDataStatus(DataStatus.CANCELLING, null);
    }
}