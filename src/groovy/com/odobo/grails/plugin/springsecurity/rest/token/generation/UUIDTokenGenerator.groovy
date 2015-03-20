package com.odobo.grails.plugin.springsecurity.rest.token.generation

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import org.springframework.security.core.userdetails.UserDetails

/**
 * Uses {@link UUID} to generate tokens.
 */
class UUIDTokenGenerator implements TokenGenerator {

    /**
     * Generates a UUID based token
     *
     * @return a String token of 32 alphanumeric characters.
     */
    AccessToken generateAccessToken(UserDetails principal) {
        String token = UUID.randomUUID().toString().replaceAll('-', '')
        return new AccessToken(principal, principal.authorities, token)
    }

}
