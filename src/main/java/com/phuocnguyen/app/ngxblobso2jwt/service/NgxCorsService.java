package com.phuocnguyen.app.ngxblobso2jwt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngxsivaos.model.properties.CorsProperties;
import com.ngxsivaos.model.request.StatelessCookieRequest;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface NgxCorsService {

    StatelessCookieRequest getStatelessBy(FilterConfig config);

    void setCurrentConfig(FilterConfig config);

    void filterBy(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain);

    ObjectMapper getMapper(JacksonProperties jacksonProperties, CorsProperties corsProperties);

    CorsConfigurationSource createCors(JacksonProperties jacksonProperties, CorsProperties corsProperties);
}
