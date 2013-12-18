package com.odobo.grails.plugins.rest

import grails.converters.JSON
import groovy.transform.CompileStatic
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Holds the API token, passed by the client via a custom HTTP header
 */
@CompileStatic
class RestAuthenticationToken extends UsernamePasswordAuthenticationToken {

    String tokenValue

    RestAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String tokenValue) {
        super(principal, credentials, authorities)
        this.tokenValue = tokenValue
    }

    String toString() {
        def result = [:]
        result.username = principal.toString()
        result.token = tokenValue

        return result as JSON
    }

}
