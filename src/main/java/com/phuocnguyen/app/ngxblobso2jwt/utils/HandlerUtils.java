package com.phuocnguyen.app.ngxblobso2jwt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivaos.Configurer.CustomFilterRequest.TypeSafeRequest;
import com.sivaos.Model.ObjectEnumeration.Original.ComparatorType;
import com.sivaos.Model.Response.Extend.HttpStatusCodesResponseDTO;
import com.sivaos.Model.Response.Extend.StatusCodeResponseDTO;
import com.sivaos.Utility.CollectionsUtility;
import com.sivaos.Utils.DateUtils;
import com.sivaos.Utils.ObjectUtils;
import com.sivaos.Utils.UrlQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({
        "DuplicatedCode"
})
public class HandlerUtils {

    private static final Logger logger = LoggerFactory.getLogger(HandlerUtils.class);

    /**
     * @param request   -
     * @param response  -
     * @param exception - extend super class exception
     * @param clazz     - class this function
     */
    public static void onHandlerErrors(HttpServletRequest request, HttpServletResponse response, RuntimeException exception, Class<?> clazz) throws ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        StatusCodeResponseDTO statuses = HttpStatusCodesResponseDTO.UNAUTHORIZED;
        statuses.setDescription(exception.getMessage());

        Map<String, Object> map = new HashMap<>();

        Map<String, String> queries = TypeSafeRequest.snapTypeSafeRequest(request);
        String urlShortCompleted = UrlQueryUtils.snapQuery(request.getServletPath(), CollectionsUtility.sortByKey(queries, ComparatorType.ASCENDING));

        map.put(HandlerAttributes.CODE_ATTRIBUTE, statuses.getCode());
        map.put(HandlerAttributes.HEADER_ATTRIBUTE, statuses);
        map.put(HandlerAttributes.MESSAGE_ATTRIBUTE, exception.getMessage());
        map.put(HandlerAttributes.PATH_ATTRIBUTE, urlShortCompleted);
        map.put(HandlerAttributes.TIMESTAMP_ATTRIBUTE, new Date().getTime() / 1000);
        map.put(HandlerAttributes.PUBLISH_ATTRIBUTE, DateUtils.feedStageAsString(new Date()));
        map.put(HandlerAttributes.CLASS_ATTRIBUTE, clazz);
        map.put(HandlerAttributes.METHOD_ATTRIBUTE, request.getMethod());
        map.put(HandlerAttributes.DEBUG_MESSAGE_ATTRIBUTE, exception.getLocalizedMessage());

        if (ObjectUtils.allNotNull(request.getSession())) {
            map.put(HandlerAttributes.SESSION_ID_ATTRIBUTE, request.getSession().getId());
        } else {
            map.put(HandlerAttributes.SESSION_ID_ATTRIBUTE, request.getRequestedSessionId());
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
    }

    private static class HandlerAttributes {
        public static final String CODE_ATTRIBUTE = "code";
        public static final String HEADER_ATTRIBUTE = "header";
        public static final String MESSAGE_ATTRIBUTE = "message";
        public static final String PATH_ATTRIBUTE = "path";
        public static final String TIMESTAMP_ATTRIBUTE = "timestamp";
        public static final String PUBLISH_ATTRIBUTE = "publish";
        public static final String CLASS_ATTRIBUTE = "class";
        public static final String METHOD_ATTRIBUTE = "method";
        public static final String DEBUG_MESSAGE_ATTRIBUTE = "debugMsg";
        public static final String SESSION_ID_ATTRIBUTE = "sessionId";
    }
}
