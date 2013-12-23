package com.odobo.grails.plugin.springsecurity.rest.token.storage

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * GORM implementation for token storage. It will look
 */
class GormTokenStorageService implements TokenStorageService, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    UserDetailsService userDetailsService

    @Override
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.tokenRepository.tokenDomainClassName
        String tokenValuePropertyName = conf.rest.tokenRepository.tokenValuePropertyName
        String usernamePropertyName = conf.rest.tokenRepository.usernamePropertyName
        def dc = grailsApplication.getDomainClass(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class")
        }

        Class<?> tokenClass = dc.clazz

        tokenClass.withTransaction { status ->
            def existingToken = tokenClass.findWhere((tokenValuePropertyName): tokenValue)

            if (existingToken) {
                def username = existingToken."${usernamePropertyName}"
                return userDetailsService.loadUserByUsername(username)
            }
        }
    }

    @Override
    void storeToken(String tokenValue, UserDetails details) {

    }
}
