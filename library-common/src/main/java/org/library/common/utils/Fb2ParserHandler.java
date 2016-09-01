package org.library.common.utils;

import java.util.Base64;

import org.library.common.entities.ParsedFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static org.library.common.utils.ParseStage.*;

/**
 * FB2 handler allowed to scan FB2 2.0 documents
 * - header: main element is "description"
 * It includes ALL the tree of sub-elements
 * Attributes are stored as map where key is stripped from namespace prefix (usually l:)
 * - section: main (first) body of the document
 * It includes ALL the tree of sub-sections
 * Every sections may have 0-n Titles
 * - count of notes
 * - count of comments
 * - cover name, cover type, cover bytes (should we decode them?)
 * Limitations:
 * - notes and comments don't any validation for empty or head sections
 * - section titles doesn't support strip or resolve links to notes/comments
 */
public class Fb2ParserHandler extends DefaultHandler {
    private static final List<String> newLineTitles = Arrays.asList("p", "empty-line");

    public static final String TAG_IMAGE_CONTENT_TYPE = "content-type";
    public static final String TAG_ELEMENT_ID = "id";
    public static final String TAG_ELEMENT_NAME = "name";
    public static final String TAG_SECTION_TITLE = "title";
    public static final String TAG_SECTION = "section";
    public static final String TAG_IMAGE_LINK = "href";
    public static final String TAG_IMAGE = "image";

    private final ParsedFile parsedFile;
    private ParseStage stage = ParseStage.None;
    private boolean isHead = false;
    private boolean isBody = false;
    private boolean isCover = false;
    private boolean isNotes = false;
    private boolean isComments = false;
    private boolean isCoverExists = false;
    private int notesCount;
    private int commentsCount;
    private String coverName;
    private String coverType;
    private String currentTag;
    private ParsedFile.Element currentElement;
    private ParsedFile.Section currentSection;
    private boolean isSectionTitle;
    private StringBuilder cover = new StringBuilder();
    private String nameSpace;

    public Fb2ParserHandler(ParsedFile parsedFile) {
        this.parsedFile = parsedFile;
    }

