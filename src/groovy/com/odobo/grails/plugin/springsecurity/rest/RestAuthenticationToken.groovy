package com.odobo.grails.plugin.springsecurity.rest

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Holds the API token, passed by the client via a custom HTTP header
 */
class RestAuthenticationToken extends UsernamePasswordAuthenticationToken {

    String tokenValue

    RestAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String tokenValue) {
        super(principal, credentials, authorities)
        this.tokenValue = tokenValue
    }

    RestAuthenticationToken(String tokenValue) {
        super("N/A", "N/A")
        this.tokenValue = tokenValue
    }

    RestAuthenticationToken(String tokenValue, Collection<? extends GrantedAuthority> authorities) {
        super("N/A", "N/A")
        this.tokenValue = tokenValue
    }

}
