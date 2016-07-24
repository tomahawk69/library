package org.library.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;
import org.library.common.services.FileService;
import org.library.common.services.ParseFileService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParserImpl implements Parser {
    private static final Logger LOGGER = LogManager.getLogger(ParserImpl.class);

    private final Path path;
    private final FileService fileService;
    private final ParseFileService parseFileService;


    ParserImpl(FileService fileService, ParseFileService parseFileService, Path path) {
        this.path = path;
        this.fileService = fileService;
        this.parseFileService = parseFileService;
    }

    @Override
    public Boolean call() throws Exception {
        Boolean result = true;
        List<Path> files = getFilesList();
        List<ParsedFile> parsedFiles = parseFiles(files);
        return result;
    }

    private List<Path> getFilesList() throws IOException {
        LOGGER.info("getFilesList started " + this);
        List<Path> files = fileService.getFilesList(FileType.getExtensions(), true, path);
        LOGGER.info("getFilesList ended " + this);
        LOGGER.info("Loaded " + files.size() + " files");
        return files;
    }

    private List<ParsedFile> parseFiles(List<Path> files) {
        LOGGER.info("parseFiles started");
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<ParsedFile>> futures = new ArrayList<>();
        files.stream().forEach(
                p -> futures.add(executorService.submit(() -> {
                    ParsedFile parsedFile = parseFileService.fileInfoToParsedFile(p, new FileInfo(p.toString()));
                    parseFileService.parseXml(parsedFile);
                    return parsedFile;
                }
        )));
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Terminated", e);
            }
        }
        List<ParsedFile> result = new ArrayList<>();
        for (Future<ParsedFile> future : futures) {
            if (future.isDone()) {
                try {
                    result.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Future get error", e);
                }
            }
        }
        LOGGER.info("parseFiles ended");
        LOGGER.info("Parsed " + result.size() + " files");
        return result;
    }

    @Override
    public String toString() {
        return "ParserImpl{" +
                "path=" + path +
                '}';
    }
}
