package org.library.parser.parser;

import org.library.common.services.FileService;
import org.library.common.services.ParseFileService;
import org.library.common.services.SemaphoreService;
import org.library.parser.services.ParserStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ParserFactory {

    @Autowired
    private FileService fileService;

    @Autowired
    private ParseFileService parseFileService;

    @Autowired
    SemaphoreService semaphoreService;

    @Autowired
    ParserStorageService parserStorageService;

    private boolean calcMD5hash;
    private String allowedExtensions;

    @Autowired
    public void setCalcMD5hash(@Value("${fileinfo.calcMD5hash}") boolean calcMD5hash) {
        this.calcMD5hash = calcMD5hash;
    }

    @Autowired
    public void setAllowedExtensions(@Value("${allowed.extensions}") String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public Parser createParser(Path path) {
        ParserImpl parser = new ParserImpl(fileService, parseFileService, semaphoreService, parserStorageService, path);
        parser.setCalcMD5hash(calcMD5hash);
        parser.setAllowedExtensions(getAllowedExtensionsList(allowedExtensions));
        return parser;
    }

    private List<String> getAllowedExtensionsList(String allowedExtensions) {
        List<String> extensions;
        if (allowedExtensions == null) {
            extensions = Collections.emptyList();
        } else {
            extensions = Arrays.asList(allowedExtensions.toUpperCase().split("\\s*,\\s*"));
        }
        return extensions;
    }

}
