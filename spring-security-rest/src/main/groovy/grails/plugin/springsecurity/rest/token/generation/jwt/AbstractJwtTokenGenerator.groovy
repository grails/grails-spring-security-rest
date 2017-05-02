/*
 * Copyright 2013-2016 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
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

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.security.core.userdetails.UserDetails

@Slf4j
@CompileStatic
abstract class AbstractJwtTokenGenerator implements TokenGenerator {

    Integer defaultExpiration

    JwtTokenStorageService jwtTokenStorageService

    List<CustomClaimProvider> customClaimProviders

    @Override
    AccessToken generateAccessToken(UserDetails details) {
        if(log.debugEnabled) log.debug "Generating an access token with default expiration: ${this.defaultExpiration}"
        generateAccessToken(details, this.defaultExpiration)
    }

    @Override
    AccessToken generateAccessToken(UserDetails details, Integer expiration) {
        generateAccessToken(details, true, expiration)
    }

    AccessToken generateAccessToken(UserDetails details, boolean withRefreshToken, Integer expiration = this.defaultExpiration) {
        if(log.debugEnabled) log.debug "Serializing the principal received"

        JWTClaimsSet.Builder builder = generateClaims(details,  expiration)

        if(log.debugEnabled) log.debug "Generating access token..."
        JWT accessTokenJwt = generateAccessToken(builder.build())
        String accessToken = accessTokenJwt.serialize()

        JWT refreshTokenJwt
        String refreshToken
        if (withRefreshToken) {
            if(log.debugEnabled) log.debug "Generating refresh token..."
            refreshTokenJwt = generateRefreshToken(details,  expiration)
            refreshToken = refreshTokenJwt.serialize()
        }

        return new AccessToken(details, details.authorities, accessToken, refreshToken, expiration, accessTokenJwt, refreshTokenJwt)
    }

    @CompileDynamic
    JWTClaimsSet.Builder generateClaims(UserDetails details, Integer expiration) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
        builder.subject(details.username)

        Date now = new Date()
        builder.issueTime(now)

        if (expiration) {
            if(log.debugEnabled) log.debug "Setting expiration to ${expiration}"
            use(TimeCategory) {
                builder.expirationTime(now + expiration.seconds)
            }
        }

        customClaimProviders.each { CustomClaimProvider customClaimProvider ->
            customClaimProvider.provideCustomClaims(builder, details, expiration)
        }

        if(log.debugEnabled) log.debug "Generated claim set: ${builder.build().toJSONObject().toString()}"
        return builder
    }

    protected abstract JWT generateAccessToken(JWTClaimsSet claimsSet)

    protected JWT generateRefreshToken(UserDetails principal, Integer expiration) {
        JWTClaimsSet.Builder builder = generateClaims(principal, expiration)
        builder.expirationTime(null)

        return generateAccessToken(builder.build())
    }
}
