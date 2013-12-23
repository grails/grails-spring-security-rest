package com.odobo.grails.plugin.springsecurity.rest.token.storage

import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired token is not found by the {@link TokenStorageService}
 */
class TokenNotFoundException extends AuthenticationException {

    TokenNotFoundException(String msg) {
        super(msg)
    }
}
