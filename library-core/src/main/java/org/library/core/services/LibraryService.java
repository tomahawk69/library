package org.library.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@Scope("singleton")
public class LibraryService {
    private static final Logger LOGGER = LogManager.getLogger(LibraryService.class);

    private final LibraryManager libraryManager;
    private final DataServiceFactory dataServiceFactory;
    private final SemaphoreService semaphoreService;

    private final ExecutorService executor;

    @Autowired
    public LibraryService(DataServiceFactory dataServiceFactory,
                          LibraryManager libraryManager,
                          SemaphoreService semaphoreService) {
        this.semaphoreService = semaphoreService;
        this.executor = Executors.newFixedThreadPool(semaphoreService.getMaxAccessThreadsCount());
        this.libraryManager = libraryManager;
        this.dataServiceFactory = dataServiceFactory;
    }

    public void refreshData() {
        LOGGER.info("refresh data");
        libraryManager.refreshData(libraryManager.getCurrentLibrary());
    }

    public Map<String, Object> getDataStatus() {
        return libraryManager.getDataStatus(libraryManager.getCurrentLibrary());
    }

    public void stopRefreshData() {
        libraryManager.stopRefreshData(libraryManager.getCurrentLibrary());
    }
}