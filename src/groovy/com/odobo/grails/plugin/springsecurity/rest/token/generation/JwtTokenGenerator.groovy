package com.odobo.grails.plugin.springsecurity.rest.token.generation

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import groovy.time.TimeCategory

/**
 * Created by mariscal on 28/1/15.
 */
class JwtTokenGenerator implements TokenGenerator {

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
        claimsSet.setIssuer("https://c2id.com")

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)

        return signedJWT.serialize()
    }

}
