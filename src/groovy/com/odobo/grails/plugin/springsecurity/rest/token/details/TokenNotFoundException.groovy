package com.odobo.grails.plugin.springsecurity.rest.token.details

import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired token is not found by the {@link TokenBasedUserDetailsService}
 */
class TokenNotFoundException extends AuthenticationException {

    TokenNotFoundException(String msg) {
        super(msg)
    }
}
