package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
class BearerTokenJsonRendererSpec extends Specification {

    def renderer = new BearerTokenJsonRenderer()

    def setupSpec() {
        def application = Mock(GrailsApplication)
        def config = new ConfigObject()
        application.getConfig() >> config
        ReflectionUtils.application = application
        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestSecurityConfig'
    }

    def "it renders valid json with an access_token and token_type property"() {
        def tokenValue = "abcdefghijklmnopqrstuvwxyz1234567890"
        def authorities = []
        def principal = new User( 'test', 'test', authorities )

        def token = new RestAuthenticationToken( principal, null, null, tokenValue )

        when:
        def result = renderer.generateJson( token )
        def json = JSON.parse( result )

        then:

        result.size() > 0
        json.access_token == tokenValue
        json.token_type == 'Bearer'
    }

}
