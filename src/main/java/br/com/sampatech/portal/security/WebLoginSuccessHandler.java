package br.com.sampatech.portal.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public class WebLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    public WebLoginSuccessHandler(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        loginAttemptService.loginSucceeded(authentication.getName());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
