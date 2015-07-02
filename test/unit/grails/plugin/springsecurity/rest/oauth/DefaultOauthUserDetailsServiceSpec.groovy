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