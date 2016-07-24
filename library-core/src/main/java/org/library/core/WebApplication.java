package org.library.core;

import org.library.common.services.FileService;
import org.library.common.services.FileServiceImpl;
import org.library.common.services.SemaphoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@EnableAutoConfiguration
public class WebApplication {
    private SemaphoreService semaphoreService;
    private static String origins;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    public FileService fileService() {
        return new FileServiceImpl();
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // TODO: add specific host/port
                registry.addMapping("/**");
            }
        };
    }

    @Bean
    public synchronized SemaphoreService getSemaphoreService(@Value("${threads.global.count}") int threadsCount,
                                                             @Value("${threads.files.count}") int threadsFilesCount) {
        if (semaphoreService == null) {
            semaphoreService = new SemaphoreService(threadsCount, threadsFilesCount);
        }
        return semaphoreService;
    }
}
