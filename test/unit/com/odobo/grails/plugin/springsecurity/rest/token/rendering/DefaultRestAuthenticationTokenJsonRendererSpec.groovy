package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.converters.JSON
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(ControllerUnitTestMixin)
class DefaultRestAuthenticationTokenJsonRendererSpec extends Specification {

    @Shared
    DefaultRestAuthenticationTokenJsonRenderer renderer

    def setupSpec() {
        def application = Mock(GrailsApplication)
        def config = new ConfigObject()
        application.getConfig() >> config
        ReflectionUtils.application = application
        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestSecurityConfig'

        renderer = new DefaultRestAuthenticationTokenJsonRenderer(authoritiesPropertyName: 'roles',
                                                                  tokenPropertyName: 'access_token',
                                                                  usernamePropertyName: 'username')
    }

    @Unroll
    void "it renders proper JSON for a given token when the user has #roles.size() roles"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def userDetails = new User(username, password, roles)

        RestAuthenticationToken token = new RestAuthenticationToken(userDetails, password, roles, tokenValue)

        when:
        def jsonResult = renderer.generateJson(token)

        then:
        jsonResult == generatedJson

        where:
        roles                                                                       | generatedJson
        [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]   | '{"username":"john.doe","roles":["ADMIN","USER"],"access_token":"1a2b3c4d"}'
        []                                                                          | '{"username":"john.doe","roles":[],"access_token":"1a2b3c4d"}'
    }

    void "Render JSON with custom properties"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def userDetails = new User(username, password, roles)

        RestAuthenticationToken token = new RestAuthenticationToken(userDetails, password, roles, tokenValue)

        RestAuthenticationTokenJsonRenderer customRenderer =
                new DefaultRestAuthenticationTokenJsonRenderer(authoritiesPropertyName: 'authorities',
                                                               tokenPropertyName: 'token',
                                                               usernamePropertyName: 'login')

        when:
        def jsonResult = customRenderer.generateJson(token)

        then:
        jsonResult == generatedJson

        where:
        roles                                                                       | generatedJson
        [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]   | '{"login":"john.doe","authorities":["ADMIN","USER"],"token":"1a2b3c4d"}'
        []                                                                          | '{"login":"john.doe","authorities":[],"token":"1a2b3c4d"}'


    }


    @Issue('https://github.com/alvarosanchez/grails-spring-security-rest/issues/18')
    void "it checks if the principal is a UserDetails"() {
        given:
        def principal = 'john.doe'
        def tokenValue = '1a2b3c4d'
        RestAuthenticationToken token = new RestAuthenticationToken(principal, '', [], tokenValue)

        when:
        renderer.generateJson(token)

        then:
        thrown IllegalArgumentException
    }

    @Issue('https://github.com/alvarosanchez/grails-spring-security-rest/issues/33')
    void "it renders OAuth information if the principal is an OAuthUser"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def roles = [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]

        def profile = new CommonProfile()
        profile.metaClass.with {
            getDisplayName = { "John Doe" }
            getEmail = { "john@doe.com" }
        }

        def userDetails = new OauthUser(username, password, roles, profile)

        RestAuthenticationToken token = new RestAuthenticationToken(userDetails, password, roles, tokenValue)

        when:
        def jsonResult = renderer.generateJson(token)

        then:
        jsonResult == '{"username":"john.doe","roles":["ADMIN","USER"],"access_token":"1a2b3c4d","email":"john@doe.com","displayName":"John Doe"}'
    }

    def "it renders valid Bearer tokens"() {
        given:
        def tokenValue = "abcdefghijklmnopqrstuvwxyz1234567890"
        def principal = new User('test', 'test', [])
        def token = new RestAuthenticationToken( principal, null, null, tokenValue )
        renderer.useBearerToken = true

        when:
        def result = renderer.generateJson( token )
        def json = JSON.parse( result )

        then:
        result.size()
        json.access_token == tokenValue
        json.token_type == 'Bearer'

        cleanup:
        renderer.useBearerToken = false
    }

}
