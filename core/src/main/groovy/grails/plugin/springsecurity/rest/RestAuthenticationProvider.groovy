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
package grails.plugin.springsecurity.rest

import com.nimbusds.jwt.JWT
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * Authenticates a request based on the token passed. This is called by {@link RestTokenValidationFilter}.
 */
@Slf4j
class RestAuthenticationProvider implements AuthenticationProvider {

    TokenStorageService tokenStorageService

    Boolean useJwt
    JwtService jwtService

    /**
     * Returns an authentication object based on the token value contained in the authentication parameter. To do so,
     * it uses a {@link TokenStorageService}.
     * @throws AuthenticationException
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Assert.isInstanceOf(AccessToken, authentication, "Only AccessToken is supported")
        AccessToken authenticationRequest = authentication as AccessToken
        AccessToken authenticationResult = new AccessToken(authenticationRequest.accessToken)

        if (authenticationRequest.accessToken) {
            log.debug "Trying to validate token ${authenticationRequest.accessToken}"
            UserDetails userDetails = tokenStorageService.loadUserByToken(authenticationRequest.accessToken) as UserDetails

            Integer expiration = null
            if (useJwt) {
                Date now = new Date()
                JWT jwt = jwtService.parse(authenticationRequest.accessToken)
                Date expiry = jwt.JWTClaimsSet.expirationTime

                log.debug "Now is ${now} and token expires at ${expiry}"

                TimeDuration timeDuration = TimeCategory.minus(expiry, now)
                expiration = Math.round(timeDuration.toMilliseconds() / 1000)
                log.debug "Expiration: ${expiration}"
            }

            authenticationResult = new AccessToken(userDetails, userDetails.authorities, authenticationRequest.accessToken, null, expiration?:null)
            log.debug "Authentication result: ${authenticationResult}"
        }

        return authenticationResult
    }

    boolean supports(Class<?> authentication) {
        return AccessToken.isAssignableFrom(authentication)
    }
}
