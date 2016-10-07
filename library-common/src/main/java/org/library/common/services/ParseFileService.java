package org.library.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.ParsedFile;
import org.library.common.utils.FileParser;

import java.nio.file.Path;

public class ParseFileService {
    private static final Logger LOGGER = LogManager.getLogger(ParseFileService.class);

    public ParseFileService() {
    }

    public ParsedFile fileInfoToParsedFile(FileInfo fileInfo) {
        ParsedFile parsedFile = new ParsedFile(fileInfo);
        return parsedFile;
    }

    public ParsedFile pathToParsedFile(Path basepath, Path path) {
        ParsedFile parsedFile = new ParsedFile(basepath.relativize(path).toString());
        return parsedFile;
    }

    public void parseFile(Path basePath, ParsedFile parsedFile) {
        if (parseFileInt(basePath, parsedFile)) {
            parsedFile.setState(ParsedFile.ProcessState.XMLProcessed);
        }
    }

    private boolean parseFileInt(Path basePath, ParsedFile parsedFile) {
        boolean result = false;
        try {
            FileParser parser = FileParser.createHandler(parsedFile.getFileInfo().getFileType());
            result = parser.parseFile(basePath, parsedFile);
        } catch (IllegalArgumentException ex) {
            parsedFile.addException(ex);
            LOGGER.error("Cannot parseFile file " + parsedFile.getFileInfo().getPath());
        }
        return result;
    }

    public void parseInfo(ParsedFile parsedFile) {
        if (parseFileInfoInt(parsedFile)) {
            parsedFile.setState(ParsedFile.ProcessState.BookInfoProcessed);
        }
    }

    private boolean parseFileInfoInt(ParsedFile parsedFile) {
        boolean result = false;
        try {
            FileParser parser = FileParser.createHandler(parsedFile.getFileInfo().getFileType());
            result = parser.parseFileData(parsedFile);
        } catch (IllegalArgumentException ex) {
            parsedFile.addException(ex);
            LOGGER.error("Cannot parseFileInfo for " + parsedFile.getFileInfo().getPath());
        }
        return result;
    }
}
