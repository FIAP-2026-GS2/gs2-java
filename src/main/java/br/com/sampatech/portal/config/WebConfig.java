package br.com.sampatech.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfig {

    public static final int MAX_PAGE_SIZE = 50;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> resolver.setMaxPageSize(MAX_PAGE_SIZE);
    }
}
