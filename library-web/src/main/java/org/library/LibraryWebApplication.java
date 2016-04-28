package org.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class LibraryWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryWebApplication.class, args);
    }
}
