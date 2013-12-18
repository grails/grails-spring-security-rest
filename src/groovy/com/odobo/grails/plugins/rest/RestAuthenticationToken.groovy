package com.odobo.grails.plugins.rest

import groovy.transform.CompileStatic
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Holds the API token, passed by the client via a custom HTTP header
 */
@CompileStatic
class RestAuthenticationToken extends AbstractAuthenticationToken {

    String tokenValue

    RestAuthenticationToken(String tokenValue) {
        super(null)
        this.tokenValue = tokenValue
    }


    @Override
    public Object getCredentials() {
        ""
    }

    @Override
    Object getPrincipal() {
        this.tokenValue
    }
}
