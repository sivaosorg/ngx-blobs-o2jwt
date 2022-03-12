package com.phuocnguyen.app.ngxblobso2jwt.service.serviceImpl;

import com.ngxsivaos.converter.NgxAccessTokenConverter;
import com.ngxsivaos.model.label.EndpointsLabel;
import com.ngxsivaos.model.properties.RSSProperties;
import com.ngxsivaos.model.properties.RSSReCallbackProperties;
import com.phuocnguyen.app.ngxblobso2jwt.model.builder.NgxTokenExtractor;
import com.phuocnguyen.app.ngxblobso2jwt.model.response.AccessDeniedHandlerResponse;
import com.phuocnguyen.app.ngxblobso2jwt.model.response.AuthEntriesPointResponse;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import com.sivaos.Model.ObjectEnumeration.ObjectUserStatusEnumeration;
import com.sivaos.Utility.CollectionsUtility;
import com.sivaos.Utility.JWTsUtility;
import com.sivaos.Utility.StringUtility;
import com.sivaos.Utils.ExchangeUtils;
import com.sivaos.Utils.LoggerUtils;
import com.sivaos.Utils.ObjectUtils;
import com.sivaos.Utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({
        "FieldCanBeLocal",
        "DuplicatedCode"
})
@Service(value = "ngxAuthRssService")
@Transactional
public class NgxAuthRssServiceImpl implements NgxAuthRssService {

    private static final Logger logger = LoggerFactory.getLogger(NgxAuthRssServiceImpl.class);

    private final AccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
    private final RSSReCallbackProperties rssReCallbackProperties;

    @Autowired
    public NgxAuthRssServiceImpl(
            RSSReCallbackProperties rssReCallbackProperties) {
        this.rssReCallbackProperties = rssReCallbackProperties;
    }


    @Override
    public void onConfigHttpSecurities(RSSProperties rssProperties, HttpSecurity http) throws Exception {

        if (!ObjectUtils.allNotNull(rssProperties)) {
            if (logger.isErrorEnabled()) {
                logger.error("ResourceServer::onConfigHttpSecurities::RSSProperties is required");
            }
            return;
        }

        if (!rssProperties.isEnabled()) {
            if (logger.isErrorEnabled()) {
                logger.error("ResourceServer::onConfigHttpSecurities::RSSProperties::enabled --> off = false");
            }

            http
                    .authorizeRequests()
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

        } else {

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
                logger.info("ResourceServer::onConfigHttpSecurities::endpointsPermittedSecurities: {}", LoggerUtils.toJson(endpointsPermittedSecurities));
                logger.info("ResourceServer::onConfigHttpSecurities::endpointsDeniedSecurities: {}", LoggerUtils.toJson(endpointsDeniedSecurities));
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

    }

    @Override
    public void onConfigResourceServerSecurities(RSSProperties rssProperties,
                                                 ResourceServerSecurityConfigurer resources,
                                                 ResourceServerTokenServices tokenService,
                                                 AuthenticationManager authenticationManagerBean) {

        if (!ObjectUtils.allNotNull(rssProperties)) {
            if (logger.isErrorEnabled()) {
                logger.error("ResourceServer::onConfigResourceServerSecurities::RSSProperties is required");
            }
            return;
        }

        if (!rssProperties.isEnabled()) {

            if (logger.isErrorEnabled()) {
                logger.error("ResourceServer::onConfigResourceServerSecurities::RSSProperties::enabled --> off = false");
            }

        } else {
            resources.resourceId(rssProperties.getResourceId())
                    .authenticationManager(authenticationManagerBean)
                    .tokenExtractor(new NgxTokenExtractor())
                    .tokenServices(tokenService)
                    .authenticationEntryPoint(new AuthEntriesPointResponse())
                    .accessDeniedHandler(new AccessDeniedHandlerResponse());
        }

    }

    /**
     * @param path           - path to check token, example: http://localhost:8083/oauth/check_token?token=eyJhfjvf
     * @param headers        -
     * @param restOperations -
     */
    private Map<String, Object> executeGet(String path, HttpHeaders headers, RestOperations restOperations) {
        try {
            if (headers.getContentType() == null) {
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }
            @SuppressWarnings("rawtypes")
            Map map = restOperations.exchange(path, HttpMethod.GET, new HttpEntity<MultiValueMap<String, String>>(null, headers), Map.class).getBody();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = map;
            return result;
        } catch (Exception error) {
            logger.error(error.getMessage(), error);
            return Collections.emptyMap();
        }
    }

    @Override
    public OAuth2Authentication onCallbackToCheckToken(String accessToken, RestOperations restOperations) {

        if (!rssReCallbackProperties.isEnabled()) {
            if (logger.isErrorEnabled()) {
                logger.error("NgxAuthRssService::onCallbackToCheckToken::rssReCallbackProperties.isEnabled() = {}",
                        rssReCallbackProperties.isEnabled());
            }

            return null;
        }

        if (!ValidationUtils.isVerifiedAsUrl(rssReCallbackProperties.getHostAuth())) {
            if (logger.isErrorEnabled()) {
                logger.error("NgxAuthRssService::onCallbackToCheckToken::rssReCallbackProperties::hostAuth = {} invalid",
                        rssReCallbackProperties.getHostAuth());
            }

            throw new IllegalArgumentException(OAuth2Exception.URI);
        }

        HttpHeaders headers = new HttpHeaders();
        String decryptAccessToken;
        String form = "%s%s%s";

        if (accessToken.startsWith(OAuth2AccessTokenVariable.TOKEN_PREFIX) ||
                accessToken.contains(OAuth2AccessTokenVariable.TOKEN_PREFIX)) {
            decryptAccessToken = accessToken;
        } else {
            // decryptAccessToken = AES256Utils.decryptDefault(accessToken);
            decryptAccessToken = JWTsUtility.decodeJWTs(accessToken);
        }

        String url = StringUtility.trimAllWhitespace(String.format(form,
                rssReCallbackProperties.getHostAuth(),
                rssReCallbackProperties.getPrefixUrl(),
                decryptAccessToken));

        Map<String, Object> map = executeGet(url, headers, restOperations);

        if (CollectionsUtility.isEmptyMap(map) ||
                ObjectUtils.allNotNull(map.get(OAuth2AccessTokenVariable.ERROR_MESSAGE_PREFIX))) {
            throw new InvalidTokenException("Token not allowed");
        }

        if (map.containsValue(ObjectUserStatusEnumeration.INACTIVE.getValues())) {
            throw new InvalidTokenException(OAuth2Exception.ACCESS_DENIED);
        }

        // return tokenConverter.extractAuthentication(map); // unused, currently, token has been custom content
        return new NgxAccessTokenConverter().extractAuthentication(map); // new format token has been custom
    }

    private static class OAuth2AccessTokenVariable {

        private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
        private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
        private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
        private static final String TOKEN_PREFIX = "eyJ";
        private static final String ERROR_MESSAGE_PREFIX = "error";
    }
}
