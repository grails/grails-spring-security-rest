package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import spock.lang.Specification

class RestTokenValidationFilterSpec extends Specification {

    def filter = new RestTokenValidationFilter()

    def request  = new MockHttpServletRequest()
    def response = new MockHttpServletResponse()
    def chain = new MockFilterChain()

    def setup() {
        filter.authenticationSuccessHandler = Mock(AuthenticationSuccessHandler)
        filter.authenticationFailureHandler = Mock(AuthenticationFailureHandler)
    }

    void "authentication passes when using valid token with custom header"() {

        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        filter.headerName = headerName
        request.addHeader( headerName, token )
        filter.doFilter( request, response, chain )

        then:
        response.status == 200
        0 * filter.authenticationFailureHandler.onAuthenticationFailure( _, _, _ )
        notThrown( TokenNotFoundException )

        where:

        token = 'mytokenvalue'
        headerName << [ 'AuthToken', 'FooBarHeader' ]

    }

    void "authentication fails when using invalid token with custom header"() {

        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        filter.headerName = headerName
        request.addHeader( headerName, token + 'invalid' )
        filter.doFilter( request, response, chain )

        then:

        1 * filter.authenticationFailureHandler.onAuthenticationFailure( request, response, _ as AuthenticationException )

        where:

        token = 'mytokenvalue'
        headerName << [ 'AuthToken', 'FooBarHeader' ]

    }

    void "authentication succeeds when using valid token in Authorization header with bearer token"() {

        filter.useBearerToken = true
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        request.addHeader( 'Authorization', "Bearer ${token}" )
        filter.doFilter( request, response, chain )

        then:

        response.status == 200
        0 * filter.authenticationFailureHandler.onAuthenticationFailure( _, _, _ )
        notThrown( TokenNotFoundException )

        where:

        token = 'abcdefghijklmnopqrstuvwxyz'
    }

    void "authentication fails when using invalid token in Authorization header with bearer token"() {

        filter.useBearerToken = true
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        request.addHeader( 'Authorization', "Bearer ${token}invalidvalue" )
        filter.doFilter( request, response, chain )

        then:

        1 * filter.authenticationFailureHandler.onAuthenticationFailure( request, response, _ as AuthenticationException )

        where:

        token = 'abcdefghijklmnopqrstuvwxyz'
    }


    void "authentication succeeds when using valid token in query string with bearer token"() {

        filter.useBearerToken = true
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        request.queryString = "access_token=${token}"
        filter.doFilter( request, response, chain )

        then:

        response.status == 200
        0 * filter.authenticationFailureHandler.onAuthenticationFailure( _, _, _ )
        notThrown( TokenNotFoundException )

        where:

        token = 'abcdefghijklmnopqrstuvwxyz'
    }

    void "authentication fails when using invalid token in query string with bearer token"() {

        filter.useBearerToken = true
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:

        request.queryString = "access_token=${token}invalidinvalid"
        filter.doFilter( request, response, chain )

        then:

        1 * filter.authenticationFailureHandler.onAuthenticationFailure( request, response, _ as AuthenticationException )

        where:

        token = 'abcdefghijklmnopqrstuvwxyz'
    }
}

/**
 * Stubs out the RestAuthenticationProvider so that we can specify what a valid token is,
 * and have it return as if it authentication correctly if the token matches.
 */
class StubRestAuthenticationProvider extends RestAuthenticationProvider {

    String validToken
    String username
    String password

    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        authentication = authentication as RestAuthenticationToken
        if( authentication.tokenValue == validToken ) {
            return new RestAuthenticationToken( username, password, null, validToken )
        }

        throw new TokenNotFoundException( 'Token not found' )
    }
}
