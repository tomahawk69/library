package org.library.parser.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.Library;
import org.library.common.entities.ParsedFile;
import org.library.parser.repositories.ParsedFileRepository;
import org.library.parser.repositories.LibraryRepository;
import org.library.parser.repositories.ParsedFileTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ParserStorageServiceMongoDB implements ParserStorageService {
    private static final Logger LOGGER = LogManager.getLogger(ParserStorageServiceMongoDB.class);
    private final LibraryRepository libraryRepository;
    private final ParsedFileTemplate parsedFileTemplate;
    private final ParsedFileRepository parsedFileRepository;

    private String mode;

    @Autowired
    public void setMode(@Value("${database.mode}") String mode) {
        this.mode = mode;
    }

    @Autowired
    public ParserStorageServiceMongoDB(LibraryRepository libraryRepository,
                                       ParsedFileRepository parsedFileRepository,
                                       ParsedFileTemplate parsedFileTemplate) {
        this.libraryRepository = libraryRepository;
        this.parsedFileTemplate = parsedFileTemplate;
        this.parsedFileRepository = parsedFileRepository;
    }

    @Override
    public Library registerLibrary(String path) {
        Library library = libraryRepository.findByPath(path);
        if (library == null) {
            LOGGER.info("Registering new library: " + path);
            library = new Library();
            library.setPath(path);
            library.setUpdated(LocalDateTime.now());
            libraryRepository.save(library);
            LOGGER.info("Library is registered: " + library.getId());
        }
        return library;
    }

    @Override
    public void saveParsedFile(Library library, ParsedFile parsedFile) {
        parsedFileTemplate.save(parsedFile, getParsedFileCollectionName(library));
    }

    public String getParsedFileCollectionName(Library library) {
        return "files_" + library.getId();
    }

    @Override
    public void clearLibrary(Library library) {
        LOGGER.info("!!! clearing library");
        parsedFileTemplate.dropCollection(getParsedFileCollectionName(library));
        libraryRepository.delete(library);
    }

    @Override
    public void initLibrary(String path) {
        LOGGER.debug("init database");
        if ("recreate".equalsIgnoreCase(mode)) {
            Library library = libraryRepository.findByPath(path);
            if (library != null) {
                clearLibrary(library);
            }
        }
    }
}
