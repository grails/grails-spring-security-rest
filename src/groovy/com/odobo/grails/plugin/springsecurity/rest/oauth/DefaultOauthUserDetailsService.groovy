package com.odobo.grails.plugin.springsecurity.rest.oauth

import org.pac4j.core.profile.UserProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Builds an {@link OauthUser}. Delegates to the default {@link UserDetailsService#loadUserByUsername(java.lang.String)}
 * where the username passed is {@link UserProfile#getId()}.
 */
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    @Delegate
    UserDetailsService userDetailsService

    OauthUser loadUserByUserProfile(UserProfile userProfile, Collection<GrantedAuthority> defaultRoles) {
        UserDetails userDetails
        OauthUser oauthUser

        try {
            userDetails = userDetailsService.loadUserByUsername userProfile.id
            Collection<GrantedAuthority> allRoles = userDetails.authorities + defaultRoles
            oauthUser = new OauthUser(userDetails.username, userDetails.password, allRoles, userProfile)
        } catch (exception) {
            oauthUser = new OauthUser(userProfile.id, 'N/A', defaultRoles, userProfile)
        }
        return oauthUser
    }


}
