package com.odobo.grails.plugin.springsecurity.rest.token.bearer

import com.odobo.grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader
import groovy.util.logging.Log4j
import groovy.util.logging.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles prompting the client for authentication when using bearer tokens.
 */
@Slf4j
class BearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    BearerTokenReader tokenReader

    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        def tokenValue = tokenReader.findToken(request)

        if (tokenValue) {
            response.addHeader('WWW-Authenticate', 'Bearer error="invalid_token"')
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        } else {
            response.addHeader('WWW-Authenticate', 'Bearer')
            response.status = HttpServletResponse.SC_FORBIDDEN
        }

    }
}
