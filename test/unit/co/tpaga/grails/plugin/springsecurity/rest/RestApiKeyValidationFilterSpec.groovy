package co.tpaga.grails.plugin.springsecurity.rest

import co.tpaga.grails.plugin.springsecurity.rest.token.reader.ApiKeyReader

import co.tpaga.grails.plugin.springsecurity.rest.token.storage.ApiKeyNotFoundException

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import spock.lang.Specification

class RestApiKeyValidationFilterUnitSpec extends Specification {

    def filter = new RestApiKeyValidationFilter(active: true)

    def request  = new GrailsMockHttpServletRequest()
    def response = new MockHttpServletResponse()
    def chain = new MockFilterChain()

    def setup() {
        filter.authenticationSuccessHandler = Mock(AuthenticationSuccessHandler)
        filter.authenticationFailureHandler = Mock(AuthenticationFailureHandler)
        filter.apiKeyReader = Mock(ApiKeyReader)
    }

    void "authentication passes when a valid token is found"() {
        given:
        filter.restApiKeyAuthenticationProvider = new StubRestApiKeyAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )


        when:
        filter.doFilter( request, response, chain )

        then:
        response.status == 200
        1 * filter.apiKeyReader.findApiKey(request, response) >> token
        0 * filter.authenticationFailureHandler.onAuthenticationFailure( _, _, _ )
        notThrown( ApiKeyNotFoundException )

        where:
        token = 'mytokenvalue'
    }

    void "authentication fails when a token cannot be found"() {
        given:
        filter.restApiKeyAuthenticationProvider = new StubRestApiKeyAuthenticationProvider(
                validToken: token,
                username: 'user',
                password: 'password'
        )

        when:
        filter.doFilter( request, response, chain )

        then:
        1 * filter.apiKeyReader.findApiKey(request, response) >> null
        1 * filter.authenticationFailureHandler.onAuthenticationFailure( request, response, _ as AuthenticationException )

        where:
        token = 'mytokenvalue'
    }
}

/**
 * Stubs out the RestAuthenticationProvider so that we can specify what a valid token is,
 * and have it return as if it authentication correctly if the token matches.
 */
class StubRestApiKeyAuthenticationProvider extends RestApiKeyAuthenticationProvider {

    String validToken
    String username
    String password

    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        authentication = authentication as RestApiKeyAuthenticationToken
        if( authentication.apiKeyValue == validToken ) {
            return new RestApiKeyAuthenticationToken( username, password, null, validToken )
        }

        throw new ApiKeyNotFoundException( 'Api key not found' )
    }
}
