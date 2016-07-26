package org.library.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.ParsedFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;

public class Fb2Parser implements FileParser {
    private static Logger LOGGER = LogManager.getLogger(Fb2Parser.class);

    @Override
    public boolean parse(ParsedFile parsedFile) {
        boolean result = false;
        try {
            SAXParser parser = ParsedFileUtils.getSaxParser();
            parser.parse(parsedFile.getPath().toFile(), new Fb2ParserHandler(parsedFile));
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
