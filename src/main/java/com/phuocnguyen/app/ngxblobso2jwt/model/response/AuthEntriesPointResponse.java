package com.phuocnguyen.app.ngxblobso2jwt.model.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivaos.Model.Response.Extend.HttpStatusCodesResponseDTO;
import com.sivaos.Model.Response.Extend.StatusCodeResponseDTO;
import com.sivaos.Utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"DuplicatedCode"})
public class AuthEntriesPointResponse implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntriesPointResponse.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        StatusCodeResponseDTO statuses = HttpStatusCodesResponseDTO.UNAUTHORIZED;
        statuses.setDescription(authException.getMessage());

        Map<String, Object> map = new HashMap<>();

        map.put("code", statuses.getCode());
        map.put("header", statuses);
        map.put("message", authException.getMessage());
        map.put("path", request.getServletPath());
        map.put("timestamp", new Date().getTime());
        map.put("publish", DateUtils.feedStageAsString(new Date()));

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException();
        }
    }
}
