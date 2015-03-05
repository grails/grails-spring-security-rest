package com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken
import groovy.util.logging.Slf4j
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

/**
 * Generates RSA-encrypted JWT's
 */
@Slf4j
class EncryptedJwtTokenGenerator extends AbstractJwtTokenGenerator {

    RSAKeyProvider keyProvider

    @Override
    protected String generateRefreshToken(String accessToken) {
        User principal = jwtTokenStorageService.loadUserByToken(accessToken) as User
        JWTClaimsSet claimsSet = generateClaims(principal)
        claimsSet.expirationTime = null

        return generateAccessToken(claimsSet)
    }

    @Override
    protected String generateAccessToken(JWTClaimsSet claimsSet) {
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A256GCM)

        // Create the encrypted JWT object
        EncryptedJWT jwt = new EncryptedJWT(header, claimsSet)

        // Create an encrypter with the specified public RSA key
        RSAEncrypter encrypter = new RSAEncrypter(keyProvider.publicKey)

        // Do the actual encryption
        jwt.encrypt(encrypter)

        // Serialise to JWT compact form
        return jwt.serialize()
    }

}
