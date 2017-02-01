package grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.core.userdetails.UserDetails

/**
 * Sets the issuer of the JWT as per a configuration value
 */
class IssuerClaimProvider implements CustomClaimProvider {

    String issuerName

    @Override
    void provideCustomClaims(JWTClaimsSet.Builder builder, UserDetails details, Integer expiration) {
        builder.issuer(issuerName)
    }

}
