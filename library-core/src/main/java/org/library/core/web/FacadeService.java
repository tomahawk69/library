package org.library.core.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.utils.LoggingUtils;
import org.library.core.services.LibraryService;
import org.library.entities.LibraryWeb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
class FacadeService {
    private final static Logger LOGGER = LogManager.getLogger(FacadeService.class);

    private LibraryService libraryService;

    @Autowired
    FacadeService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @RequestMapping(value = "/refreshData/{uuid}", produces= MediaType.TEXT_HTML_VALUE)
    @ResponseBody String refreshData(@PathVariable("uuid") String uuid) {
        LOGGER.info("refreshData for " + uuid);
        libraryService.refreshData(uuid);
        return "Refresh started";
    }

    @RequestMapping(value = "/stopRefreshData/{uuid}", produces= MediaType.TEXT_HTML_VALUE)
    @ResponseBody String stopRefreshData(@PathVariable("uuid") String uuid) {
        LOGGER.info("stopRefreshData for " + uuid);
        libraryService.stopRefreshData(uuid);
        return "Refresh is cancelling";
    }

    @RequestMapping(value = "/getDataStatus/{uuid}", produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody Map<String, Object> getDataStatus(@PathVariable("uuid") String uuid) {
        LOGGER.debug("getDataStatus for " + uuid);
        return libraryService.getDataStatus(uuid);
    }

    @RequestMapping(value = "/getLibraries", produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody Collection<LibraryWeb> getLibraries() {
        LOGGER.debug("getLibraries");
        return libraryService.getLibraries();
    }

    @RequestMapping(value = "/debugEnabled", produces= MediaType.APPLICATION_JSON_VALUE, method = POST)
    @ResponseBody String setDebugEnabled(@RequestParam Boolean debugEnabled) {
        LOGGER.debug("setDebugEnabled " + debugEnabled);
        LoggingUtils.setDebugEnabled(debugEnabled);
        return getDebugEnabled();
    }

    @RequestMapping(value = "/debugEnabled", produces= MediaType.APPLICATION_JSON_VALUE, method = GET)
    @ResponseBody String getDebugEnabled() {
        LOGGER.debug("getDebugEnabled");
        return String.valueOf(LoggingUtils.getDebugEnabled());
    }
}
