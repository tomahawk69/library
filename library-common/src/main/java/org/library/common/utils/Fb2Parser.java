package org.library.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.BookInfo;
import org.library.common.entities.ParsedFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Fb2Parser implements FileParser {
    private static Logger LOGGER = LogManager.getLogger(Fb2Parser.class);
    private static final String TAG_ELEMENT_TITLE_INFO = "title-info";
    private static final String TAG_ELEMENT_BOOK_TITLE = "book-title";
    private static final String TAG_ELEMENT_GENRE = "genre";
    private static final String TAG_ELEMENT_DATE = "date";
    private static final String TAG_ELEMENT_ANNOTATION = "annotation";
    private static final String TAG_ELEMENT_GENRE_MATCH = "match";
    private static final String TAG_ELEMENT_SEQUENCE = "sequence";
    private static final String TAG_ELEMENT_SEQUENCE_NAME = "name";
    private static final String TAG_ELEMENT_SEQUENCE_NUMBER = "number";
    private static final String TAG_ELEMENT_LANGUAGE = "lang";
    private static final String TAG_ELEMENT_SOURCE_LANGUAGE = "src-lang";

    private static int currentYear;

    static {
        currentYear = LocalDateTime.now().getYear();
    }

    @Override
    public boolean parseFile(Path basePath, ParsedFile parsedFile) {
        boolean result = false;
        try {
            SAXParser parser = ParsedFiles.getSaxParser();
            parser.parse(basePath.resolve(parsedFile.getFileInfo().getPath()).toFile(), new Fb2ParserHandler(parsedFile));
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

    @Override
    public boolean parseFileData(ParsedFile parsedFile) {
        parseTitleInfo(parsedFile);
        return true;
    }

    private void parseTitleInfo(ParsedFile parsedFile) {
        ParsedFile.Element root = ParsedFiles.findElement(parsedFile.getHeader(), TAG_ELEMENT_TITLE_INFO);
        if (root == null) return;
        BookInfo bookInfo = parsedFile.getBookInfo();
        bookInfo
                .setTitle(parseTitle(root))
                .setLanguage(parseLanguage(root))
                .setSourceLanguage(parseSourceLanguage(root))
                .setGenres(parseGenres(root))
                .setSequences(parseSequences(root))
                .setAnnotation(parseAnnotation(root))
                .setYear(parseYear(root));
        validateBookInfo(bookInfo);
//        System.out.println(parsedFile.getBookInfo());
    }

    private void validateBookInfo(BookInfo bookInfo) {
        if (bookInfo.getSourceLanguage() == null) {
            bookInfo.setSourceLanguage(bookInfo.getLanguage());
        }
    }

    private String parseAnnotation(ParsedFile.Element root) {
        ParsedFile.Element element = ParsedFiles.findElement(root, TAG_ELEMENT_ANNOTATION);
        String result = null;
        if (element != null) {
            result = ParsedFiles.elementDeepToString(element);
        }
        return result;
    }

    private Integer parseYear(ParsedFile.Element root) {
        String yearString = ParsedFiles.elementText(ParsedFiles.findElement(root, TAG_ELEMENT_DATE), "");
        Integer result = stringToNumber(yearString, null);
        if (result <= currentYear + 1) {
            return result;
        }
        return null;
    }

    private String parseLanguage(ParsedFile.Element root) {
        return ParsedFiles.elementText(ParsedFiles.findElement(root, TAG_ELEMENT_LANGUAGE), "");
    }

    private String parseSourceLanguage(ParsedFile.Element root) {
        return ParsedFiles.elementText(ParsedFiles.findElement(root, TAG_ELEMENT_SOURCE_LANGUAGE), "");
    }

    private String parseTitle(ParsedFile.Element root) {
        return ParsedFiles.elementText(ParsedFiles.findElement(root, TAG_ELEMENT_BOOK_TITLE), "");
    }

    private Map<String, Integer> parseSequences(ParsedFile.Element root) {
        Map<String, Integer> sequences = root.stream()
                .filter(e -> e.getName().equalsIgnoreCase(TAG_ELEMENT_SEQUENCE))
                .filter(e -> e instanceof ParsedFile.TagElement)
                .map(e -> (ParsedFile.TagElement) e)
                .map(t -> new AbstractMap.SimpleEntry<>(t.getAttribute(TAG_ELEMENT_SEQUENCE_NAME),
                        stringToNumber(t.getAttribute(TAG_ELEMENT_SEQUENCE_NUMBER), null)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        if (sequences.size() == 0) {
            return null;
        } else {
            return sequences;
        }
    }

    private Map<String, Integer> parseGenres(ParsedFile.Element root) {
        Map<String, Integer> genres = root.stream()
                .filter(e -> e.getName().equalsIgnoreCase(TAG_ELEMENT_GENRE))
                .filter(e -> e instanceof ParsedFile.TagElement)
                .map(e -> (ParsedFile.TagElement) e)
                .map(t -> new AbstractMap.SimpleEntry<>(ParsedFiles.elementText(t, ""),
                        stringToNumber(t.getAttribute(TAG_ELEMENT_GENRE_MATCH), 0)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        if (genres.size() == 0) {
            return null;
        } else {
            if (genres.size() == 1) {
                genres.put(genres.keySet().iterator().next(), 100);
            }
            return genres;
        }
    }

    private Integer stringToNumber(String stringValue, Integer defaultValue) {
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

}
