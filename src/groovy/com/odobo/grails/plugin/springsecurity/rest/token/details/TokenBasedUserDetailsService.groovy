package com.odobo.grails.plugin.springsecurity.rest.token.details

import org.springframework.security.core.userdetails.UserDetails

/**
 * TODO: write doc
 */
interface TokenBasedUserDetailsService {

    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException


}
