package co.tpaga.grails.plugin.springsecurity.rest.token.storage

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
class GormApiKeyStorageService implements ApiKeyStorageService, GrailsApplicationAware {

    /** Dependency injection for the application. */
    GrailsApplication grailsApplication

    UserDetailsService userDetailsService

    Object loadUserByApiKey(String apiKeyValue) throws ApiKeyNotFoundException {
        def conf = SpringSecurityUtils.securityConfig
        String usernamePropertyName = conf.rest.apiKey.storage.gorm.usernamePropertyName
        def existingApiKey = findExistingApiKey(apiKeyValue)

        if (existingApiKey) {
            def username = existingApiKey."${usernamePropertyName}"
            return userDetailsService.loadUserByUsername(username)
        }

        throw new ApiKeyNotFoundException("Api Key ${apiKeyValue} not found")

    }

    private findExistingApiKey(String apiKeyValue) {
        def conf = SpringSecurityUtils.securityConfig
        String apiKeyClassName = conf.rest.apiKey.storage.gorm.apiKeyDomainClassName
        String apiKeyValuePropertyName = conf.rest.apiKey.storage.gorm.apiKeyValuePropertyName
        def dc = grailsApplication.getClassForName(apiKeyClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$apiKeyClassName' is not a domain class")
        }

        dc.withTransaction { status ->
            return dc.findWhere((apiKeyValuePropertyName): apiKeyValue)
        }
    }

}
