package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.util.Assert

/**
 * Authenticates a request based on the token passed. This is called by the filter.
 */
class RestAuthenticationProvider implements AuthenticationProvider {

    TokenStorageService tokenStorageService

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Assert.isInstanceOf(RestAuthenticationToken.class, authentication, "Only RestAuthenticationToken is supported")
        RestAuthenticationToken authenticationRequest = (RestAuthenticationToken) authentication
        RestAuthenticationToken authenticationResult = new RestAuthenticationToken(authenticationRequest.tokenValue)

        if (authenticationRequest.tokenValue) {
            def userDetails = tokenStorageService.loadUserByToken(authenticationRequest.tokenValue)
            authenticationResult = new RestAuthenticationToken(userDetails.username, userDetails.password, userDetails.authorities, authenticationRequest.tokenValue)
        }

        return authenticationResult
    }

    @Override
    boolean supports(Class<?> authentication) {
        return (RestAuthenticationToken.class.isAssignableFrom(authentication))
    }
}
