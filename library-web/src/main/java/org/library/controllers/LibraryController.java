package org.library.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.services.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LibraryController {
    private static final Logger LOGGER = LogManager.getLogger(LibraryController.class);

    @Autowired
    private LibraryService libraryService;

    public void refreshData() {
        LOGGER.info("refreshData");
        libraryService.refreshData();
    }

    public Map<String, Object> getDataStatus() {
        return libraryService.getDataStatus();
    }

    public void stopRefreshData() {
        LOGGER.info("stopRefreshData");
        libraryService.stopRefreshData();
    }
}
