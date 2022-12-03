package com.phuocnguyen.app.ngxblobso2jwt.service;

import com.ngxsivaos.model.request.StatelessCookieRequest;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface NgxCorsService {

    StatelessCookieRequest getStatelessBy(FilterConfig config);

    void setCurrentConfig(FilterConfig config);

    void filterBy(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain);
}
