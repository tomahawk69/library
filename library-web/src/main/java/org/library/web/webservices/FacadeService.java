package org.library.web.webservices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.controllers.LibraryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FacadeService {
    private final static Logger LOGGER = LogManager.getLogger(FacadeService.class);

    @Autowired
    private LibraryController libraryController;

    @RequestMapping("/")
    @CrossOrigin(origins = "http://localhost")
    public @ResponseBody String index() {
        LOGGER.info("hello world greeting");
        return "Hello World!";
    }

    @RequestMapping(value = "/refreshData", produces= MediaType.TEXT_HTML_VALUE)
    // TODO make CORS global
    @CrossOrigin(origins = "http://localhost:3000")
    public @ResponseBody String refreshData() {
        LOGGER.info("refreshData");
        libraryController.refreshData();
        return "Refresh started";
    }

    @RequestMapping(value = "/stopRefreshData", produces= MediaType.TEXT_HTML_VALUE)
    @CrossOrigin(origins = "http://localhost:3000")
    public @ResponseBody String stopRefreshData() {
        LOGGER.info("stopRefreshData");
        libraryController.stopRefreshData();
        return "Refresh is cancelling";
    }

    @RequestMapping(value = "/getDataStatus", produces= MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000")
    public @ResponseBody
    Map<String, Object> getDataStatus() {
        LOGGER.debug("getDataStatus");
        return libraryController.getDataStatus();
    }
}
