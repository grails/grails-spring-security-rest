package grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import groovy.transform.CompileStatic
import org.springframework.security.core.userdetails.UserDetails

/**
 * A {@link CustomClaimProvider} that does nothing
 */
@CompileStatic
class NoOpCustomClaimProvider implements CustomClaimProvider {
    @Override
    void provideCustomClaims(JWTClaimsSet.Builder builder, UserDetails details, String principal, Integer expiration) {
    }
}
