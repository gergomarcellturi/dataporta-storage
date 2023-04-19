package com.dataportal.datastorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // replace '*' with your frontend origin to restrict access
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS") // allowed HTTP methods
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Disposition") // expose the Content-Disposition header
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
