package org.library.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileInfo;
import org.library.common.entities.FileType;
import org.library.common.entities.ParsedFile;
import org.library.common.services.FileService;
import org.library.common.services.ParseFileService;
import org.library.common.services.SemaphoreService;
import org.library.common.utils.FileInfoHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParserImpl implements Parser {
    private static final Logger LOGGER = LogManager.getLogger(ParserImpl.class);

    private final Path path;
    private final FileService fileService;
    private final ParseFileService parseFileService;
    private final SemaphoreService semaphoreService;
    private List<String> allowedExtensions;
    private boolean calcMD5hash;

    ParserImpl(FileService fileService, ParseFileService parseFileService, SemaphoreService semaphoreService, Path path) {
        this.path = path;
        this.fileService = fileService;
        this.parseFileService = parseFileService;
        this.semaphoreService = semaphoreService;
    }

    @Override
    public Boolean call() throws Exception {
        Boolean result = true;
        List<Path> files = getFilesList();
        List<FileInfo> fileInfos = createFileInfos(files);
        List<ParsedFile> parsedFiles = parseFiles(fileInfos);
        return result;
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

    private List<FileInfo> createFileInfos(List<Path> files) {
        LOGGER.info("createFileInfos started " + this);
        ExecutorService service = Executors.newFixedThreadPool(semaphoreService.getMaxFilesThreadsCount());
        List<CompletableFuture<FileInfo>> result =
            files.stream().map(path ->
                    CompletableFuture.supplyAsync(() -> updateFileInfo(path), service))
                    .collect(Collectors.toList());
        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Terminated", e);
            }
        }
        LOGGER.info("createFileInfos ended " + this);
        LOGGER.info("Updated " + result.size() + " files");
        return result.stream().filter(CompletableFuture::isDone).map(f -> f.getNow(null)).filter(i -> i != null).collect(Collectors.toList());
    }

    public FileInfo updateFileInfo(final Path path) {
        FileInfo fileInfo = new FileInfo(path.toString());
        semaphoreService.acquireFilesAccess();
        try {
            FileInfoHelper.updateFileInfo(path, fileInfo, calcMD5hash);
        } catch (Exception e) {
            LOGGER.error("Cannot update file info " + fileInfo, e);
        } finally {
            semaphoreService.releaseFilesAccess();
        }
        return fileInfo;
    }


    private List<ParsedFile> parseFiles(List<FileInfo> fileInfos) {
        LOGGER.info("parseFiles started");
        List<Future<ParsedFile>> futures = addFilesToParser(fileInfos);
        List<ParsedFile> result = futuresToParsedFiles(futures);
        LOGGER.info("parseFiles ended");
        LOGGER.info("Parsed " + result.size() + " files");
        return result;
    }

    private List<ParsedFile> futuresToParsedFiles(List<Future<ParsedFile>> futures) {
        List<ParsedFile> result = new ArrayList<>();
        futures.stream().filter(Future::isDone).forEach(future -> {
            try {
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Future get error", e);
            }
        });
        return result;
    }

    private List<Future<ParsedFile>> addFilesToParser(List<FileInfo> files) {
        LOGGER.info("addFilesToParser started");
        ExecutorService executorService = Executors.newFixedThreadPool(semaphoreService.getMaxAccessThreadsCount());
        List<Future<ParsedFile>> futures = new ArrayList<>();
        files.stream().forEach(
                i -> futures.add(executorService.submit(() -> {
                            ParsedFile parsedFile = parseFileService.fileInfoToParsedFile(path.resolve(i.getPath()), i);
                            semaphoreService.acquireFilesAccess();
                            try {
                                parseFileService.parseXml(parsedFile);
                            } finally {
                                semaphoreService.releaseFilesAccess();
                            }
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
        LOGGER.info("addFilesToParser ended");
        return futures;
    }

    @Override
    public String toString() {
        return "ParserImpl{" +
                "path=" + path +
                '}';
    }

}
