package org.oscwii.repositorymanager.config;

import freemarker.template.TemplateException;
import jakarta.servlet.Filter;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.io.IOException;

import static freemarker.template.Configuration.VERSION_2_3_32;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer
{
    @Bean
    public FreeMarkerConfigurer freemarkerConfig() throws TemplateException, IOException
    {
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:templates/");

        Java8ObjectWrapper objectWrapper = new Java8ObjectWrapper(VERSION_2_3_32);
        objectWrapper.setExposeFields(true);

        freeMarkerConfigurer.afterPropertiesSet();
        freeMarkerConfigurer.getConfiguration().setObjectWrapper(objectWrapper);
        freeMarkerConfigurer.getConfiguration().setSharedVariable("statics", objectWrapper.getStaticModels());
        return freeMarkerConfigurer;
    }

    @Bean
    public FreeMarkerViewResolver freeMarkerViewResolver()
    {
        FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
        viewResolver.setCache(true);
        viewResolver.setSuffix(".ftl");
        viewResolver.setContentType("text/html; charset=UTF-8");
        return viewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public FilterRegistrationBean<?> registerFilters()
    {
        // Required to compute Content-Length for API responses
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ShallowEtagHeaderFilter());
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
