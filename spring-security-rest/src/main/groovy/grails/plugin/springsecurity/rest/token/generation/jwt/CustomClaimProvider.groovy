package grails.plugin.springsecurity.rest.token.generation.jwt

import com.nimbusds.jwt.JWTClaimsSet
import org.springframework.security.core.userdetails.UserDetails

/**
 * Interface providing a hook to have a chance to add additional claims inside the JWT
 */
interface CustomClaimProvider {

    /**
     * The method will be called when the JWT is built. Use the builder to include additional claims, if needed.
     *
     * @param builder the claim builder used to add additional claims
     * @param details the {@link UserDetails} representing the authenticated user
     * @param principal the principal, usually the username
     * @param expiration the expiration time in seconds for which the JWT is configured to
     */
    void provideCustomClaims(JWTClaimsSet.Builder builder, UserDetails details, String principal, Integer expiration)

}