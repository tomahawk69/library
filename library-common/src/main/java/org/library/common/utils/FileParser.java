package org.library.common.utils;

import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;

public interface FileParser {

    static FileParser createHandler(FileType fileType) {
        switch (fileType) {
            case FB2: return new Fb2Parser();
            default: throw new IllegalArgumentException("Given File Type is not supported: " + fileType);
        }
    }

    boolean parse(ParsedFile parsedFile);

}
