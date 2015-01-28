package com.odobo.grails.plugin.springsecurity.rest.token.storage

import groovy.transform.InheritConstructors
import org.springframework.security.core.AuthenticationException

/**
 * Thrown if the desired token is not found by the {@link TokenStorageService}
 */
@InheritConstructors
class TokenNotFoundException extends AuthenticationException {}
