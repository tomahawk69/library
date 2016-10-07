package org.library.common.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.*;
import java.util.stream.Stream;

public class ParsedFile {

    @Id
    private String path;

    private ProcessState state = ProcessState.None;

    private FileInfo fileInfo;
    private BookInfo bookInfo;
    private Element header = Element.Empty;
    private Section section = Section.Empty;
    private Cover cover = new Cover();

    private int notesCount;
    private int commentsCount;

    @Transient
    private Exception exception;

    public ParsedFile() {
    }

    public ParsedFile(String path) {
        this.path = path;
        this.fileInfo = new FileInfo(path);
        this.bookInfo = new BookInfo();
    }

    public ParsedFile(FileInfo fileInfo) {
        this.path = fileInfo.getPath();
        this.fileInfo = fileInfo;
        this.bookInfo = new BookInfo();
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public Element getHeader() {
        return header;
    }

    public Section getSection() {
        return section;
    }

    public Section createSection(Section parent) {
        Section result = new Section();
        if (parent != null) {
            parent.sections.add(result);
            result.parent = parent;
        }
        return result;
    }

    public void setHeader(Element head) {
        this.header = head;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public void setNotesCount(int notesCount) {
        this.notesCount = notesCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getNotesCount() {
        return notesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCoverInfo(String name, String coverType, byte[] bytes) {
        cover.setCoverName(name);
        cover.setCoverType(coverType);
        cover.setBytes(bytes);
    }

    public Cover getCover() {
        return cover;
    }

    public void addException(Exception ex) {
        if (exception == null) {
            exception = new Exception(ex);
        } else {
            exception.addSuppressed(ex);
        }
    }

    public Exception getException() {
        return exception;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public static class Element implements Iterable<Element> {
        protected String name;
        public static Element Empty = new Element();

        @Transient
        private Element parent;

        public Element() {
        }

        public void setParent(Element parent) {
            this.parent = parent;
        }

        public Element getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        @Override
        public Iterator<Element> iterator() {
            return Collections.emptyIterator();
        }

        public Stream<Element> stream() {
            return Stream.empty();
        }
    }

    public static class TextElement extends Element {
        private final String text;

        public TextElement(String text) {
            super();
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "TextElement{" +
                    "text='" + text + '\'' +
                    '}';
        }
    }

    public static class TagElement extends Element {
        private List<Element> elements = new ArrayList<>();
        private Map<String, String> attributes = new HashMap<>();

        public TagElement(String name) {
            super();
            this.name = name;
        }

        public void addElement(Element element) {
            elements.add(element);
        }

        public void addAttribute(String key, String value) {
            attributes.put(key, value);
        }

        public String getAttribute(String key) {
            return attributes.getOrDefault(key, null);
        }

        @Override
        public String toString() {
            return "Element{" +
                    "name='" + name + '\'' +
                    ", elements=" + elements +
                    ", attributes=" + attributes +
                    '}';
        }

        @Override
        public Iterator<Element> iterator() {
            return elements.iterator();
        }

        @Override
        public Stream<Element> stream() {
            return elements.stream();
        }
    }

    /**
     * Title class to fit multi-line title possibility
     */
    public static class Title {
        private List<String> titles = new ArrayList<>();

        public List<String> getTitles() {
            return titles;
        }

        /**
         * Create new item in the titles list
         *
         * @param title item to add
         */
        public void addTitle(String title) {
            titles.add(title);
        }

        /**
         * Append new item in the titles list
         *
         * @param title item to append
         */
        public void setTitle(String title) {
            String old = titles.get(titles.size() - 1);
            titles.set(titles.size() - 1, old + title);
        }

        @Override
        public String toString() {
            return "Title{" +
                    "titles=" + titles +
                    '}';
        }
    }

    /**
     * Uniqueness is not guaranteed because there can be sections with same title on one level
     * ID is not mandatory
     */
    public static class Section {
        public static Section Empty = new Section();

        private final Title title = new Title();
        private List<Section> sections = new LinkedList<>();
        @Transient
        private Section parent;

        Section() {

        }

        @Override
        public String toString() {
            return "Section{" +
                    "title='" + title + '\'' +
                    "parent='" + (parent == null ? null : parent.title) + '\'' +
                    ", sections=" + sections +
                    '}';
        }

        public Title getTitle() {
            return title;
        }

        public void addTitle(String title) {
            this.title.addTitle(title);
        }

        public Section getParent() {
            return parent;
        }

        public void setTitle(String title) {
            this.title.setTitle(title);
        }
    }

    @Override
    public String toString() {
        return "ParsedFile{" +
                "state=" + state +
                ", fileInfo=" + fileInfo +
                ", fileInfo=" + bookInfo +
                ", header=" + header +
                ", section=" + section +
                ", cover=" + cover +
                ", notes=" + notesCount +
                ", comments=" + commentsCount +
                '}';
    }

    public enum ProcessState {
        None, XMLProcessed, BookInfoProcessed
    }

    public class Cover {
        private String coverName;
        private byte[] bytes;
        private String coverType;

        public String getCoverName() {
            return coverName;
        }

        public void setCoverName(String coverName) {
            this.coverName = coverName;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public void setCoverType(String coverType) {
            this.coverType = coverType;
        }

        public String getCoverType() {
            return coverType;
        }

        @Override
        public String toString() {
            return "Cover{" +
                    "coverName='" + coverName + '\'' +
                    "coverType='" + coverType + '\'' +
                    ", bytes length=" + (bytes == null ? -1 : bytes.length) +
                    '}';
        }
    }
}