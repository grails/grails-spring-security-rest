package com.odobo.grails.plugin.springsecurity.rest.token

import com.odobo.grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.generation.UUIDTokenGenerator
import org.apache.commons.lang.StringUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class TokenGeneratorSpec extends Specification {

    @Unroll
    void "#generator.class.name generates tokens with 32 characters"() {

        given:
        def tokens = []

        when:
        def startTime = System.nanoTime()
        10000.times {
            tokens << generator.generateToken()
        }
        def endTime = System.nanoTime()

        println "Time elapsed: ${(endTime - startTime)}"

        then:
        tokens.every { String token ->
            token.size() == 32 && StringUtils.isAlphanumeric(token)
        }

        where:
        generator << [new SecureRandomTokenGenerator(), new UUIDTokenGenerator()]
    }
}
