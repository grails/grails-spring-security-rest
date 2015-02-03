package com.odobo.grails.plugin.springsecurity.rest.token.storage.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.odobo.grails.plugin.springsecurity.rest.token.generation.jwt.RSAKeyProvider
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

import java.text.ParseException

/**
 * Re-hydrates JWT's with HMAC protection or JWE encryption
 */
@Slf4j
class JwtTokenStorageService implements TokenStorageService {

    String jwtSecret

    RSAKeyProvider keyProvider

    @Override
    Object loadUserByToken(String tokenValue) throws TokenNotFoundException {
        Date now = new Date()
        JWT jwt
        try {
            jwt = JWTParser.parse(tokenValue)

            if (jwt instanceof  SignedJWT) {
                log.debug "Parsed an HMAC signed JWT"

                SignedJWT signedJwt = jwt as SignedJWT
                signedJwt.verify(new MACVerifier(jwtSecret))
            } else if (jwt instanceof EncryptedJWT) {
                log.debug "Parsed an RSA encrypted JWT"

                EncryptedJWT encryptedJWT = jwt as EncryptedJWT
                RSADecrypter decrypter = new RSADecrypter(keyProvider.privateKey)

                // Decrypt
                encryptedJWT.decrypt(decrypter)
            }

            if (jwt.JWTClaimsSet.expirationTime.before(now)) {
                throw new TokenNotFoundException("Token ${tokenValue} has expired")
            }

            def roles = jwt.JWTClaimsSet.getStringArrayClaim('roles')?.collect { new SimpleGrantedAuthority(it) }

            log.debug "Successfully verified JWT"
            return new User(jwt.JWTClaimsSet.subject, 'N/A', roles)
        } catch (ParseException pe) {
            throw new TokenNotFoundException("Token ${tokenValue} is not valid")
        } catch (JOSEException je) {
            throw new TokenNotFoundException("Token ${tokenValue} has an invalid signature")
        }
    }

    @Override
    void storeToken(String tokenValue, Object principal) {
        log.debug "Nothing to store as this is a stateless implementation"
    }

    @Override
    void removeToken(String tokenValue) throws TokenNotFoundException {
        log.debug "Nothing to remove as this is a stateless implementation"
        throw new TokenNotFoundException("Token ${tokenValue} cannot be removed as this is a stateless implementation")
    }
}
