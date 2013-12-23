package com.odobo.grails.plugin.springsecurity.rest.token.validator

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import net.spy.memcached.MemcachedClient
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.util.Assert

/**
 * TODO: write doc
 */
class MemcachedTokenAuthenticationProvider implements AuthenticationProvider {

    MemcachedClient memcachedClient

    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(RestAuthenticationToken.class, authentication, "Only RestAuthenticationToken is supported")

        RestAuthenticationToken authenticationRequest = (RestAuthenticationToken) authentication

        RestAuthenticationToken authenticationResult = new RestAuthenticationToken(authenticationRequest.tokenValue)

        if (authenticationRequest.tokenValue) {

        }

        return authenticationResult
    }

    @Override
    boolean supports(Class<?> aClass) {
        return (RestAuthenticationToken.class.isAssignableFrom(authentication))
    }
}
