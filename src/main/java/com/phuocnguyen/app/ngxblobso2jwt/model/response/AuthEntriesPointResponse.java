package com.phuocnguyen.app.ngxblobso2jwt.model.response;

import com.phuocnguyen.app.ngxblobso2jwt.utils.HandlerUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({"DuplicatedCode"})
public class AuthEntriesPointResponse implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws ServletException {
        HandlerUtils.onHandlerErrors(request, response, authException, AuthEntriesPointResponse.class);
    }
}
