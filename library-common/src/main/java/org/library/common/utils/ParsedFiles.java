package org.library.common.utils;

import org.library.common.entities.ParsedFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ParsedFiles {

    private static final Set<String> NEW_LINE_TAGS = new HashSet<String>() {{
        add("br");
        add("p");
        add("div");
    }};

    public static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    public static ParsedFile.Element createTextElement(String text) {
        ParsedFile.Element result = new ParsedFile.TextElement(text);
        return result;
    }

    public static ParsedFile.Element createTagElement(String name) {
        ParsedFile.Element result = new ParsedFile.TagElement(name);
        return result;
    }

    public static ParsedFile.Element addTagElement(ParsedFile.Element parent, String name) {
        if (!(parent instanceof ParsedFile.TagElement)) {
            throw new IllegalArgumentException("TagElement only allowed");
        }
        ParsedFile.Element result = createTagElement(name);
        result.setParent(parent);
        ((ParsedFile.TagElement) parent).addElement(result);
        return result;
    }

    public static ParsedFile.Element addTextElement(ParsedFile.Element parent, String text) {
        if (!(parent instanceof ParsedFile.TagElement)) {
            throw new IllegalArgumentException("TagElement only allowed");
        }
        ParsedFile.Element result = createTextElement(text);
        result.setParent(parent);
        ((ParsedFile.TagElement) parent).addElement(result);
        return result;
    }

    public static void addAttribute(ParsedFile.Element element, String key, String value) {
        if (!(element instanceof ParsedFile.TagElement)) {
            throw new IllegalArgumentException("TagElement only allowed");
        }
        ((ParsedFile.TagElement) element).addAttribute(key, value);
    }

    public static ParsedFile.Element findElement(ParsedFile.Element element, String id) {
        if (element == null) {
            return null;
        }
        if (id.equalsIgnoreCase(element.getName())) {
            return element;
        }
        ParsedFile.Element result;
        for (ParsedFile.Element el : element) {
            if ((result = findElement(el, id)) != null) {
                return result;
            }
        }
        return null;
    }

    public static String elementDeepToString(ParsedFile.Element container) {
        StringBuilder result = new StringBuilder();
        for (ParsedFile.Element element : container) {
            if (element instanceof ParsedFile.TextElement) {
                result.append(((ParsedFile.TextElement) element).getText());
            } else if (element instanceof ParsedFile.TagElement) {
                if (NEW_LINE_TAGS.contains(element.getName()) && (result.length() > 0)) {
                    result.append(System.lineSeparator());
                }
                result.append(elementDeepToString(element));
            }
        }
        return result.toString();
    }

    public static String elementText(ParsedFile.Element element, String delimiter) {
        if (element == null) {
            return null;
        }
        return element.stream()
                .filter(e -> e instanceof ParsedFile.TextElement)
                .map(e -> ((ParsedFile.TextElement) e).getText().trim())
                .collect(Collectors.joining(delimiter));
    }
}
