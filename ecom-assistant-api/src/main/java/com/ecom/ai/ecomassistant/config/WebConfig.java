package com.ecom.ai.ecomassistant.config;

import com.ecom.ai.ecomassistant.common.resource.Permission;
import com.ecom.ai.ecomassistant.auth.resolver.CurrentUserIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.format.FormatterRegistry;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet servlet = new DispatcherServlet();
        servlet.setPublishEvents(false); // 不觸發 request handled event
        return servlet;
    }

}
