package com.odobo.grails.plugin.springsecurity.rest.token

import com.odobo.grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.generation.UUIDTokenGenerator
import spock.lang.Specification
import spock.lang.Unroll

class TokenGeneratorSpec extends Specification {

    @Unroll
    void "#generator.class.name generates tokens with 32 characters"() {

        when:
        def token = generator.generateToken()

        then:
        token.size() == 32

        where:
        generator << [new SecureRandomTokenGenerator(), new UUIDTokenGenerator()]
    }
}
