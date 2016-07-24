package org.library.common.utils;

import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;
import org.xml.sax.helpers.DefaultHandler;

public abstract class FileParseHandler extends DefaultHandler {

    public static DefaultHandler createHandler(FileType fileType, ParsedFile parsedFile) {
        switch (fileType) {
            case FB2: return new Fb2ParseHandler(parsedFile);
            default: throw new IllegalArgumentException("Given File Type is not supported: " + fileType);
        }
    }

}
