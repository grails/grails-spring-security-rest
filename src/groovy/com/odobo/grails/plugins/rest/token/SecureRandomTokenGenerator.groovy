package com.odobo.grails.plugins.rest.token

import groovy.transform.CompileStatic

import java.security.SecureRandom

/**
 * A {@link TokenGenerator} implementation using {@link java.security.SecureRandom}
 */
@CompileStatic
class SecureRandomTokenGenerator implements TokenGenerator {

    SecureRandom random = new SecureRandom()

    /**
     * Generates a token using {@link java.security.SecureRandom}, a cryptographically strong random number generator.
     *
     * @return a String token of 32 alphanumeric characters.
     */
    @Override
    String generateToken() {
        new BigInteger(130, random).toString(32)
    }

}
