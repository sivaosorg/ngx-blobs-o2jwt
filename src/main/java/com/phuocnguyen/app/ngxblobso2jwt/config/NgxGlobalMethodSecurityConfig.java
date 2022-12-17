package com.phuocnguyen.app.ngxblobso2jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngxsivaos.model.properties.CorsProperties;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxCorsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


@SuppressWarnings({"All"})
@Configuration
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class NgxGlobalMethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NgxGlobalMethodSecurityConfig.class);

    private final JacksonProperties jacksonProperties;
    private final CorsProperties corsProperties;

    private final NgxCorsService ngxCorsService;

    @Autowired
    public NgxGlobalMethodSecurityConfig(
            JacksonProperties jacksonProperties,
            CorsProperties corsProperties,
            NgxCorsService ngxCorsService) {
        this.jacksonProperties = jacksonProperties;
        this.corsProperties = corsProperties;
        this.ngxCorsService = ngxCorsService;
    }

    @Bean
    public ObjectMapper createMapperGlobally() {
        return ngxCorsService.getMapper(jacksonProperties, corsProperties);
    }

    @Bean
    @ConditionalOnProperty(
            value = "spring.cors-starter.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    CorsConfigurationSource corsConfigs() {
        return ngxCorsService.createCors(jacksonProperties, corsProperties);
    }

}
