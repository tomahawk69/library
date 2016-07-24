package org.library.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.library.common.entities.FileType;
import org.library.common.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.library.common.services.*;

@SpringBootApplication
public class Application implements ApplicationRunner {
    private static Logger LOGGER = LogManager.getLogger(Application.class);
    private SemaphoreService semaphoreService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FileService fileService() {
        return new FileServiceImpl();
    }

    @Bean
    public ParseFileService parseFileService() {
        return new ParseFileService();
    }

    @Bean
    public synchronized SemaphoreService getSemaphoreService(@Value("${threads.global.count}") int threadsCount,
                                                             @Value("${threads.files.count}") int threadsFilesCount) {
        if (semaphoreService == null) {
            semaphoreService = new SemaphoreService(threadsCount, threadsFilesCount);
        }
        return semaphoreService;
    }

    @Autowired
    private ParserFactory parserFactory;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("===Library parserImpl===");
        System.out.print("Available extensions: ");
        FileType.getExtensions().stream().forEach(ex -> System.out.print(ex.toUpperCase().substring(1) + " "));
        System.out.println("");
        List<String> paths = applicationArguments.getNonOptionArgs();
        if (paths.size() == 0) {
            System.out.println("Invalid arguments");
            System.out.println("Usage: <app> <path_to_library>");
            System.exit(1);
        }
        LOGGER.info("Paths: " + paths);
        runParsers(paths);
    }

    private boolean runParsers(List<String> paths) {
        // get list of files
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Boolean>> futures = new LinkedList<>();
        for (String stringPath : paths) {
            Path path = FileUtils.stringToExistingDirectoryPath(stringPath);
            if (path != null) {
                futures.add(executor.submit(parserFactory.createParser(path)));
            } else {
                LOGGER.error("Path isn't a folder or doesn't exists: " + stringPath);
            }
        }
        executor.shutdown();
        boolean result = true;
        for (Future<Boolean> future : futures) {
            try {
                if (!future.get()) {
                    result = false;
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted: ", e);
            } catch (ExecutionException e) {
                LOGGER.error("Exception: ", e);
            }
        }
        return result;
    }

}
