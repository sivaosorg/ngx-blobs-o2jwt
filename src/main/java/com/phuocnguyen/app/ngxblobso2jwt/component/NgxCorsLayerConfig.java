package com.phuocnguyen.app.ngxblobso2jwt.component;

import com.phuocnguyen.app.ngxblobso2jwt.service.NgxCorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;

@SuppressWarnings({
        "SpringJavaAutowiredFieldsWarningInspection"
})
@Component
public class NgxCorsLayerConfig implements Filter {

    @Autowired
    private NgxCorsService ngxCorsService;

    public NgxCorsLayerConfig() {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {
        ngxCorsService.filterBy(servletRequest, servletResponse, chain);
    }

    public void init(FilterConfig config) {
        ngxCorsService.setCurrentConfig(config);
    }
}
