package com.phuocnguyen.app.ngxblobso2jwt.service;

import com.ngxsivaos.model.properties.RSSProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

public interface NgxAuthRssService {

    void onConfigHttpSecurities(RSSProperties rssProperties, HttpSecurity http) throws Exception;

    void onConfigResourceServerSecurities(
            RSSProperties rssProperties,
            ResourceServerSecurityConfigurer resources,
            ResourceServerTokenServices tokenService,
            AuthenticationManager authenticationManagerBean);
}
