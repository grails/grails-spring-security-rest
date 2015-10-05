package grails.plugin.springsecurity.rest.error

import org.springframework.security.core.userdetails.UsernameNotFoundException

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

/**
 * Error handler that's backwardly compatible with the behaviour that was embedded within the callback action of
 * RestOauthController up to and including version 1.5.2
 */
class DefaultCallbackErrorHandler implements CallbackErrorHandler {

    @Override
    Map convert(Exception e) {

        Map params = [:]

        if (e instanceof UsernameNotFoundException) {
            params.error = FORBIDDEN.value()

        } else {
            params.error = e.cause?.hasProperty('code') ? e.cause.code : INTERNAL_SERVER_ERROR.value()
        }

        // Add the error message under the keys 'error_description' and 'message' - the former for compatibility with
        // the RFC and the latter for backwards compatibility with plugin versions <= 1.5.2
        params.error_description = params.message = e.message ?: ''
        params.error_code= e.cause ? e.cause.class.simpleName : e.class.simpleName
        params
    }
}
