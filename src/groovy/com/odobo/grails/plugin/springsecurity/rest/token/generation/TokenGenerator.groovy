package com.odobo.grails.plugin.springsecurity.rest.token.generation

/**
 * Implementations of this interface must provide a token generation strategy
 */
public interface TokenGenerator {

    /**
     * Generates a globally unique token.
     *
     * @return a String based token.
     */
    String generateToken()

}