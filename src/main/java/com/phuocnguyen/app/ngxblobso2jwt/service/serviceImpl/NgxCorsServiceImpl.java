package com.phuocnguyen.app.ngxblobso2jwt.service.serviceImpl;

import com.ngxsivaos.model.request.StatelessCookieRequest;
import com.ngxsivaos.utils.original.KLogs;
import com.phuocnguyen.app.ngxblobso2jwt.service.NgxCorsService;
import com.sivaos.Configurer.CustomFilterRequest.Model.HttpServletFilter;
import com.sivaos.Utility.StringUtility;
import com.sivaos.Utils.ClassesUtils;
import com.sivaos.Utils.ExchangeUtils;
import com.sivaos.Utils.ObjectUtils;
import com.sivaos.Utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

import static com.phuocnguyen.app.ngxblobso2jwt.service.serviceImpl.NgxCorsServiceImpl.CorsAttributes.*;

@SuppressWarnings({
        "FieldCanBeLocal",
        "DuplicatedCode"
})
@Service(value = "ngxCorsService")
public class NgxCorsServiceImpl implements NgxCorsService {

    private static final Logger logger = LoggerFactory.getLogger(NgxCorsServiceImpl.class);

    static {
        KLogs.setClazz(NgxCorsServiceImpl.class);
        KLogs.setEnabledThrowable(true);
        KLogs.setEnabledSecureLogs(true);
    }

    private FilterConfig config;

    public static String generateClass(Class<?> clazz) {
        return String.format("%s.%s", clazz.getName(), FIELD_ATTRIBUTE);
    }

    public FilterConfig getConfig() {
        return config;
    }

    public void setConfig(FilterConfig config) {
        this.config = config;
    }

    @Override
    public void setCurrentConfig(FilterConfig config) {
        setConfig(config);
    }

    @Override
    public StatelessCookieRequest getStatelessBy(FilterConfig config) {
        StatelessCookieRequest response = new StatelessCookieRequest();

        Pattern COMMA_PATTERN = Pattern.compile(",");

        Class<?> clazz = StatelessCookieRequest.class;
        String value = ClassesUtils.snagOneField(clazz, CSRF_ATTRIBUTE);
        String currentExcludedUrls = config.getInitParameter(EXCLUDE_ATTRIBUTE);
        String currentExcludedFormUrls = config.getInitParameter(EXCLUDE_GET_ATTRIBUTE);
        String currentExcludeStartWithUrls = config.getInitParameter(EXCLUDE_GET_WITH_ATTRIBUTE);
        String currentCookieMaxAge = config.getInitParameter(COOKIE_MAX_AGE_ATTRIBUTE);

        int cookieMaxAge = 0;
        Set<String> excludeUrls;
        Set<String> excludeFormUrls;
        List<String> excludeStartWithUrls;

        if (StringUtility.isEmpty(value)) {
            try {
                String message = String.format("%s parameter should be specified", value);
                throw new ServletException(message);
            } catch (ServletException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                } else {
                    KLogs.error(e.getMessage(), e);
                }
            }
        }

        if (StringUtility.isNotEmpty(currentExcludedUrls)) {
            String[] elements = COMMA_PATTERN.split(currentExcludedUrls);
            excludeUrls = new HashSet<>(elements.length);
            excludeUrls.addAll(Arrays.asList(elements));
        } else {
            excludeUrls = new HashSet<>(0);
        }

        if (StringUtility.isNotEmpty(currentExcludedFormUrls)) {
            String[] elements = COMMA_PATTERN.split(currentExcludedFormUrls);
            excludeFormUrls = new HashSet<>(elements.length);

            for (String element : elements) {
                excludeFormUrls.add(element.trim());
            }
        } else {
            excludeFormUrls = new HashSet<>(0);
        }

        if (StringUtility.isNotEmpty(currentExcludeStartWithUrls)) {
            String[] elements = COMMA_PATTERN.split(currentExcludeStartWithUrls);
            excludeStartWithUrls = new ArrayList<>(elements.length);

            for (String element : elements) {
                excludeStartWithUrls.add(element.trim());
            }
        } else {
            excludeStartWithUrls = new ArrayList<>(0);
        }

        if (StringUtility.isNotEmpty(currentCookieMaxAge)) {
            if (ValidationUtils.isVerifiedAsNumber(currentCookieMaxAge)) {
                cookieMaxAge = ExchangeUtils.exchangeStringToIntegerUsingParseInt(currentCookieMaxAge);
            } else {
                try {
                    String message = String.format("%d must be an integer", cookieMaxAge);
                    throw new ServletException(message);
                } catch (ServletException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(e.getMessage(), e);
                    } else {
                        KLogs.error(e.getMessage(), e);
                    }
                }
            }
        } else {
            cookieMaxAge = 3600; // 60*60 seconds = 1 hour
        }

        response.setCsrfToken(value);
        response.setRandom(new SecureRandom());
        response.setCookieMaxAge(cookieMaxAge);
        response.setExcludeUrls(excludeUrls);
        response.setExcludeFormUrls(excludeFormUrls);
        response.setExcludeStartWithUrls(excludeStartWithUrls);
        response.setOncePerRequestAttributeName(generateClass(clazz));

        return response;
    }

    @Override
    public void filterBy(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        StatelessCookieRequest stateless = getStatelessBy(getConfig());

        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }

        try {
            if (StringUtility.isNotEmpty(request.getContentType()) ||
                    ObjectUtils.allNotNull(request.getAttribute(stateless.getOncePerRequestAttributeName()))) {
                if (request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
                    chain.doFilter(request, response);
                } else {
                    doFilter(request, response, chain);
                }
            } else {
                doFilter(request, response, chain);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            } else {
                KLogs.error(e.getMessage(), e);
            }
        }

    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        HttpServletFilter filter = new HttpServletFilter(request, response);
        try {
            chain.doFilter(filter.getBufferedRequestWrapper(), filter.getBufferedResponseWrapper());

        } catch (IOException | ServletException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            } else {
                KLogs.error(e.getMessage(), e);
            }
        }
    }

    public static class CorsAttributes {
        public static final String FIELD_ATTRIBUTE = "ATTR";
        public static final String CSRF_ATTRIBUTE = "csrf";
        public static final String EXCLUDE_ATTRIBUTE = "exclude";
        public static final String EXCLUDE_GET_ATTRIBUTE = "excludeGET";
        public static final String EXCLUDE_GET_WITH_ATTRIBUTE = "excludeGETStartWith";
        public static final String COOKIE_MAX_AGE_ATTRIBUTE = "cookieMaxAge";
    }
}
