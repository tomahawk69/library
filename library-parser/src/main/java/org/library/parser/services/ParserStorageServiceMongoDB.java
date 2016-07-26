package org.library.parser.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.Library;
import org.library.common.entities.ParsedFile;
import org.library.parser.repositories.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ParserStorageServiceMongoDB implements ParserStorageService {
    private static final Logger LOGGER = LogManager.getLogger(ParserStorageServiceMongoDB.class);
    private final LibraryRepository libraryRepository;

    private String mode;

    @Autowired
    public void setMode(@Value("${database.mode}") String mode) {
        this.mode = mode;
    }

    @Autowired
    public ParserStorageServiceMongoDB(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    @Override
    public String registerLibrary(String path) {
        Library library = libraryRepository.findByPath(path);
        if (library == null) {
            LOGGER.info("Registering new database: " + path);
            library = new Library(UUID.randomUUID().toString());
            library.setPath(path);
            library.setUpdated(LocalDateTime.now());
            libraryRepository.save(library);
        }
        return library.getId();
    }

    @Override
    public void saveParsedFile(String library, ParsedFile parsedFile) {

    }

    @Override
    public void clearLibrary(String library) {

    }

    @Override
    public void initLibrary() {
        LOGGER.debug("init database");
        if ("recreate".equalsIgnoreCase(mode)) {
            LOGGER.info("!!! recreating database");
            libraryRepository.deleteAll();
        }
    }
}
