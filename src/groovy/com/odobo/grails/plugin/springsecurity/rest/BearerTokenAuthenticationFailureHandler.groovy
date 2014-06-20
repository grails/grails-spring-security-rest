package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles authentication failure when BearerToken authentication is enabled.
 */
@Slf4j
class BearerTokenAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * Sends the proper response code and headers, as defined by RFC6750.
     *
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        if (!response.containsHeader('WWW-Authenticate')) {
            String headerValue

            //response code is determined by authentication failure reason
            if(e instanceof TokenNotFoundException) {
                //The user supplied credentials, but they did not match an account,
                // or there was an underlying authentication issue.
                headerValue = 'Bearer error="invalid_token"'
            } else {
                //no credentials were provided.  Add no additional information
                headerValue = 'Bearer'
            }

            response.addHeader('WWW-Authenticate', headerValue)
        }

        if (response.status == 200) {
            response.status = 401
        }

        log.debug "Sending status code ${response.status} and header WWW-Authenticate: ${response.getHeader('WWW-Authenticate')}"
    }
}
