package com.odobo.grails.plugin.springsecurity.rest.token.generation

/**
 * Uses {@link UUID} to generate tokens.
 */
class UUIDTokenGenerator implements TokenGenerator {

    /**
     * Generates a UUID based token
     *
     * @return a String token of 32 alphanumeric characters.
     */
    String generateToken() {
        return UUID.randomUUID().toString().replaceAll('-', '')
    }
}
