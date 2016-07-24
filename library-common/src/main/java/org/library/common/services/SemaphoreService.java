package org.library.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Semaphore;

public class SemaphoreService {
    private final Logger LOGGER = LogManager.getLogger(SemaphoreService.class);
    private final Semaphore globalAccessControl;
    private final Semaphore globalFileControl;
    private final int maxFilesThreadsCount;
    private final int maxAccessThreadsCount;

    public SemaphoreService(int threadsCount, int threadsFilesCount) {
        this.maxAccessThreadsCount = threadsCount;
        this.maxFilesThreadsCount = threadsFilesCount;
        this.globalAccessControl = new Semaphore(threadsCount);
        this.globalFileControl = new Semaphore(threadsFilesCount);
    }

    public int getMaxAccessThreadsCount() {
        return maxAccessThreadsCount;
    }

    public int getMaxFilesThreadsCount() {
        return maxFilesThreadsCount;
    }

    public boolean acquireGlobalAccess() {
        try {
            globalAccessControl.acquire();
            return true;
        } catch (InterruptedException e) {
            LOGGER.error("acquireGlobalAccess error", e);
        }
        return false;
    }

    public boolean acquireFilesAccess() {
        try {
            globalFileControl.acquire();
            return true;
        } catch (InterruptedException e) {
            LOGGER.error("acquireFilesAccess error", e);
        }
        return false;
    }

    public void releaseFilesAccess() {
        releaseFilesAccess(1);
    }

    public void releaseFilesAccess(int releaseCount) {
        globalFileControl.release(releaseCount);
    }

    public void releaseGlobalAccess() {
        releaseGlobalAccess(1);
    }

    public void releaseGlobalAccess(int releaseCount) {
        globalAccessControl.release(releaseCount);
    }
}
