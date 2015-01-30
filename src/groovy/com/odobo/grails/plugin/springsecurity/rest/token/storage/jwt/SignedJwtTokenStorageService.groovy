package com.odobo.grails.plugin.springsecurity.rest.token.storage.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

import java.text.ParseException

/**
 * Re-hydrates JWT's protected using HMAC with SHA-256
 */
@Slf4j
class SignedJwtTokenStorageService implements TokenStorageService {

    String jwtSecret

    @Override
    Object loadUserByToken(String tokenValue) throws TokenNotFoundException {
        Date now = new Date()
        SignedJWT signedJWT
        try {
            signedJWT = SignedJWT.parse(tokenValue)
            signedJWT.verify(new MACVerifier(jwtSecret))

            if (signedJWT.JWTClaimsSet.expirationTime.before(now)) {
                throw new TokenNotFoundException("Token ${tokenValue} has expired")
            }

            def roles = signedJWT.JWTClaimsSet.getStringArrayClaim('roles')?.collect { new SimpleGrantedAuthority(it) }
            return new User(signedJWT.JWTClaimsSet.subject, 'N/A', roles)
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
