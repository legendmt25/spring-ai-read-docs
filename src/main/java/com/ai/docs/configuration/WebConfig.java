package com.ai.docs.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve JS files from classpath:/public/js/**
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/public/js/")
                .setCachePeriod(3600);

        // Serve CSS files from classpath:/public/css/**
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/public/css/")
                .setCachePeriod(3600);

        // Serve HTML files located directly under classpath:/public (e.g. /public/*.html)
        registry.addResourceHandler("/*.html")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);

        // Fallback: serve any other static resources from standard locations
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/resources/", "classpath:/META-INF/resources/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward root to index.html in /public
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

