package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.util.Assert

/**
 * Authenticates a request based on the token passed. This is called by {@link RestTokenValidationFilter}.
 */
class RestAuthenticationProvider implements AuthenticationProvider {

    TokenStorageService tokenStorageService

    /**
     * Returns an authentication object based on the token value contained in the authentication parameter. To do so,
     * it uses a {@link TokenStorageService}.
     * @throws AuthenticationException
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Assert.isInstanceOf(RestAuthenticationToken, authentication, "Only RestAuthenticationToken is supported")
        RestAuthenticationToken authenticationRequest = authentication
        RestAuthenticationToken authenticationResult = new RestAuthenticationToken(authenticationRequest.tokenValue)

        if (authenticationRequest.tokenValue) {
            def userDetails = tokenStorageService.loadUserByToken(authenticationRequest.tokenValue)
            authenticationResult = new RestAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, authenticationRequest.tokenValue)
        }

        return authenticationResult
    }

    boolean supports(Class<?> authentication) {
        return RestAuthenticationToken.isAssignableFrom(authentication)
    }
}
