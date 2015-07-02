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

import grails.plugin.springsecurity.userdetails.DefaultPreAuthenticationChecks
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import spock.lang.Specification

class DefaultOauthUserDetailsServiceSpec extends Specification {

    void "it load users either existing or not"() {
        given:
        OauthUserDetailsService service = new DefaultOauthUserDetailsService()
        service.userDetailsService = new InMemoryUserDetailsManager([])
        UserDetails jimi = new User('jimi', 'jimispassword', [new SimpleGrantedAuthority('ROLE_USER'), new SimpleGrantedAuthority('ROLE_ADMIN')])
        service.userDetailsService.createUser(jimi)

        when:
        OauthUser oauthUser = service.loadUserByUserProfile([id: username] as CommonProfile, [])

        then:
        oauthUser.authorities.size() == authorities

        where:
        username    | authorities
        'test'      | 0
        'jimi'      | 2
    }

    void "when a user exists but it's disabled, it throws an exception"() {
        given:
        OauthUserDetailsService service = new DefaultOauthUserDetailsService()
        service.preAuthenticationChecks = new DefaultPreAuthenticationChecks()
        service.userDetailsService = new InMemoryUserDetailsManager([])
        UserDetails jimi = new User('jimi', 'jimispassword', false, false, false, false, [new SimpleGrantedAuthority('ROLE_USER'), new SimpleGrantedAuthority('ROLE_ADMIN')])
        service.userDetailsService.createUser(jimi)

        when:
        OauthUser oauthUser = service.loadUserByUserProfile([id: 'jimi'] as CommonProfile, [])

        then:
        thrown(AccountStatusException)

    }

}