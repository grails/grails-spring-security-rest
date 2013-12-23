package com.odobo.grails.plugin.springsecurity.rest.token.validator

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.util.Assert

/**
 * GORM-based {@link AuthenticationProvider} implementation to validate tokens
 */
class GormTokenAuthenticationProvider implements AuthenticationProvider, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    /** To load a {@link org.springframework.security.core.userdetails.UserDetails} object */
    TokenStorageService userDetailsService

    /**
     * Uses GORM to find tokens and thus authenticating users
     *
     * @throws AuthenticationException if authentication fails.
     */
    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(RestAuthenticationToken.class, authentication, "Only RestAuthenticationToken is supported")

        RestAuthenticationToken authenticationRequest = (RestAuthenticationToken) authentication

        RestAuthenticationToken authenticationResult = new RestAuthenticationToken(authenticationRequest.tokenValue)

        if (authenticationRequest.tokenValue) {

            def conf = SpringSecurityUtils.securityConfig
            String tokenClassName = conf.rest.tokenRepository.tokenDomainClassName
            String tokenValuePropertyName = conf.rest.tokenRepository.tokenValuePropertyName
            def dc = grailsApplication.getDomainClass(tokenClassName)

            //TODO check at startup, not here
            if (!dc) {
                throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class")
            }

            Class<?> tokenClass = dc.clazz

            tokenClass.withTransaction { status ->
                def existingToken = tokenClass.findWhere((tokenValuePropertyName): authenticationRequest.tokenValue)

                if (existingToken) {
                    def userDetails = userDetailsService.loadUserByToken(authenticationRequest.tokenValue)
                    authenticationResult = new RestAuthenticationToken(userDetails.username, userDetails.password, userDetails.authorities, authenticationRequest.tokenValue)
                }
            }

        }

        return authenticationResult

    }

    @Override
    boolean supports(Class<?> authentication) {
        return (RestAuthenticationToken.class.isAssignableFrom(authentication))
    }

}
