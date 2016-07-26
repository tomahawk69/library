package org.library.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.ParsedFile;
import org.library.common.utils.FileParser;
import org.library.common.utils.ParsedFileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.nio.file.Path;

public class ParseFileService {
    private static final Logger LOGGER = LogManager.getLogger(ParseFileService.class);

    public ParseFileService() {
    }

    public ParsedFile fileInfoToParsedFile(Path basePath, FileInfo fileInfo) {
        ParsedFile parsedFile = new ParsedFile(basePath.resolve(fileInfo.getPath()), fileInfo);
        return parsedFile;
    }

    public ParsedFile pathToParsedFile(Path path) {
        ParsedFile parsedFile = new ParsedFile(path, new FileInfo(path.toString()));
        return parsedFile;
    }

    public void parseFile(ParsedFile parsedFile) {
        if (parseFileInt(parsedFile)) {
            parsedFile.setState(ParsedFile.ProcessState.XMLProcessed);
        }
    }

    private boolean parseFileInt(ParsedFile parsedFile) {
        boolean result = false;
        try {
            FileParser parser = FileParser.createHandler(parsedFile.getFileInfo().getFileType());
            result = parser.parse(parsedFile);
        } catch (IllegalArgumentException ex) {
            parsedFile.addException(ex);
            LOGGER.error("Cannot parse file " +  parsedFile.getFileInfo().getPath());
        }
        return result;
    }
}
