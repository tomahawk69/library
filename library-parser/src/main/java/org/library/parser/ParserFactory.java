package org.library.parser;

import org.library.common.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ParserFactory {

    @Autowired
    private FileService fileService;

    public Parser createParser(Path path) {
        return new ParserImpl(fileService, path);
    }

}
