package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Generates a JSON response like the following: <code>{"username":"john.doe","token":"1a2b3c4d","roles":["ADMIN","USER"]}</code>
 */
class DefaultRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {

    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        UserDetails userDetails = restAuthenticationToken.principal

        def result = [
            username: userDetails.username,
            token: restAuthenticationToken.tokenValue,
            roles: userDetails.authorities.collect {GrantedAuthority role -> role.authority }]

        return (result as JSON).toString()
    }
}
