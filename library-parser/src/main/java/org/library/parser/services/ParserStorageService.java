package org.library.parser.services;

import org.library.common.entities.ParsedFile;

public interface ParserStorageService {

    String registerLibrary(String path);
    void saveParsedFile(String library, ParsedFile parsedFile);
    void clearLibrary(String library);

    void initLibrary();
}
