package com.odobo.grails.plugin.springsecurity.rest

import groovy.util.logging.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Sets the configured status code.
 */
@Slf4j
class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Configurable status code, by default: conf.rest.login.failureStatusCode?:HttpServletResponse.SC_FORBIDDEN
     */
    Integer statusCode

    /**
     * Called when an authentication attempt fails.
     * @param request the request during which the authentication attempt occurred.
     * @param response the response.
     * @param exception the exception which was thrown to reject the authentication request.
     */
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.debug "Setting status code to ${statusCode}"
        response.setStatus(statusCode)
    }
}
