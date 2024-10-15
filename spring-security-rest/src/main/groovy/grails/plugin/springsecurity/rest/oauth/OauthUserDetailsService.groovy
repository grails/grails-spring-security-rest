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
