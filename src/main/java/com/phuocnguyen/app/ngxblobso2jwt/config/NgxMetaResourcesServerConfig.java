package com.phuocnguyen.app.ngxblobso2jwt.config;

import com.ngxsivaos.config.EventListenerConfig;
import com.ngxsivaos.model.properties.RSSProperties;
import com.ngxsivaos.model.properties.RSSReCallbackProperties;
import com.phuocnguyen.app.ngxblobso2jwt.model.indicator.ReCallbackResourceServerIndicator;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@SuppressWarnings({"FieldCanBeLocal", "All"})
@Configuration
@EnableResourceServer
public class NgxMetaResourcesServerConfig extends ResourceServerConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NgxMetaResourcesServerConfig.class);

    private final NgxAuthRssService ngxAuthRssService;
    private final RSSProperties rssProperties;
    private final RSSReCallbackProperties rssReCallbackProperties;

    @Autowired
    public NgxMetaResourcesServerConfig(
            NgxAuthRssService ngxAuthRssService,
            RSSProperties rssProperties,
            RSSReCallbackProperties rssReCallbackProperties
    ) {
        this.ngxAuthRssService = ngxAuthRssService;
        this.rssProperties = rssProperties;
        this.rssReCallbackProperties = rssReCallbackProperties;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() {
        OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager();
        authenticationManager.setTokenServices(rssReCallbackProperties.isEnabled() ? reCallBackTokenService() : tokenService());
        return authenticationManager;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        ngxAuthRssService.onConfigResourceServerSecurities(
                rssProperties,
                resources,
                rssReCallbackProperties.isEnabled() ? reCallBackTokenService() : tokenService(),
                authenticationManagerBean());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ngxAuthRssService.onConfigHttpSecurities(rssProperties, http);
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        return EventListenerConfig.accessTokenConverter();
    }

    @Bean
    public TokenStore createTokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    @ConditionalOnProperty(
            value = "spring.resource-server-callback-starter.enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public DefaultTokenServices tokenService() {

        if (logger.isInfoEnabled()) {
            logger.info(
                    "NgxMetaResourcesServerConfig::tokenService()::rssReCallbackProperties.isEnabled() = {}",
                    rssReCallbackProperties.isEnabled());
        }

        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(createTokenStore());
        return defaultTokenServices;
    }

    @Bean
    @ConditionalOnProperty(
            value = "spring.resource-server-callback-starter.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public ResourceServerTokenServices reCallBackTokenService() {

        if (logger.isInfoEnabled()) {
            logger.info(
                    "NgxMetaResourcesServerConfig::reCallBackTokenService()::rssReCallbackProperties.isEnabled() = {}",
                    rssReCallbackProperties.isEnabled());
        }

        return new ReCallbackResourceServerIndicator();
    }
}
