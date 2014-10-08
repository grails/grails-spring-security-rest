package co.tpaga.grails.plugin.springsecurity.rest.apiKey.storage

import groovy.transform.InheritConstructors
import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired Api Key is not found by the {@link co.tpaga.grails.plugin.springsecurity.rest.apiKey.storage.ApiKeyStorageService}
 */
@InheritConstructors
class ApiKeyNotFoundException extends AuthenticationException {}
