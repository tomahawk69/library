package org.library.parser.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;
import org.library.common.services.FileService;
import org.library.common.services.ParseFileService;
import org.library.common.services.SemaphoreService;
import org.library.common.utils.FileInfoHelper;
import org.library.parser.services.ParserStorageService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParserImpl implements Parser {
    private static final Logger LOGGER = LogManager.getLogger(ParserImpl.class);

    private final Path path;
    private final FileService fileService;
    private final ParseFileService parseFileService;
    private final SemaphoreService semaphoreService;
    private final ParserStorageService parserStorageService;
    private List<String> allowedExtensions;
    private boolean calcMD5hash;
    private String databaseId;

    ParserImpl(FileService fileService, ParseFileService parseFileService, SemaphoreService semaphoreService, ParserStorageService parserStorageService, Path path) {
        this.path = path;
        this.fileService = fileService;
        this.parseFileService = parseFileService;
        this.semaphoreService = semaphoreService;
        this.parserStorageService = parserStorageService;
    }

    @Override
    public Boolean call() throws Exception {
        Boolean result = true;
        registerLibrary(path);
        ExecutorService serviceIO = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        List<CompletableFuture<ParsedFile>> futures =
                getFilesList().parallelStream()
                        .map(p ->  new FileInfo(p.toString()))
                        .map(fi -> parseFileService.fileInfoToParsedFile(path, fi))
                        .map(pf -> CompletableFuture.supplyAsync(() -> updateFileInfo(pf), serviceIO))
                        .map(fi -> fi.thenApplyAsync(this::parseXML, serviceIO))
                        .collect(Collectors.toList());
        List<ParsedFile> parsedFiles = allDone(futures).join();
        serviceIO.shutdown();
        serviceIO.awaitTermination(1, TimeUnit.SECONDS);
        LOGGER.info("Parsed files: " + parsedFiles.size());
        LOGGER.info("Erroneous: " + parsedFiles.stream().filter(p -> p.getException() != null).count());
        return result;
    }

    private void registerLibrary(Path path) {
        LOGGER.debug("Registering library " + path);
        parserStorageService.initLibrary();
        databaseId = parserStorageService.registerLibrary(path.toString());
        LOGGER.debug("Registered library " + databaseId);

    }

    private <ParsedFile> CompletableFuture<List<ParsedFile>> allDone(List<CompletableFuture<ParsedFile>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream()
                        .map(f -> f.join())
                        .collect(Collectors.toList()));
    }

    private List<Path> getFilesList() throws IOException {
        LOGGER.info("getFilesList started " + this);
        List<Path> files = fileService.getFilesList(filterAllowExtensions(), true, path);
        LOGGER.info("getFilesList ended " + this);
        LOGGER.info("Loaded " + files.size() + " files");
        return files;
    }

    private List<String> filterAllowExtensions() {
        return FileType.getExtensions().stream()
                .filter(ex -> allowedExtensions.size() == 0 || allowedExtensions.contains(ex.toUpperCase().substring(1)))
                .collect(Collectors.toList());
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public void setCalcMD5hash(boolean calcMD5hash) {
        this.calcMD5hash = calcMD5hash;
    }

    public ParsedFile updateFileInfo(final ParsedFile parsedFile) {
        semaphoreService.acquireFilesAccess();
        try {
            FileInfoHelper.updateFileInfo(path, parsedFile.getFileInfo(), calcMD5hash);
        } catch (Exception e) {
            LOGGER.error("Cannot update file info " + parsedFile.getFileInfo(), e);
        } finally {
            semaphoreService.releaseFilesAccess();
        }
        return parsedFile;
    }


    private ParsedFile parseXML(ParsedFile parsedFile) {
        semaphoreService.acquireFilesAccess();
        try {
            parseFileService.parseFile(parsedFile);
        } catch (Exception ex) {
            parsedFile.addException(ex);
        } finally {
            semaphoreService.releaseFilesAccess();
        }
        return parsedFile;
    }

    @Override
    public String toString() {
        return "ParserImpl{" +
                "path=" + path +
                '}';
    }
}
