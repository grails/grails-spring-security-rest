package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Created by mariscal on 23/12/13.
 */
class DefaultRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {

    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        UserDetails userDetails = restAuthenticationToken.details

        def result = [
            username: userDetails.username,
            token: restAuthenticationToken.tokenValue,
            roles: userDetails.authorities.collect {GrantedAuthority role -> role.authority }]

        return (result as JSON).toString()
    }
}
