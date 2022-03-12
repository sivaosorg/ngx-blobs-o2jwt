package com.phuocnguyen.app.ngxblobso2jwt.model.indicator;

import com.phuocnguyen.app.ngxblobso2jwt.service.NgxAuthRssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@SuppressWarnings({
        "FieldCanBeLocal",
        "FieldMayBeFinal",
        "NullableProblems",
        "SpringJavaAutowiredMembersInspection"
})
public class ReCallbackResourceServerIndicator implements ResourceServerTokenServices {

    private RestOperations restOperations;

    @Autowired
    private NgxAuthRssService ngxAuthRssService;

    @Autowired
    public ReCallbackResourceServerIndicator() {
        restOperations = new RestTemplate();
        ((RestTemplate) restOperations).setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400) {
                    super.handleError(response);
                }
            }
        });
    }


    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        return ngxAuthRssService.onCallbackToCheckToken(accessToken, restOperations);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

}
