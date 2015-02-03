package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j

@Slf4j
abstract class AbstractJwtTokenGenerator {

    Integer expiration

    JWTClaimsSet generateClaims(principal) {
        JWTClaimsSet claimsSet = new JWTClaimsSet()
        claimsSet.setSubject(principal.username)

        Date now = new Date()
        claimsSet.setIssueTime(now)
        use(TimeCategory) {
            claimsSet.setExpirationTime(now + expiration.seconds)
        }

        claimsSet.setCustomClaim('roles', principal.authorities?.collect { it.role })

        log.debug "Generated claim set: ${claimsSet.toJSONObject().toString()}"
        return claimsSet
    }
}