    @Override
    public void startDocument() throws SAXException {
        setStage(None);
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        setStage(None);
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTag = qName;
        switch (stage) {
            case None:
                if (qName.equalsIgnoreCase(Started.getElementTag())) {
                    setStage(Started);
                    extractNameSpace(attributes);
                }
                break;
            case Started: {
                if (qName.equalsIgnoreCase(ParseStage.Head.getElementTag())) {
                    if (!isHead) {
                        setStage(Head);
                        currentElement = new ParsedFile.Element("header");
                        parsedFile.setHeader(currentElement);
                        proceedAttributes(currentElement, attributes);
                        isHead = true;
                    } else {
                        return;
                    }
                } else if (qName.equalsIgnoreCase(ParseStage.Body.getElementTag())) {
                    if (!isBody) {
                        setStage(Body);
                        currentSection = parsedFile.createSection(null);
                        parsedFile.setSection(currentSection);
                        isSectionTitle = false;
                        isBody = true;
                    } else {
                        String name = attributes.getValue(TAG_ELEMENT_NAME);
                        if (name != null) {
                            if (!isNotes && Notes.getElementName().equalsIgnoreCase(name)) {
                                setStage(Notes);
                                isNotes = true;
                            } else if (!isComments && Comments.getElementName().equalsIgnoreCase(name)) {
                                setStage(Comments);
                                isComments = true;
                            }
                        } else {
                            return;
                        }
                    }
                } else if (qName.equalsIgnoreCase(ParseStage.Binary.getElementTag())) {
                    if (!isCoverExists) {
                        return;
                    }
                    if (!isCover) {
                        String name = attributes.getValue(TAG_ELEMENT_ID);
                        if (coverName.equalsIgnoreCase(name)) {
                            setStage(Binary);
                            coverType = attributes.getValue(TAG_IMAGE_CONTENT_TYPE);
                            isCover = true;
                        }
                    } else {
                        return;
                    }
                }
                break;
            }
            case Head: {
                currentElement = ParsedFiles.addElement(currentElement, qName);
                proceedAttributes(currentElement, attributes);
                isCoverExists = isCoverExists || checkIsCover(currentElement);
                break;
            }
            case Body: {
                if (!isSectionTitle &&
                        qName.equalsIgnoreCase(TAG_SECTION_TITLE) &&
                        currentSection.getTitle().getTitles().size() == 0) {
                    isSectionTitle = true;
                } else if (isSectionTitle) {
                    if (currentSection.getTitle().getTitles().size() == 0 || isTitleNewLine(qName)) {
                        currentSection.getTitle().addTitle("");
                    }

                } else if (!isSectionTitle && qName.equalsIgnoreCase(TAG_SECTION)) {
                    currentSection = parsedFile.createSection(currentSection);
                }
                break;
            }
            case Notes: {
                if (qName.equalsIgnoreCase(TAG_SECTION)) {
                    notesCount++;
                }
                break;
            }
            case Comments: {
                if (qName.equalsIgnoreCase(TAG_SECTION)) {
                    commentsCount++;
                }
                break;
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentTag = null;
        switch (stage) {
            case Started:
                if (qName.equalsIgnoreCase(Started.getElementTag())) {
                    setStage(None);
                }
                break;
            case None:
                break;
            case Head:
                if (qName.equalsIgnoreCase(ParseStage.Head.getElementTag())) {
                    setStage(Started);
                } else {
                    currentElement = currentElement.getParent();
                }
                break;
            case Body:
                if (qName.equalsIgnoreCase(ParseStage.Body.getElementTag())) {
                    setStage(Started);
                } else if (qName.equalsIgnoreCase(TAG_SECTION_TITLE)) {
                    isSectionTitle = false;
                } else if (qName.equalsIgnoreCase(TAG_SECTION)) {
                    currentSection = currentSection.getParent();
                }
                break;
            case Notes:
                if (qName.equalsIgnoreCase(ParseStage.Notes.getElementTag())) {
                    setStage(Started);
                    parsedFile.setNotesCount(notesCount);
                }
                break;
            case Comments:
                if (qName.equalsIgnoreCase(ParseStage.Comments.getElementTag())) {
                    setStage(Started);
                    parsedFile.setCommentsCount(commentsCount);
                }
                break;
            case Binary:
                setStage(Started);
                parsedFile.setCoverInfo(coverName, coverType, Base64.getMimeDecoder().decode(cover.toString()));
                break;
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (stage) {
            case Head:
                currentElement.setValue(String.valueOf(Arrays.copyOfRange(ch, start, start + length)).trim());
                break;
            case Body:
                if (isSectionTitle && currentTag != null && !currentTag.equalsIgnoreCase(TAG_SECTION_TITLE)) {
                    String value = String.valueOf(Arrays.copyOfRange(ch, start, start + length));
                    if (value.length() > 0) {
                        currentSection.setTitle(value);
                    }
                }
                break;
            case Binary:
                cover.append(String.valueOf(Arrays.copyOfRange(ch, start, start + length)));
        }
        super.characters(ch, start, length);
    }

    private boolean isTitleNewLine(String tag) {
        return newLineTitles.contains(tag.toLowerCase());
    }

    private void extractNameSpace(Attributes attributes) {
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String[] splittedKey = attributes.getQName(i).split(":");
                if (splittedKey.length == 2) {
                    nameSpace = splittedKey[1];
                    break;
                }
            }
        }
    }

    boolean checkIsCover(ParsedFile.Element currentElement) {
        if (currentElement.getName().equalsIgnoreCase(TAG_IMAGE)) {
            String temp = currentElement.getAttribute(TAG_IMAGE_LINK);
            if (temp != null && temp.length() > 0) {
                if (temp.substring(0, 1).equals("#")) {
                    temp = temp.substring(1);
                }
                coverName = temp;
                return true;
            }
        }
        return false;
    }

    void proceedAttributes(ParsedFile.Element element, Attributes attributes) {
        if (attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                ParsedFiles.addAttribute(element, normalizeAttributeName(attributes.getQName(i)), attributes.getValue(i));
            }
        }
    }

    String normalizeAttributeName(String qName) {
        if (nameSpace != null) {
            int i = qName.indexOf(nameSpace + ":");
            if (i == 0) {
                return qName.substring(i + nameSpace.length() + 1);
            }
        }
        return qName;
    }

    public void setStage(ParseStage stage) {
        this.stage = stage;
    }

    public String getCoverName() {
        return coverName;
    }

}

enum ParseStage {
    None("", ""), Started("FictionBook", ""), Head("description", ""), Body("body", ""),
    Notes("body", "notes"), Comments("body", "comments"), Binary("binary", "");

    private final String elementTag;
    private final String elementName;

    public String getElementTag() {
        return elementTag;
    }

    public String getElementName() {
        return elementName;
    }

    ParseStage(String elementTag, String elementName) {
        this.elementTag = elementTag;
        this.elementName = elementName;
    }
}
