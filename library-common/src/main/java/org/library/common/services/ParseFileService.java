package org.library.common.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.ParsedFile;
import org.library.common.utils.FileParseHandler;
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

    public void parseXml(ParsedFile parsedFile) {
        if (parseXmlInt(parsedFile)) {
            parsedFile.setState(ParsedFile.ProcessState.XMLProcessed);
        }
    }

    private boolean parseXmlInt(ParsedFile parsedFile) {
        boolean result = false;
        try {
            SAXParser parser = ParsedFileUtils.getSaxParser();
            parser.parse(parsedFile.getPath().toFile(), FileParseHandler.createHandler(parsedFile.getFileInfo().getFileType(), parsedFile));
            result = true;
        } catch (ParserConfigurationException e) {
            LOGGER.error("ParserConfigurationException: ", e);
        } catch (SAXException e) {
            LOGGER.error("Parsing exception: ", e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Cannot create parser: ", e);
        } catch (IOException e) {
            LOGGER.error("IOException: ", e);
        }
        return result;
    }
}
