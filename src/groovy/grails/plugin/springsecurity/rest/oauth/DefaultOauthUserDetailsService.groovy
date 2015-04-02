/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package grails.plugin.springsecurity.rest.oauth

import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Builds an {@link OauthUser}. Delegates to the default {@link UserDetailsService#loadUserByUsername(java.lang.String)}
 * where the username passed is {@link UserProfile#getId()}. If the user is not found, it will create a new one with
 * the the default roles.
 */
@Slf4j
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    @Delegate
    UserDetailsService userDetailsService

    OauthUser loadUserByUserProfile(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles)
            throws UsernameNotFoundException {
        UserDetails userDetails
        OauthUser oauthUser

        try {
            log.debug "Trying to fetch user details for user profile: ${userProfile}"
            userDetails = userDetailsService.loadUserByUsername userProfile.id
            Collection<GrantedAuthority> allRoles = userDetails.authorities + defaultRoles
            oauthUser = new OauthUser(userDetails.username, userDetails.password, allRoles, userProfile)
        } catch (UsernameNotFoundException unfe) {
            log.debug "User not found. Creating a new one with default roles: ${defaultRoles}"
            oauthUser = new OauthUser(userProfile.id, 'N/A', defaultRoles, userProfile)
        }
        return oauthUser
    }

}
