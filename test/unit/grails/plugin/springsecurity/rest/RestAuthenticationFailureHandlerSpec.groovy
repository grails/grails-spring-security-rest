package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.AuthenticationException
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RestAuthenticationFailureHandlerSpec extends Specification {

	void "it returns #statusCode when authentication fails"() {
        given:
        RestAuthenticationFailureHandler handler = new RestAuthenticationFailureHandler()
        HttpServletRequest request = new MockHttpServletRequest()
        HttpServletResponse response = new MockHttpServletResponse()
        AuthenticationException exception = new TokenNotFoundException('n/a')

        when:
        handler.statusCode = statusCode
        handler.onAuthenticationFailure(request, response, exception)

        then:
        response.status == statusCode

        where:
        statusCode << [401, 403]
	}
}
