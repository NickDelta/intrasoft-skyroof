package com.intrasoft.skyroof.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ObjectMapper objectMapperBean(){
        ObjectMapper mapper = new ObjectMapper();

        // Setup some filters to use with @JsonFilter
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();

        filterProvider.addFilter("userShowOnlyUsernameFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept("username","role"));
        filterProvider.addFilter("projectShowOnlyIdAndTitle",
                SimpleBeanPropertyFilter.filterOutAllExcept("id","title"));

        mapper.setFilterProvider(filterProvider);

        // Setup naming strategy
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        return mapper;

    }

}