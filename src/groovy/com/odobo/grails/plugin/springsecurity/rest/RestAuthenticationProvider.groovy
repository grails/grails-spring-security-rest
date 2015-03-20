package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * Authenticates a request based on the token passed. This is called by {@link RestTokenValidationFilter}.
 */
@Slf4j
class RestAuthenticationProvider implements AuthenticationProvider {

    TokenStorageService tokenStorageService

    /**
     * Returns an authentication object based on the token value contained in the authentication parameter. To do so,
     * it uses a {@link TokenStorageService}.
     * @throws AuthenticationException
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Assert.isInstanceOf(AccessToken, authentication, "Only AccessToken is supported")
        AccessToken authenticationRequest = authentication as AccessToken
        AccessToken authenticationResult = new AccessToken(authenticationRequest.accessToken)

        if (authenticationRequest.accessToken) {
            log.debug "Trying to validate token ${authenticationRequest.accessToken}"
            UserDetails userDetails = tokenStorageService.loadUserByToken(authenticationRequest.accessToken) as UserDetails

            authenticationResult = new AccessToken(userDetails, userDetails.authorities, authenticationRequest.accessToken)
            log.debug "Authentication result: ${authenticationResult}"
        }

        return authenticationResult
    }

    boolean supports(Class<?> authentication) {
        return AccessToken.isAssignableFrom(authentication)
    }
}
