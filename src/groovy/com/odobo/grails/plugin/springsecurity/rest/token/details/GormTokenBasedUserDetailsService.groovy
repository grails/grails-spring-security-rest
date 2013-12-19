package com.odobo.grails.plugin.springsecurity.rest.token.details

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.core.userdetails.UserDetails

/**
 * TODO: write doc
 */
class GormTokenBasedUserDetailsService implements TokenBasedUserDetailsService, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    @Override
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        return null
    }
}
