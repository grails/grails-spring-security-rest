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
package grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import grails.plugin.springsecurity.rest.JwtService
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j
import org.springframework.security.core.userdetails.UserDetails

@Slf4j
abstract class AbstractJwtTokenGenerator implements TokenGenerator {

    Integer defaultExpiration

    JwtTokenStorageService jwtTokenStorageService


    @Override
    AccessToken generateAccessToken(UserDetails details) {
        log.debug "Generating an access token with default expiration: ${this.defaultExpiration}"
        generateAccessToken(details, this.defaultExpiration)
    }

    @Override
    AccessToken generateAccessToken(UserDetails details, Integer expiration) {
        generateAccessToken(details, true, expiration)
    }

    AccessToken generateAccessToken(UserDetails details, boolean withRefreshToken, Integer expiration = this.defaultExpiration) {
        log.debug "Serializing the principal received"
        String serializedPrincipal = serializePrincipal(details)

        JWTClaimsSet claimsSet = generateClaims(details, serializedPrincipal, expiration)

        log.debug "Generating access token..."
        String accessToken = generateAccessToken(claimsSet)

        String refreshToken
        if (withRefreshToken) {
            log.debug "Generating refresh token..."
            refreshToken = generateRefreshToken(details, serializedPrincipal, expiration)
        }

        return new AccessToken(details, details.authorities, accessToken, refreshToken, expiration)
    }

    JWTClaimsSet generateClaims(UserDetails details, String serializedPrincipal, Integer expiration) {
        JWTClaimsSet claimsSet = new JWTClaimsSet()
        claimsSet.setSubject(details.username)

        log.debug "Setting expiration to ${expiration}"
        Date now = new Date()
        claimsSet.setIssueTime(now)
        use(TimeCategory) {
            claimsSet.setExpirationTime(now + expiration.seconds)
        }

        claimsSet.setCustomClaim('roles', details.authorities?.collect { it.authority })
        claimsSet.setCustomClaim('principal', serializedPrincipal)

        log.debug "Generated claim set: ${claimsSet.toJSONObject().toString()}"
        return claimsSet
    }

    protected String serializePrincipal(UserDetails principal) {
        try {
            return JwtService.serialize(principal)
        } catch (exception) {
            log.debug(exception.message)
            log.debug "The principal class (${principal.class}) is not serializable. Object: ${principal}"
            return null
        }
    }

    protected abstract String generateAccessToken(JWTClaimsSet claimsSet)

    protected String generateRefreshToken(UserDetails principal, String serializedPrincipal, Integer expiration) {
        JWTClaimsSet claimsSet = generateClaims(principal, serializedPrincipal, expiration)
        claimsSet.expirationTime = null

        return generateAccessToken(claimsSet)
    }
}
