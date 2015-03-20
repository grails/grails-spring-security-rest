package com.odobo.grails.plugin.springsecurity.rest.token.storage.jwt

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.test.mixin.TestFor
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Specification

/**
 * Created by @marcos-carceles on 03/03/15.
 */
@TestFor(JwtTokenStorageService)
class JwtTokenStorageServiceSpec extends Specification {

    JwtTokenStorageService service
    SignedJwtTokenGenerator tokenGenerator

    void setup() {
        service = new JwtTokenStorageService(jwtSecret: 'fooo'*8)
        tokenGenerator = new SignedJwtTokenGenerator(jwtSecret: service.jwtSecret, expiration: 60, jwtTokenStorageService: service)
        tokenGenerator.afterPropertiesSet()
    }

    def "JWT Token signatures are verified"() {
        given:
        AccessToken accessToken = tokenGenerator.generateAccessToken(new User('testUser', 'testPassword', []))

        when:
        UserDetails user = service.loadUserByToken(accessToken.accessToken)

        then:
        user.username == 'testUser'

        when:
        service.loadUserByToken(accessToken.accessToken.replaceAll(/...$/,'bar'))

        then:
        thrown(TokenNotFoundException)

    }
}
