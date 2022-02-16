package com.phuocnguyen.app.ngxblobso2jwt.config;

import com.ngxsivaos.config.EventListenerConfig;
import com.ngxsivaos.model.properties.RSSProperties;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import com.sivaos.Service.SIVAOSAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@SuppressWarnings({"FieldCanBeLocal", "All"})
@Configuration
@EnableResourceServer
public class NgxMetaResourcesServerConfig extends ResourceServerConfigurerAdapter {

    private final SIVAOSAuthenticationService sivaOsAuthenticationService;
    private final NgxAuthRssService ngxAuthRssService;
    private final RSSProperties rssProperties;

    @Autowired
    public NgxMetaResourcesServerConfig(
            SIVAOSAuthenticationService sivaOsAuthenticationService,
            NgxAuthRssService ngxAuthRssService,
            RSSProperties rssProperties
    ) {
        this.sivaOsAuthenticationService = sivaOsAuthenticationService;
        this.ngxAuthRssService = ngxAuthRssService;
        this.rssProperties = rssProperties;
    }


    @Bean
    public AuthenticationManager authenticationManagerBean() {
        OAuth2AuthenticationManager authenticationManager = new OAuth2AuthenticationManager();
        authenticationManager.setTokenServices(tokenService());
        return authenticationManager;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        /*
        sivaOsAuthenticationService.configureResourceServerSecurityConfigurer(
                resources,
                tokenService(),
                authenticationManagerBean());
        */

        ngxAuthRssService.onConfigResourceServerSecurities(
                rssProperties,
                resources,
                tokenService(),
                authenticationManagerBean());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // sivaOsAuthenticationService.configureHttpSecurity(http);
        ngxAuthRssService.onConfigHttpSecurities(rssProperties, http);
    }

    /**
     * @apiNote - take authentication info without make request to get user info
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        return EventListenerConfig.accessTokenConverter();
    }

    /**
     * @apiNote - take authentication info without make request to get user info
     */
    @Bean
    public TokenStore createTokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    /*
     @Bean
    public ResourceServerTokenServices tokenService() {
        return new CustomRemoteTokenServiceConfigure();
    }
    */

    @Bean
    public DefaultTokenServices tokenService() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(createTokenStore());
        return defaultTokenServices;
    }
}
