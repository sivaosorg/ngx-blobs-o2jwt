package com.phuocnguyen.app.ngxblobso2jwt.component;

import com.ngxsivaos.config.EventListenerConfig;
import com.ngxsivaos.model.request.AuthSecuritiesContextRequest;
import com.ngxsivaos.model.request.AuthenticationDetailsParserRequest;
import com.ngxsivaos.model.response.UserDetailsResponse;
import com.ngxsivaos.utilities.JsonUtility;
import com.phuocnguyen.app.ngxblobso2jwt.model.request.MutableHttpServletRequest;
import com.sivaos.Model.UserDTO;
import com.sivaos.Variables.PatternEpochVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

@SuppressWarnings({
        "NullableProblems"
})
@Component
public class NgxRequestWrapperFiltersConfig extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(NgxRequestWrapperFiltersConfig.class);


    private static UserDetailsResponse build(UserDTO user) {
        UserDetailsResponse usersDetails = new UserDetailsResponse();

        usersDetails.setRolesId(user.getRoleIds());
        usersDetails.setId(user.getId());
        usersDetails.setUsername(user.getUsername());
        usersDetails.setEmail(user.getEmail());
        usersDetails.setPrivileges(user.getPrivileges());
        usersDetails.setSysSubjectsPerms(user.getSysSubjectsPerms());

        return usersDetails;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userDetailsByToken = JsonUtility.minify(SecurityContextHolder.getContext().getAuthentication());
        AuthenticationDetailsParserRequest authentication = JsonUtility.json2Target(
                userDetailsByToken,
                AuthenticationDetailsParserRequest.class,
                JsonUtility.createInstance().setDateFormat(new SimpleDateFormat(PatternEpochVariable.BIBLIOGRAPHY_EPOCH_PATTERN))
        );

        AuthSecuritiesContextRequest authSecuritiesContextRequest = EventListenerConfig.onAuthSecuritiesContextRequest(authentication);
        UserDTO user = EventListenerConfig.getUserDetails(EventListenerConfig.getAuthentication());
        Map<String, Boolean> privilegesCurrently = EventListenerConfig.allRuntimePerms();

        // logger.info("doFilterInternal() authentication before = {}", JsonUtility.toJsonPretty(authentication));
        // logger.info("doFilterInternal() authSecuritiesContextRequest = {}", JsonUtility.toJsonPretty(authSecuritiesContextRequest));
        // logger.info("doFilterInternal() user request = {}", JsonUtility.toJsonPretty(user));
        // logger.info("doFilterInternal() privilegesCurrently = {}", JsonUtility.toJsonPretty(privilegesCurrently));

        MutableHttpServletRequest wrappedRequest = new MutableHttpServletRequest(request);
        wrappedRequest.addHeader("new_header", "x_cr_sf_");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                build(user),
                authentication.getCredentials(),
                authentication.getAuthorities());

        auth.setDetails(authentication.getDetails());

        // SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("doFilterInternal(), pre-filter starting");
        filterChain.doFilter(wrappedRequest, response);
    }
}
