package com.odobo.grails.plugin.springsecurity.rest.token.bearer

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import spock.lang.Specification

/**
 * Created by Sean Brady on 12/3/14.
 */
class BearerTokenAccessDeniedHandlerSpec extends Specification {

    def bearerTokenAccessDeniedHandler = new BearerTokenAccessDeniedHandler()

    def "sets the correct bearer token header when forbidden"() {
        given:
        def response = new MockHttpServletResponse()
        def request = new MockHttpServletRequest()
        when:
        bearerTokenAccessDeniedHandler.handle(request,response,new AccessDeniedException("fake"))
        then:
        response.getHeader( 'WWW-Authenticate' ) == 'Bearer error="insufficient_scope"'
    }
}
