package com.odobo.grails.plugin.springsecurity.rest.oauth

import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.profile.OAuth20Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Load users based on a {@link OAuth20Profile}
 */
interface OauthUserDetailsService extends UserDetailsService {

    /**
     * Search for a user based on his {@link OAuth20Profile}. Implementations have a chance here to define additional
     * checks, like verifying that user's email domain is valid.
     *
     * @param userProfile user's OAuth profile returned by OAuth provider.
     * @param defaultRoles
     * @return a valid {@link OauthUser}, otherwise a exception is thrown
     * @throws UsernameNotFoundException if the user is not found and/or not allowed to login based on his profile
     */
    OauthUser loadUserByUserProfile(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles) throws UsernameNotFoundException

}
