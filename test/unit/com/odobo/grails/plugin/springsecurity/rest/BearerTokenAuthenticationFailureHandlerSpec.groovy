package com.odobo.grails.plugin.springsecurity.rest

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import spock.lang.Specification

class BearerTokenAuthenticationFailureHandlerSpec extends Specification {

    def handler = new BearerTokenAuthenticationFailureHandler()

    def "it will send a 401 status and WWW-Authenticate header when no credentials were provided"() {

        def request  = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()

        when:
        def exception = new AuthenticationCredentialsNotFoundException( 'No credentials :-(' )
        handler.onAuthenticationFailure( request, response, exception )

        then:
        response.status == 401
        response.getHeader( 'WWW-Authenticate' ) == 'Bearer'
    }

    def "it will send a 401 status and WWW-Authenticate header with an error param when credentials are invalid"() {

        def request  = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()

        when:
        def exception = new BadCredentialsException( 'Bad credentials :-(' )
        handler.onAuthenticationFailure( request, response, exception )

        then:
        response.status == 401
        response.getHeader( 'WWW-Authenticate' ) == 'Bearer error="invalid_token"'
    }

}
