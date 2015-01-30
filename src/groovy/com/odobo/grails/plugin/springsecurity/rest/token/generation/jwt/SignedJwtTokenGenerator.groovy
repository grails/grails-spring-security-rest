package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import groovy.util.logging.Slf4j

/**
 * Generates JWT's protected using HMAC with SHA-256
 */
@Slf4j
class SignedJwtTokenGenerator extends AbstractJwtTokenGenerator implements TokenGenerator {

    String jwtSecret

    @Override
    String generateToken(Object principal) {
        JWSSigner signer = new MACSigner(jwtSecret)

        JWTClaimsSet claimsSet = generateClaims(principal)

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)

        return signedJWT.serialize()
    }



}
