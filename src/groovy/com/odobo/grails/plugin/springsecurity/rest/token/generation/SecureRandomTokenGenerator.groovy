package com.odobo.grails.plugin.springsecurity.rest.token.generation

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
        new BigInteger(160, random).toString(32)
    }

}
