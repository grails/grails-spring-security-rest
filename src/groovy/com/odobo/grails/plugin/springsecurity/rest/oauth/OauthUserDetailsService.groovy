package com.odobo.grails.plugin.springsecurity.rest.oauth

import org.pac4j.core.profile.UserProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * TODO: write doc
 */
interface OauthUserDetailsService extends UserDetailsService {

    OauthUser loadUserByUserProfile(UserProfile userProfile, Collection<GrantedAuthority> defaultRoles)

}
