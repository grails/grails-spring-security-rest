package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.rfc6750.BearerTokenReader
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles prompting the client for authentication when using the BearerToken authentication scheme.
 */
@Log4j
@CompileStatic
class BearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    BearerTokenReader tokenReader

    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        def tokenValue = tokenReader.findToken(request, response)

        if( tokenValue ) {
            response.addHeader( 'WWW-Authenticate', 'Bearer error="invalid_token"' )
        } else {
            response.addHeader( 'WWW-Authenticate', 'Bearer' )
        }

        if( response.status in 200..299 ) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        }
    }
}
