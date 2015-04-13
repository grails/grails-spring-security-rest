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
import org.apache.commons.lang3.SerializationUtils
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

@Slf4j
abstract class AbstractJwtTokenGenerator implements TokenGenerator {

    Integer expiration

    JwtTokenStorageService jwtTokenStorageService

    @Override
    AccessToken generateAccessToken(UserDetails details) {
        generateAccessToken(details, true)
    }

    AccessToken generateAccessToken(UserDetails details, boolean withRefreshToken) {
        JWTClaimsSet claimsSet = generateClaims(details)

        log.debug "Generating access token..."
        String accessToken = generateAccessToken(claimsSet)

        String refreshToken
        if (withRefreshToken) {
            log.debug "Generating refresh token..."
            refreshToken = generateRefreshToken(accessToken)
        }

        return new AccessToken(details, details.authorities, accessToken, refreshToken, expiration)
    }

    JWTClaimsSet generateClaims(UserDetails details) {
        JWTClaimsSet claimsSet = new JWTClaimsSet()
        claimsSet.setSubject(details.username)

        Date now = new Date()
        claimsSet.setIssueTime(now)
        use(TimeCategory) {
            claimsSet.setExpirationTime(now + expiration.seconds)
        }

        claimsSet.setCustomClaim('roles', details.authorities?.collect { it.authority })

        log.debug "Serializing the principal received"
        try {
            String serializedPrincipal = SerializationUtils.serialize(details)?.encodeBase64()
            claimsSet.setCustomClaim('principal', serializedPrincipal)
        } catch (exception) {
            log.debug(exception.message)
            log.debug "The principal class (${details.class}) is not serializable. Object: ${details}"
        }

        log.debug "Generated claim set: ${claimsSet.toJSONObject().toString()}"
        return claimsSet
    }

    protected abstract String generateAccessToken(JWTClaimsSet claimsSet)

    protected String generateRefreshToken(String accessToken) {
        User principal = jwtTokenStorageService.loadUserByToken(accessToken) as User
        JWTClaimsSet claimsSet = generateClaims(principal)
        claimsSet.expirationTime = null

        return generateAccessToken(claimsSet)
    }
}
