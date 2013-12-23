package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Created by mariscal on 23/12/13.
 */
class DefaultRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {


    @Override
    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        def result = [:]
        UserDetails userDetails = restAuthenticationToken.details

        result.username = userDetails.username
        result.token = restAuthenticationToken.tokenValue
        result.roles = []

        userDetails.authorities.each {GrantedAuthority role ->
            result.roles << role.authority
        }

        return (result as JSON).toString()
    }
}
