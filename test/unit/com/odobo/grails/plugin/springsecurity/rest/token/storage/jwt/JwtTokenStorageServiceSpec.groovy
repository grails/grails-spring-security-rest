package com.odobo.grails.plugin.springsecurity.rest.token.storage.jwt

import com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.test.mixin.TestFor
import org.springframework.security.core.userdetails.User
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
        tokenGenerator = new SignedJwtTokenGenerator(jwtSecret: 'fooo'*8, expiration: 60)
    }

    def "JWT Token signatures are verified"() {
        given:
        String token = tokenGenerator.generateToken(new User('testUser', 'testPassword', []))

        when:
        User user = service.loadUserByToken(token)

        then:
        user.username == 'testUser'

        when:
        service.loadUserByToken(token.replaceAll(/...$/,'bar'))

        then:
        thrown(TokenNotFoundException)

    }
}
