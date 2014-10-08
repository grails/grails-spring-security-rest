package co.tpaga.grails.plugin.springsecurity.rest.apiKey.storage

import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * GORM implementation for Api Key storage. It will look for Api Keys on the DB using a domain class that will contain the
 * generated Api Key and the userDomainClassName associated.
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
        //TODO Add support for an Active property in the Api Key Domain Class and add the validation.
        dc.withTransaction { status ->
            return dc.findWhere((apiKeyValuePropertyName): apiKeyValue)
        }
    }

}
