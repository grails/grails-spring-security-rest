package com.odobo.grails.plugin.springsecurity.rest.token.storage

import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * GORM implementation for token storage. It will look for tokens on the DB using a domain class that will contain the
 * generated token and the username associated.
 *
 * Once the username is found, it will delegate to the configured {@link UserDetailsService} for obtaining authorities
 * information.
 */
class GormTokenStorageService implements TokenStorageService, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    UserDetailsService userDetailsService

    Object loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def existingToken = findExistingToken(tokenValue)

        if (existingToken) {
            def conf = SpringSecurityUtils.securityConfig
            String usernamePropertyName = conf.rest.token.storage.gorm.usernamePropertyName
            String marker1PropertyName = conf.rest.token.storage.gorm.marker1PropertyName
            String marker2PropertyName = conf.rest.token.storage.gorm.marker2PropertyName
            def username = existingToken."${usernamePropertyName}"
            def marker1 = marker1PropertyName ? existingToken."${marker1PropertyName}" : null
            def marker2 = marker2PropertyName ? existingToken."${marker2PropertyName}" : null
            return userDetailsService.loadUserByUsername(username, marker1, marker2)
        }

        throw new TokenNotFoundException("Token ${tokenValue.mask()} not found")

    }

    void storeToken(String tokenValue, Object principal) {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        def dc = grailsApplication.getClassForName(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class ")
        }
        String tokenValuePropertyName = conf.rest.token.storage.gorm.tokenValuePropertyName
        String usernamePropertyName = conf.rest.token.storage.gorm.usernamePropertyName
        dc.withTransaction { status ->
            def newTokenObject = dc.newInstance((tokenValuePropertyName): tokenValue, (usernamePropertyName): principal.username)\
            newTokenObject.save()
        }
    }

    Object storeMarkedToken(String tokenValue, Object principal) {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        def dc = grailsApplication.getClassForName(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class ")
        }

        String tokenValuePropertyName = conf.rest.token.storage.gorm.tokenValuePropertyName
        String usernamePropertyName = conf.rest.token.storage.gorm.usernamePropertyName
        String marker1PropertyName = conf.rest.token.storage.gorm.marker1PropertyName
        String marker2PropertyName = conf.rest.token.storage.gorm.marker2PropertyName
        dc.withTransaction { status ->
            def newTokenObject = dc.newInstance((tokenValuePropertyName): tokenValue, (usernamePropertyName): principal.username)
            if (marker1PropertyName) newTokenObject."${marker1PropertyName}" = principal."${marker1PropertyName}"
            if (marker2PropertyName) newTokenObject."${marker2PropertyName}" = principal."${marker2PropertyName}"
            newTokenObject.save()
            return newTokenObject
        }
    }

    void removeToken(String tokenValue) throws TokenNotFoundException {
        def existingToken = findExistingToken(tokenValue, false)

        if (existingToken) {
            existingToken.delete()
        } else {
            throw new TokenNotFoundException("Token ${tokenValue.mask()} not found")
        }

    }

    private findExistingToken(String tokenValue, Boolean readOnly = true) {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        def dc = grailsApplication.getClassForName(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class")
        }

        String tokenValuePropertyName = conf.rest.token.storage.gorm.tokenValuePropertyName
        dc.withNewTransaction { status ->
            return dc.findWhere((tokenValuePropertyName): tokenValue)
        }
    }

}
