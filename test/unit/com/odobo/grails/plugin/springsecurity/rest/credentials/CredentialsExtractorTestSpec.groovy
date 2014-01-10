package com.odobo.grails.plugin.springsecurity.rest.credentials

import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification of all the credentials extractors
 */
class CredentialsExtractorTestSpec extends Specification {

    void "credentials can be extracted from a JSON request"() {

        given:
        def request = new GrailsMockHttpServletRequest()
        request.json = '{"username": "foo", "password": "bar"}'
        def extractor = new DefaultJsonPayloadCredentialsExtractor()

        when:
        def token = extractor.extractCredentials(request)

        then:
        token.principal == 'foo'
        token.credentials == 'bar'

    }

    void "JSON parsing handles unexpected requests"() {

        given:
        def request = new GrailsMockHttpServletRequest()
        request.json = '{"different": "format", "of": "JSON"}'
        def extractor = new DefaultJsonPayloadCredentialsExtractor()

        when:
        def token = extractor.extractCredentials(request)

        then:
        !token.principal
        !token.credentials


    }

    void "credentials can be extracted from a request params-based request"() {

        given:
        def request = new GrailsMockHttpServletRequest()
        request.parameters = [username: 'foo', password: 'bar']
        def extractor = new RequestParamsCredentialsExtractor(usernameParameter: 'username', passwordParameter: 'password')

        when:
        def token = extractor.extractCredentials(request)

        then:
        token.principal == 'foo'
        token.credentials == 'bar'

    }

}
