/*
 * Copyright (c) 2023-2025 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.oscwii.repositorymanager.config;

import freemarker.template.TemplateException;
import jakarta.servlet.Filter;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.oscwii.repositorymanager.config.repoman.ShopConfig;
import org.oscwii.repositorymanager.controllers.shop.ShopControllerAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.io.IOException;
import java.time.Duration;

import static freemarker.template.Configuration.VERSION_2_3_32;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer
{
    private final ShopConfig shopConfig;

    @Autowired
    public WebConfig(ShopConfig shopConfig)
    {
        this.shopConfig = shopConfig;
    }

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
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new ShopControllerAuth(shopConfig)).addPathPatterns("/shop/**");
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
