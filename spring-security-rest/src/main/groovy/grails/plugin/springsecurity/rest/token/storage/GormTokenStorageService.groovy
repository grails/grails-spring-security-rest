/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package grails.plugin.springsecurity.rest.token.storage

import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.springframework.security.core.userdetails.UserDetails
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

    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def conf = SpringSecurityUtils.securityConfig
        String usernamePropertyName = conf.rest.token.storage.gorm.usernamePropertyName
        def existingToken = findExistingToken(tokenValue)

        if (existingToken) {
            def username = existingToken."${usernamePropertyName}"
            return userDetailsService.loadUserByUsername(username)
        }

        throw new TokenNotFoundException("Token ${tokenValue} not found")

    }

    void storeToken(String tokenValue, UserDetails principal) {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        String tokenValuePropertyName = conf.rest.token.storage.gorm.tokenValuePropertyName
        String usernamePropertyName = conf.rest.token.storage.gorm.usernamePropertyName
        def dc = grailsApplication.getClassForName(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class ")
        }

        dc.withTransaction { status ->
            def newTokenObject = dc.newInstance((tokenValuePropertyName): tokenValue, (usernamePropertyName): principal.username)
            newTokenObject.save()
        }
    }

    void removeToken(String tokenValue) throws TokenNotFoundException {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        def existingToken = findExistingToken(tokenValue)
        if (existingToken) {
            def dc = grailsApplication.getClassForName(tokenClassName)
            dc.withTransaction() {
                existingToken.delete()
            }
        } else {
            throw new TokenNotFoundException("Token ${tokenValue} not found")
        }

    }

    private findExistingToken(String tokenValue) {
        def conf = SpringSecurityUtils.securityConfig
        String tokenClassName = conf.rest.token.storage.gorm.tokenDomainClassName
        String tokenValuePropertyName = conf.rest.token.storage.gorm.tokenValuePropertyName
        def dc = grailsApplication.getClassForName(tokenClassName)

        //TODO check at startup, not here
        if (!dc) {
            throw new IllegalArgumentException("The specified token domain class '$tokenClassName' is not a domain class")
        }

        dc.withTransaction() { status ->
            return dc.findWhere((tokenValuePropertyName): tokenValue)
        }
    }

}
