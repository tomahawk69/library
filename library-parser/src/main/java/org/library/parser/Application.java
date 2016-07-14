package org.library.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FileService fileService() {
        return new FileServiceImpl();
    }

    @Autowired
    private ParserFactory parserFactory;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("===Library parserImpl===");
        List<String> paths = applicationArguments.getNonOptionArgs();
        LOGGER.debug("Simple arguments: " + paths);
        LOGGER.debug("Named arguments: " + applicationArguments.getOptionNames());
        if (paths.size() == 0) {
            System.out.println("Invalid arguments");
            System.out.println("Usage: <app> <path_to_library>");
            System.exit(1);
        }
        LOGGER.info("Paths: " + paths);
        runParsers(paths);

    }

    private boolean runParsers(List<String> paths) {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Boolean>> futures = new LinkedList<>();
        for (String stringPath: paths) {
            Path path = stringToExistingDirectoryPath(stringPath);
            if (path != null) {
                futures.add(executor.submit(parserFactory.createParser(path)));
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
                LOGGER.error("Interruped: ", e);
            } catch (ExecutionException e) {
                LOGGER.error("Exception: ", e);
            }
        }
        return result;
    }

    private Path stringToExistingDirectoryPath(String stringPath) {
        Path path = Paths.get(stringPath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            return path;
        } else {
            LOGGER.error("Not exists or not a directory: " + path);
            return null;
        }
    }
}
