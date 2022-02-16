package com.phuocnguyen.app.ngxblobso2jwt.service.serviceImpl;

import com.ngxsivaos.model.label.EndpointsLabel;
import com.ngxsivaos.model.properties.RSSProperties;
import com.phuocnguyen.app.ngxblobso2jwt.model.builder.NgxTokenExtractor;
import com.phuocnguyen.app.ngxblobso2jwt.model.response.AccessDeniedHandlerResponse;
import com.phuocnguyen.app.ngxblobso2jwt.model.response.AuthEntriesPointResponse;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import com.sivaos.Utility.CollectionsUtility;
import com.sivaos.Utility.StringUtility;
import com.sivaos.Utils.ExchangeUtils;
import com.sivaos.Utils.LoggerUtils;
import com.sivaos.Utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldCanBBeLocal", "DuplicatedCode"})
@Service(value = "ngxAuthRssService")
@Transactional
public class NgxAuthRssServiceImpl implements NgxAuthRssService {

    private static final Logger logger = LoggerFactory.getLogger(NgxAuthRssServiceImpl.class);

    @Override
    public void onConfigHttpSecurities(RSSProperties rssProperties, HttpSecurity http) throws Exception {

        if (!ObjectUtils.allNotNull(rssProperties)) {
            if (logger.isErrorEnabled()) {
                logger.error("onConfigHttpSecurities::RSSProperties is required");
            }
            return;
        }

        if (!rssProperties.isEnabled()) {
            if (logger.isErrorEnabled()) {
                logger.error("onConfigHttpSecurities::RSSProperties::enabled --> off = false");
            }
            return;
        }

        if (CollectionsUtility.isEmpty(rssProperties.getEndpointsPermitted())) {
            rssProperties.setEndpointsPermitted(RSSProperties.initEndpointsPermitted());
        }

        if (CollectionsUtility.isEmpty(rssProperties.getEndpointsDenied())) {
            rssProperties.setEndpointsDenied(RSSProperties.initEndpointsDenied());
        }

        List<String> endpointsPermitted = rssProperties.getEndpointsPermitted()
                .stream()
                .filter(endpointsLabel -> endpointsLabel.isEnabled() && StringUtility.isNotEmpty(endpointsLabel.getEndpointShortUrl()))
                .map(EndpointsLabel::getEndpointShortUrl)
                .collect(Collectors.toList());

        List<String> endpointsDenied = rssProperties.getEndpointsDenied()
                .stream()
                .filter(endpointsLabel -> endpointsLabel.isEnabled() && StringUtility.isNotEmpty(endpointsLabel.getEndpointShortUrl()))
                .map(EndpointsLabel::getEndpointShortUrl)
                .collect(Collectors.toList());

        if (CollectionsUtility.isEmpty(endpointsPermitted)) {
            endpointsPermitted = new ArrayList<>();
            endpointsPermitted.add("/swagger-ui.html"); // default value
        }

        if (CollectionsUtility.isEmpty(endpointsDenied)) {
            endpointsDenied = new ArrayList<>();
            endpointsDenied.add("/api/v1/configs/**");
        }

        String[] endpointsPermittedSecurities = ExchangeUtils.exchangeListStringToStringArrayUsingArraysCopyOf(endpointsPermitted);
        String[] endpointsDeniedSecurities = ExchangeUtils.exchangeListStringToStringArrayUsingArraysCopyOf(endpointsDenied);

        if (logger.isInfoEnabled()) {
            logger.info("onConfigHttpSecurities::endpointsPermittedSecurities: {}", LoggerUtils.toJson(endpointsPermittedSecurities));
            logger.info("onConfigHttpSecurities::endpointsDeniedSecurities: {}", LoggerUtils.toJson(endpointsDeniedSecurities));
        }

        http
                .authorizeRequests()
                .antMatchers(endpointsPermittedSecurities).permitAll()
                .mvcMatchers(endpointsPermittedSecurities).permitAll()
                .antMatchers(endpointsDeniedSecurities).denyAll()
                .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('read')")
                .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.PATCH, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('write')")
                .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('write')")
                .and()
                .headers().addHeaderWriter((request, response) -> {
            response.addHeader(OAuth2AccessTokenVariable.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            if (request.getMethod().equals("OPTIONS")) {
                response.setHeader(
                        OAuth2AccessTokenVariable.ACCESS_CONTROL_ALLOW_METHODS,
                        request.getHeader(OAuth2AccessTokenVariable.ACCESS_CONTROL_REQUEST_METHOD));
                response.setHeader(
                        OAuth2AccessTokenVariable.ACCESS_CONTROL_ALLOW_HEADERS,
                        request.getHeader(OAuth2AccessTokenVariable.ACCESS_CONTROL_REQUEST_HEADERS));
            }
        });
    }

    @Override
    public void onConfigResourceServerSecurities(RSSProperties rssProperties, ResourceServerSecurityConfigurer resources, ResourceServerTokenServices tokenService, AuthenticationManager authenticationManagerBean) {

        if (!ObjectUtils.allNotNull(rssProperties)) {
            if (logger.isErrorEnabled()) {
                logger.error("onConfigResourceServerSecurities::RSSProperties is required");
            }
            return;
        }

        if (!rssProperties.isEnabled()) {
            if (logger.isErrorEnabled()) {
                logger.error("onConfigResourceServerSecurities::RSSProperties::enabled --> off = false");
            }
            return;
        }

        resources.resourceId(rssProperties.getResourceId())
                .authenticationManager(authenticationManagerBean)
                .tokenExtractor(new NgxTokenExtractor())
                .tokenServices(tokenService)
                .authenticationEntryPoint(new AuthEntriesPointResponse())
                .accessDeniedHandler(new AccessDeniedHandlerResponse());
    }

    private static class OAuth2AccessTokenVariable {

        private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    }
}
