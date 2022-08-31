package com.phuocnguyen.app.ngxblobso2jwt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivaos.Model.Response.Extend.HttpStatusCodesResponseDTO;
import com.sivaos.Model.Response.Extend.StatusCodeResponseDTO;
import com.sivaos.Utils.DateUtils;
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

        map.put("code", statuses.getCode());
        map.put("header", statuses);
        map.put("message", exception.getMessage());
        map.put("path", request.getServletPath());
        map.put("timestamp", new Date().getTime());
        map.put("publish", DateUtils.feedStageAsString(new Date()));
        map.put("class", clazz);
        map.put("method", request.getMethod());
        map.put("sessionId", request.getSession().getId());

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }
    }
}
