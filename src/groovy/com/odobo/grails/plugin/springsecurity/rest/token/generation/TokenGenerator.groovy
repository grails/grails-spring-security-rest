package com.odobo.grails.plugin.springsecurity.rest.token.generation

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import org.springframework.security.core.userdetails.UserDetails

/**
 * Implementations of this interface must provide a token generation strategy
 */
public interface TokenGenerator {

    /**
     * Generates a globally unique token.
     *
     * @return a String based token.
     */
    AccessToken generateAccessToken(UserDetails principal)

}