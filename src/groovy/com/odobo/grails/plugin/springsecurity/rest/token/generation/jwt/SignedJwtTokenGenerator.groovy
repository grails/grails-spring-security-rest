package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j

/**
 * Generates JWT's protected using HMAC with SHA-256
 */
@Slf4j
class SignedJwtTokenGenerator implements TokenGenerator {

    String jwtSecret

    Integer expiration

    @Override
    String generateToken(Object principal) {
        JWSSigner signer = new MACSigner(jwtSecret)
        JWTClaimsSet claimsSet = new JWTClaimsSet()
        claimsSet.setSubject(principal.username)

        Date now = new Date()
        claimsSet.setIssueTime(now)
        use(TimeCategory) {
            claimsSet.setExpirationTime(now + expiration.seconds)
        }

        claimsSet.setCustomClaim('roles', principal.authorities?.collect{ it.role })

        log.debug "Generated claim set: ${claimsSet.toJSONObject().toString()}"

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)

        return signedJWT.serialize()
    }

}
