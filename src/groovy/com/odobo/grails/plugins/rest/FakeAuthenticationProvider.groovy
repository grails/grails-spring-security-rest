package com.odobo.grails.plugins.rest

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

/**
 * TODO: write doc
 */
class FakeAuthenticationProvider implements AuthenticationProvider {

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authenticationToken = authentication as UsernamePasswordAuthenticationToken

        if (authenticationToken.principal.equals(authenticationToken.credentials)) {
            return authenticationToken
        } else {
            throw new BadCredentialsException()
        }
    }

    @Override
    boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication))
    }
}
