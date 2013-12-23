package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(ControllerUnitTestMixin)
class DefaultRestAuthenticationTokenJsonRendererSpec extends Specification {

    @Unroll
	void "it renders proper JSON for a given token when the user has #roles.size() roles"() {
        given:
        def username = 'john.doe'
        def password = 'donttellanybody'
        def tokenValue = '1a2b3c4d'
        def userDetails = new User(username, password, roles)

        RestAuthenticationToken token = new RestAuthenticationToken(username, password, roles, tokenValue)
        token.details = userDetails

        DefaultRestAuthenticationTokenJsonRenderer renderer = new DefaultRestAuthenticationTokenJsonRenderer()

        when:
        def jsonResult = renderer.generateJson(token)
        println jsonResult

        then:
        jsonResult == generatedJson

        where:
        roles                                                                       | generatedJson
        [new SimpleGrantedAuthority('USER'), new SimpleGrantedAuthority('ADMIN')]   | '{"username":"john.doe","token":"1a2b3c4d","roles":["ADMIN","USER"]}'
        []                                                                          | '{"username":"john.doe","token":"1a2b3c4d","roles":[]}'
	}


}
