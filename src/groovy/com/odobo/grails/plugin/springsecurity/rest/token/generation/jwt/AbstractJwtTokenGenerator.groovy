package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j
import org.springframework.security.core.userdetails.UserDetails

@Slf4j
abstract class AbstractJwtTokenGenerator implements TokenGenerator {

    Integer expiration

    JwtTokenStorageService jwtTokenStorageService

    AccessToken generateAccessToken(UserDetails details) {
        JWTClaimsSet claimsSet = generateClaims(details)

        String accessToken = generateAccessToken(claimsSet)
        String refreshToken = generateRefreshToken(accessToken)

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

        log.debug "Generated claim set: ${claimsSet.toJSONObject().toString()}"
        return claimsSet
    }

    protected abstract String generateAccessToken(JWTClaimsSet claimsSet)

    protected abstract String generateRefreshToken(String accessToken)
}
