package org.library.parser.services;

import org.library.common.entities.Library;
import org.library.common.entities.ParsedFile;

public interface ParserStorageService {

    Library registerLibrary(String path);
    void saveParsedFile(Library library, ParsedFile parsedFile);
    void clearLibrary(Library library);

    void initLibrary(String path);
}
