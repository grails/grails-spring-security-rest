package com.odobo.grails.plugin.springsecurity.rest.oauth

import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

/**
 * A {@link UserDetails} implementation that holds the {@link CommonProfile} returned by the OAuth provider
 */
class OauthUser extends User {

    CommonProfile userProfile

    OauthUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities)
    }

    OauthUser(String username, String password, Collection<? extends GrantedAuthority> authorities, CommonProfile userProfile) {
        super(username, password, authorities)
        this.userProfile = userProfile
    }

}
