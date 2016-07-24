package org.library.common.utils;

import org.library.common.entities.ParsedFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ParsedFileUtils {

    public static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    public static ParsedFile.Element createElement(String name) {
        ParsedFile.Element result = new ParsedFile.Element(name);
        return result;
    }

    public static ParsedFile.Element addElement(ParsedFile.Element parent, String name) {
        ParsedFile.Element result = createElement(name);
        result.setParent(parent);
        parent.addElement(result);
        return result;
    }

    public static void addAttribute(ParsedFile.Element element, String key, String value) {
        element.addAttribute(key, value);
    }
}
