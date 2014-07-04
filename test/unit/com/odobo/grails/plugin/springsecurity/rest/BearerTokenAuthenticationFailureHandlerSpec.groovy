package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.bearer.BearerTokenAuthenticationFailureHandler
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import spock.lang.Specification

class BearerTokenAuthenticationFailureHandlerSpec extends Specification {

    def handler = new BearerTokenAuthenticationFailureHandler()

    def "it will send a 401 status and WWW-Authenticate header when no credentials were provided"() {
        given:
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
        given:
        def request  = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()

        when:
        def exception = new TokenNotFoundException( 'Bad token :-(' )
        handler.onAuthenticationFailure( request, response, exception )

        then:
        response.status == 401
        response.getHeader( 'WWW-Authenticate' ) == 'Bearer error="invalid_token"'
    }

}
