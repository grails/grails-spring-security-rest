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
package grails.plugin.springsecurity.rest.token.storage.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jwt.JWT
import grails.plugin.springsecurity.rest.JwtService
import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

import java.text.ParseException

/**
 * Re-hydrates JWT's with HMAC protection or JWE encryption
 */
@Slf4j
class JwtTokenStorageService implements TokenStorageService {

    JwtService jwtService

    @Override
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        Date now = new Date()
        try {
            JWT jwt = jwtService.parse(tokenValue)

            if (jwt.JWTClaimsSet.expirationTime?.before(now)) {
                throw new TokenNotFoundException("Token ${tokenValue} has expired")
            }

            def roles = jwt.JWTClaimsSet.getStringArrayClaim('roles')?.collect { new SimpleGrantedAuthority(it) }

            log.debug "Successfully verified JWT"

            log.debug "Trying to deserialize the principal object"
            try {
                UserDetails details = JwtService.deserialize(jwt.JWTClaimsSet.getCustomClaim('principal')?.toString())
                log.debug "UserDetails deserialized: ${details}"
                if (details) {
                    return details
                }
            } catch (exception) {
                log.debug(exception.message)
            }

            log.debug "Returning a org.springframework.security.core.userdetails.User instance"
            return new User(jwt.JWTClaimsSet.subject, 'N/A', roles)
        } catch (ParseException pe) {
            throw new TokenNotFoundException("Token ${tokenValue} is not valid")
        } catch (JOSEException je) {
            throw new TokenNotFoundException("Token ${tokenValue} has an invalid signature")
        }
    }

    @Override
    void storeToken(String tokenValue, UserDetails principal) {
        log.debug "Nothing to store as this is a stateless implementation"
    }

    @Override
    void removeToken(String tokenValue) throws TokenNotFoundException {
        log.debug "Nothing to remove as this is a stateless implementation"
        throw new TokenNotFoundException("Token ${tokenValue} cannot be removed as this is a stateless implementation")
    }

}
