package com.ecom.ai.ecomassistant.config;

import com.ecom.ai.ecomassistant.db.model.Permission;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.format.FormatterRegistry;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, Permission>() {
            @Override
            public Permission convert(String source) {
                try {
                    return Permission.valueOf(source.trim().toUpperCase());
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid permission value: " + source);
                }
            }
        });
    }
}
