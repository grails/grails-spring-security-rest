package co.tpaga.grails.plugin.springsecurity.rest.token.storage

import groovy.transform.InheritConstructors
import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired token is not found by the {@link com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService}
 */
@InheritConstructors
class ApiKeyNotFoundException extends AuthenticationException {}
