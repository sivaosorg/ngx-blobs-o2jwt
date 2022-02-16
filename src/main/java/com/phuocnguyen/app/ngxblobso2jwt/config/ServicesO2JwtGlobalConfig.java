package com.phuocnguyen.app.ngxblobso2jwt.config;

import com.ngxsivaos.model.properties.RSSProperties;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import com.phuocnguyen.app.ngxblobso2jwt.service.serviceImpl.NgxAuthRssServiceImpl;
import com.sivaos.Service.SIVAOSAuthenticationService;
import com.sivaos.Service.SIVAOSServiceImplement.SIVAOSAuthenticationServiceImplement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;

@SuppressWarnings("All")
@Configuration
public class ServicesO2JwtGlobalConfig {

    @Bean
    @Primary
    public RSSProperties rssProperties() {
        return new RSSProperties();
    }

    @Bean
    @Resource(name = "sivaOsAuthenticationService")
    public SIVAOSAuthenticationService sivaOsAuthenticationService() {
        return new SIVAOSAuthenticationServiceImplement();
    }

    @Bean
    @Primary
    @Resource(name = "ngxAuthRssService")
    public NgxAuthRssService ngxAuthRssService() {
        return new NgxAuthRssServiceImpl();
    }
}
