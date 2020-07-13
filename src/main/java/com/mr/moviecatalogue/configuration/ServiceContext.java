package com.mr.moviecatalogue.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class ServiceContext extends WebMvcConfigurationSupport {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }
}
