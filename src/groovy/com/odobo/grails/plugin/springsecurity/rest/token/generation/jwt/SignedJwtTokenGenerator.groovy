package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.core.userdetails.User

/**
 * Generates JWT's protected using HMAC with SHA-256
 */
@Slf4j
class SignedJwtTokenGenerator extends AbstractJwtTokenGenerator implements InitializingBean {

    String jwtSecret

    JWSSigner signer

    @Override
    void afterPropertiesSet() throws Exception {
        signer = new MACSigner(jwtSecret)
    }

    @Override
    protected String generateRefreshToken(String accessToken) {
        User principal = jwtTokenStorageService.loadUserByToken(accessToken) as User
        JWTClaimsSet claimsSet = generateClaims(principal)
        claimsSet.expirationTime = null

        return generateAccessToken(claimsSet)
    }

    @Override
    protected String generateAccessToken(JWTClaimsSet claimsSet) {
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
        signedJWT.sign(signer)

        return signedJWT.serialize()
    }

}
