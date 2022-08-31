package com.phuocnguyen.app.ngxblobso2jwt.model.response;

import com.phuocnguyen.app.ngxblobso2jwt.utils.HandlerUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({"DuplicatedCode"})
public class AccessDeniedHandlerResponse implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws ServletException {
        HandlerUtils.onHandlerErrors(request, response, accessDeniedException, AccessDeniedHandlerResponse.class);
    }
}
