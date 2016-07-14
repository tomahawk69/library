package org.library.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileType;
import org.library.common.services.FileService;

import java.nio.file.Path;
import java.util.List;

public class ParserImpl implements Parser {
    private final Path path;
    private static final Logger LOGGER =LogManager.getLogger(ParserImpl.class);

    private final FileService fileService;

    ParserImpl(FileService fileService, Path path) {
        this.path = path;
        this.fileService = fileService;
    }

    @Override
    public Boolean call() throws Exception {
        LOGGER.info("Started " + this);
        System.out.println(fileService);
        List<Path> files = fileService.getFilesList(FileType.getExtensions(), true, path);
        LOGGER.info("Ended " + this);
        LOGGER.info("Loaded " + files.size() + " files");
        return false;
    }

    @Override
    public String toString() {
        return "ParserImpl{" +
                "path=" + path +
                " service=" + fileService +
                '}';
    }
}
