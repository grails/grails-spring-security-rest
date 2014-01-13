package com.odobo.grails.plugin.springsecurity.rest.token.generation

import org.apache.commons.lang.RandomStringUtils

import java.security.SecureRandom

/**
 * A {@link TokenGenerator} implementation using {@link java.security.SecureRandom}
 */
class SecureRandomTokenGenerator implements TokenGenerator {

    SecureRandom random = new SecureRandom()

    /**
     * Generates a token using {@link java.security.SecureRandom}, a cryptographically strong random number generator.
     *
     * @return a String token of 32 alphanumeric characters.
     */
    String generateToken() {
        String token = new BigInteger(160, this.random).toString(32)
        def tokenSize = token.size()
        if (tokenSize < 32) token += RandomStringUtils.randomAlphanumeric(32 - tokenSize)
        return token
    }

}
