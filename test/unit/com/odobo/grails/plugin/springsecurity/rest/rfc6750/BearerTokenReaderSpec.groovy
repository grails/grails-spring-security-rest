package com.odobo.grails.plugin.springsecurity.rest.rfc6750

import com.odobo.grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by ajbrown on 6/25/14.
 */
class BearerTokenReaderSpec extends Specification {

    def tokenReader = new BearerTokenReader()
    def request = new GrailsMockHttpServletRequest()
    def response = new MockHttpServletResponse()

    def setup() {
        //TODO, untill we get spring-test 4.0.5 or greater, there is no getParts() on MockHttpServletRequest.  We
        // just need to define it so that an exception doesn't occur when checking for parts.
        request.metaClass.getParts = { -> [] }
    }

    @Unroll
    def "token value can be read from #method request Authorization header (prefixed with 'Bearer ')"() {
        given:
        def token    = 'mytestotkenvalue'
        request.addHeader( 'Authorization', 'Bearer ' + token )
        request.method = method
        request.contentType = MediaType.TEXT_PLAIN_VALUE

        expect:
        tokenReader.findToken(request, response) == token

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

    @Unroll
    def "token value cannot be read from #method request access_token query string parameter, due to its insecurity"() {
        given:
        def token = 'mytesttokenvalue'
        request.queryString = 'access_token=' + token
        request.contentType = MediaType.TEXT_PLAIN_VALUE

        expect:
        tokenReader.findToken(request, response) == null

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

    @Unroll
    def "token value can be read from application/x-www-form-url-encoded #method request body"() {
        given:
        def token = 'mytesttokenvalue'
        request.contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
        request.addParameter( 'access_token', token )
        request.method = method

        expect:
        tokenReader.findToken(request, response) == token

        where:
        method << [ 'POST', 'PUT', 'PATCH' ]
    }

    @Unroll
    def "token value will not be read from #metod request Authorization header not prefix with 'Bearer'"() {
        given:
        def token = 'abadtokenvalue'
        request.addHeader( 'Authorization', token )
        request.method = method
        request.contentType = MediaType.TEXT_PLAIN_VALUE

        expect:
        !tokenReader.findToken(request, response)

        where:

        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

    @Unroll
    def "token value will not be read from #method request body if not form encoded"() {
        given:
        def token = 'abadtokenvalue'
        request.addParameter( 'access_token', token )
        request.contentType = MediaType.MULTIPART_FORM_DATA_VALUE
        request.method = method

        expect:
        !tokenReader.findToken(request, response)

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

    @Unroll
    def "token value will not be read from request body if request method is #method"() {
        given:
        def token = 'mytesttokenvalue'
        request.contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
        request.addParameter( 'access_token', token )
        request.method = method

        expect:
        !tokenReader.findToken(request, response)

        where:
        method << [ 'GET' ]
    }

    @Unroll
    def "when the mediatype is null still works"() {
        given:
        request.contentType = null

        expect:
        !tokenReader.findToken(request, response)
    }

}
