package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.reader.TokenReader
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import spock.lang.Specification

import javax.servlet.FilterChain

class RestTokenValidationFilterUnitSpec extends Specification {

    def filter = new RestTokenValidationFilter(active: true)

    def request  = new GrailsMockHttpServletRequest()
    def response = new MockHttpServletResponse()
    def chain = new MockFilterChain()

    def setup() {
        filter.authenticationSuccessHandler = Mock(AuthenticationSuccessHandler)
        filter.authenticationFailureHandler = Mock(AuthenticationFailureHandler)
        filter.tokenReader = Mock(TokenReader)
        filter.authenticationEventPublisher = Mock(DefaultAuthenticationEventPublisher)
    }

    void "authentication passes when a valid token is found"() {
        given:
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )


        when:
        filter.doFilter( request, response, chain )

        then:
        response.status == 200
        1 * filter.tokenReader.findToken(request) >> token
        0 * filter.authenticationFailureHandler.onAuthenticationFailure( _, _, _ )
        1 * filter.authenticationEventPublisher.publishAuthenticationSuccess(_ as Authentication)
        notThrown( TokenNotFoundException )

        where:
        token = 'mytokenvalue'
    }

    void "when a token cannot be found, the request continues through the filter chain"() {
        given:
        filter.restAuthenticationProvider = new StubRestAuthenticationProvider(
                validToken: 'mytokenvalue',
                username: 'user',
                password: 'password'
        )
        FilterChain filterChain = GroovyMock(MockFilterChain, global: true)

        when:
        filter.doFilter( request, response, chain )

        then:
        1 * filter.tokenReader.findToken(request) >> null
        1 * filterChain.doFilter(request, response)
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
