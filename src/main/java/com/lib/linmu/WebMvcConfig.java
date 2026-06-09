package com.lib.linmu;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve cover images
        registry.addResourceHandler("/uploads/covers/**")
                .addResourceLocations("file:uploads/covers/");

        // PDFs are NOT served as static files — they go through the controller
        // so you can control access (auth checks, logging, etc.)
    }

}
