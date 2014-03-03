package com.odobo.grails.plugin.springsecurity.rest.credentials

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.ReflectionUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Specification of all the credentials extractors
 */
class CredentialsExtractorTestSpec extends Specification {
    def config
    def setup(){
        def application = Mock(GrailsApplication)
        config = new ConfigObject()
        application.getConfig() >> config
        ReflectionUtils.application = application
    }

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

    void "json credentials changed on the configuration"(){

        given:
        def request = new GrailsMockHttpServletRequest()
        request.json = '{"new_username": "foo", "new_password": "bar"}'
        def extractor = new DefaultJsonPayloadCredentialsExtractor()

        and: "Spring security configuration"
        SpringSecurityUtils.securityConfig.rest.token.json.usernamePropertyName = "new_username"
        SpringSecurityUtils.securityConfig.rest.token.json.passwordPropertyName = "new_password"

        when:
        def token = extractor.extractCredentials(request)

        then:
        token.principal == 'foo'
        token.credentials == 'bar'

    }

}
