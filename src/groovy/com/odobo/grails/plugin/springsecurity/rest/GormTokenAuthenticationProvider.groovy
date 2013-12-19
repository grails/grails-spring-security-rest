package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.details.TokenBasedUserDetailsService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * GORM-based {@link AuthenticationProvider} implementation to validate tokens
 */
class GormTokenAuthenticationProvider implements AuthenticationProvider, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    /** To load a {@link org.springframework.security.core.userdetails.UserDetails} object */
    TokenBasedUserDetailsService userDetailsService

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
                    authenticationResult = new RestAuthenticationToken(authenticationRequest.tokenValue)
                }
            }

        }

        return authenticationResult

    }

    @Override
    boolean supports(Class<?> authentication) {
        return (RestAuthenticationToken.class.isAssignableFrom(authentication))
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                         UserDetails user) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        RestAuthenticationToken result = new UsernamePasswordAuthenticationToken(principal,
                authentication.getCredentials(), authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());

        return result;
    }
}
