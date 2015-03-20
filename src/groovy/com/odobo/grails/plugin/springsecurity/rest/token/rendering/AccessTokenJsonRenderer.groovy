package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken

/**
 * Generates a JSON representation of a {@link org.springframework.security.core.userdetails.UserDetails} object.
 */
public interface AccessTokenJsonRenderer {

    String generateJson(AccessToken accessToken)

}