/* Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.rest.oauth

import grails.core.DefaultGrailsApplication
import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.config.PropertySourcesConfig
import org.grails.spring.GrailsApplicationContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

//tag::class[]
/**
 * Builds an {@link OauthUser}. Delegates to the default {@link UserDetailsService#loadUserByUsername(java.lang.String)}
 * where the username passed is {@link UserProfile#getId()}.
 *
 * If the user is not found, it will create a new one with the the default roles.
 */
@Slf4j
@CompileStatic
class DefaultOauthUserDetailsService implements OauthUserDetailsService {

    @Delegate
    UserDetailsService userDetailsService

    UserDetailsChecker preAuthenticationChecks

    OauthUser loadUserByUserProfile(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles)
            throws UsernameNotFoundException {

        String userDomainClass = userDomainClassName()
        if ( !userDomainClass ) {
            return instantiateOauthUser(userProfile, defaultRoles)
        }
        loadUserByUserProfileWhenUserDomainClassIsSet(userProfile, defaultRoles)
    }

    OauthUser loadUserByUserProfileWhenUserDomainClassIsSet(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles) {
        OauthUser oauthUser
        try {
            log.debug "Trying to fetch user details for user profile: ${userProfile}"
            UserDetails userDetails = userDetailsService.loadUserByUsername userProfile.id

            log.debug "Checking user details with ${preAuthenticationChecks.class.name}"
            preAuthenticationChecks?.check(userDetails)

            Collection<GrantedAuthority> allRoles = (userDetails.authorities + defaultRoles) as Collection<GrantedAuthority>
            oauthUser = new OauthUser(userDetails.username, userDetails.password, allRoles, userProfile)
        } catch (UsernameNotFoundException unfe) {
            log.debug "User not found. Creating a new one with default roles: ${defaultRoles}"
            oauthUser = instantiateOauthUser(userProfile, defaultRoles)
        }
        oauthUser
    }

    OauthUser instantiateOauthUser(CommonProfile userProfile, Collection<GrantedAuthority> defaultRoles) {
        new OauthUser(userProfile.id, 'N/A', defaultRoles, userProfile)
    }

    @CompileDynamic
    String userDomainClassName() {

        SpringSecurityUtils.getSecurityConfig()?.get('userLookup')?.get('userDomainClassName')
    }

}
//end::class[]