package org.library.parser.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileType;
import org.library.common.entities.Library;
import org.library.common.entities.ParsedFile;
import org.library.common.entities.ParsedFilesStatus;
import org.library.common.services.FileService;
import org.library.common.services.ParseFileService;
import org.library.common.services.SemaphoreService;
import org.library.common.utils.FileInfoHelper;
import org.library.parser.services.ParserStorageService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Library library = registerLibrary(path);
        List<Path> files = getFilesList();
        ParsedFilesStatus status = new ParsedFilesStatus(files.size());
        proceedFiles(library, status, files);
        return result;
    }

    private void proceedFiles(Library library, ParsedFilesStatus status, List<Path> files) {
        LOGGER.info("Starting processing files: " + files.size());
        ExecutorService serviceI1 = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        ExecutorService serviceI2 = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        ExecutorService serviceO = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        ExecutorService serviceInMemory = Executors.newFixedThreadPool(semaphoreService.getMaxAccessThreadsCount());
        List<CompletableFuture<ParsedFile>> futures =
                files.parallelStream()
                        .map(p -> parseFileService.pathToParsedFile(path, p))
                        .map(pf -> CompletableFuture.supplyAsync(() -> updateFileInfo(library, pf, status), serviceI1))
                        .map(fi -> fi.thenApplyAsync((pf) -> parseFile(library, pf, status), serviceI2))
                        .map(fi -> fi.thenApplyAsync((pf) -> parseInfo(library, pf, status), serviceInMemory))
                        .map(fs -> fs.thenApplyAsync((pf) -> saveParsedFiled(library, pf, status), serviceO))
                        .collect(Collectors.toList());
        List<ParsedFile> parsedFiles = allDone(futures).join();
        serviceI1.shutdown();
        serviceI2.shutdown();
        serviceO.shutdown();
        serviceInMemory.shutdown();
        try {
            serviceI1.awaitTermination(1, TimeUnit.SECONDS);
            serviceI2.awaitTermination(1, TimeUnit.SECONDS);
            serviceO.awaitTermination(1, TimeUnit.SECONDS);
            serviceInMemory.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("serviceIO awaitTermination error");
        }
        LOGGER.info("Files processed: " + parsedFiles.size());
        LOGGER.info("Erroneous: " + parsedFiles.stream().filter(p -> p.getException() != null).count());
    }

    private ParsedFile saveParsedFiled(Library library, ParsedFile parsedFile, ParsedFilesStatus status) {
        try {
            parserStorageService.saveParsedFile(library, parsedFile);
            status.getSavedCount().increment();
            displayInfo(status);
        } catch (Exception e) {
            parsedFile.addException(e);
        }
        return parsedFile;
    }

    private ParsedFile parseFile(final Library library, ParsedFile parsedFile, ParsedFilesStatus status) {
        semaphoreService.acquireFilesAccess();
        try {
            parseFileService.parseFile(Paths.get(library.getPath()), parsedFile);
            status.getXmlParsedCount().increment();
            displayInfo(status);
        } catch (Exception ex) {
            parsedFile.addException(ex);
        } finally {
            semaphoreService.releaseFilesAccess();
        }
        return parsedFile;
    }

    private ParsedFile parseInfo(Library library, ParsedFile parsedFile, ParsedFilesStatus status) {
        try {
            parseFileService.parseInfo(parsedFile);

            status.getInfoParsedCount().increment();
            displayInfo(status);
        } catch (Exception e) {
            parsedFile.addException(e);
        }
        return parsedFile;
    }

    public ParsedFile updateFileInfo(final Library library, final ParsedFile parsedFile, ParsedFilesStatus status) {
        semaphoreService.acquireFilesAccess();
        try {
            FileInfoHelper.updateFileInfo(Paths.get(library.getPath()), parsedFile.getFileInfo(), calcMD5hash);
            status.getFileInfoUpdatedCount().increment();
            displayInfo(status);
        } catch (Exception e) {
            LOGGER.error("Cannot update file info " + parsedFile.getFileInfo(), e);
        } finally {
            semaphoreService.releaseFilesAccess();
        }
        return parsedFile;
    }

    private Library registerLibrary(Path path) {
        LOGGER.debug("Registering library " + path);
        parserStorageService.initLibrary(path.toString());
        Library library = parserStorageService.registerLibrary(path.toString());
        LOGGER.debug("Registered library " + library);
        return library;
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

    private void displayInfo(ParsedFilesStatus status) {
        LOGGER.debug(status);
    }


    @Override
    public String toString() {
        return "ParserImpl{" +
                "path=" + path +
                '}';
    }
}
