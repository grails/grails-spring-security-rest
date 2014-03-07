package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(ControllerUnitTestMixin)
class DefaultRestAuthenticationTokenJsonRendererSpec extends Specification {
    def config
    def setup(){
        def application = Mock(GrailsApplication)
        config = new ConfigObject()
        application.getConfig() >> config
        ReflectionUtils.application = application
        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestSecurityConfig'
    }

    @Unroll
    void "it renders proper JSON for a given token when the user has #roles.size() roles"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def userDetails = new User(username, password, roles)

        RestAuthenticationToken token = new RestAuthenticationToken(userDetails, password, roles, tokenValue)

        DefaultRestAuthenticationTokenJsonRenderer renderer = new DefaultRestAuthenticationTokenJsonRenderer()

        when:
        def jsonResult = renderer.generateJson(token)

        then:
        jsonResult == generatedJson

        where:
        roles                                                                       | generatedJson
        [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]   | '{"username":"john.doe","token":"1a2b3c4d","roles":["ADMIN","USER"]}'
        []                                                                          | '{"username":"john.doe","token":"1a2b3c4d","roles":[]}'
    }

    void "Render json with custom properties"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def userDetails = new User(username, password, roles)

        RestAuthenticationToken token = new RestAuthenticationToken(userDetails, password, roles, tokenValue)

        DefaultRestAuthenticationTokenJsonRenderer renderer = new DefaultRestAuthenticationTokenJsonRenderer()

        and: "Spring security configuration"
        SpringSecurityUtils.securityConfig.rest.response.usernamePropertyName = "login"
        SpringSecurityUtils.securityConfig.rest.response.tokenPropertyName = "access_token"
        SpringSecurityUtils.securityConfig.rest.response.authoritiesPropertyName = "authorities"

        when:
        def jsonResult = renderer.generateJson(token)

        then:
        jsonResult == generatedJson

        where:
        roles                                                                       | generatedJson
        [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]   | '{"login":"john.doe","access_token":"1a2b3c4d","authorities":["ADMIN","USER"]}'
        []                                                                          | '{"login":"john.doe","access_token":"1a2b3c4d","authorities":[]}'
    }


    @Issue('https://github.com/alvarosanchez/grails-spring-security-rest/issues/18')
    void "it checks if the principal is a UserDetails"() {
        given:
        def principal = 'john.doe'
        def tokenValue = '1a2b3c4d'
        RestAuthenticationToken token = new RestAuthenticationToken(principal, '', [], tokenValue)
        DefaultRestAuthenticationTokenJsonRenderer renderer = new DefaultRestAuthenticationTokenJsonRenderer()

        when:
        renderer.generateJson(token)

        then:
        thrown IllegalArgumentException
    }
}
