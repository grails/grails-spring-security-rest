package com.odobo.grails.plugin.springsecurity.rest.token

import com.odobo.grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.generation.UUIDTokenGenerator
import org.apache.commons.lang.StringUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import spock.lang.Specification
import spock.lang.Unroll

class TokenGeneratorSpec extends Specification {

    @Unroll
    void "#generator.class.name generates tokens with 32 characters"() {

        given:
        def tokens = []
        def userDetails = new User('foo', 'bar', [new SimpleGrantedAuthority('USER')])

        when:
        def startTime = System.nanoTime()
        10000.times {
            tokens << generator.generateAccessToken(userDetails)
        }
        def endTime = System.nanoTime()

        println "Time elapsed: ${(endTime - startTime)}"

        then:
        tokens.every { AccessToken token ->
            token.accessToken.size() == 32 && StringUtils.isAlphanumeric(token.accessToken)
        }

        where:
        generator << [new SecureRandomTokenGenerator(), new UUIDTokenGenerator()]
    }
}
