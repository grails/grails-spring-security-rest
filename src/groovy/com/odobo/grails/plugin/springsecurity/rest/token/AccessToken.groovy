package com.odobo.grails.plugin.springsecurity.rest.token

import groovy.transform.ToString
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.SpringSecurityCoreVersion
import org.springframework.security.core.userdetails.UserDetails

/**
 * TODO: write doc
 */
@ToString(includeNames = true, includeSuper = true, includes = ['principal', 'accessToken', 'refreshToken', 'expiration'])
class AccessToken extends AbstractAuthenticationToken {

    static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID

    String accessToken

    Integer expiration
    String refreshToken

    /** The username */
    UserDetails principal

    AccessToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
        super.setAuthenticated(true)
    }

    AccessToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities, String accessToken, String refreshToken = null, Integer expiration = null) {
        this(authorities)
        this.principal = principal
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiration = expiration
    }

    AccessToken(String accessToken, String refreshToken = null, Integer expiration = null) {
        super(null)
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.expiration = expiration
        super.setAuthenticated(false)
    }

    Object getCredentials() {
        return null
    }

    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead")
        }

        super.setAuthenticated(false)
    }

}
