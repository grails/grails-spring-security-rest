package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken

/**
 * Generates a JSON representation of a {@link org.springframework.security.core.userdetails.UserDetails} object.
 */
public interface RestAuthenticationTokenJsonRenderer {

    String generateJson(RestAuthenticationToken restAuthenticationToken)

}