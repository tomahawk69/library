package org.library.core.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.core.services.LibraryService;
import org.library.common.utils.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class FacadeService {
    private final static Logger LOGGER = LogManager.getLogger(FacadeService.class);

    private LibraryService libraryService;

    @Autowired
    public FacadeService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @RequestMapping(value = "/refreshData", produces= MediaType.TEXT_HTML_VALUE)
    @ResponseBody String refreshData() {
        LOGGER.info("refreshData");
        libraryService.refreshData();
        return "Refresh started";
    }

    @RequestMapping(value = "/stopRefreshData", produces= MediaType.TEXT_HTML_VALUE)
    @ResponseBody String stopRefreshData() {
        LOGGER.info("stopRefreshData");
        libraryService.stopRefreshData();
        return "Refresh is cancelling";
    }

    @RequestMapping(value = "/getDataStatus", produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody Map<String, Object> getDataStatus() {
        LOGGER.debug("getDataStatus");
        return libraryService.getDataStatus();
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
