package com.odobo.grails.plugins.rest

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpRequestResponseHolder
import org.springframework.security.web.context.SecurityContextRepository

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * TODO: write doc
 */
class GormSecurityContextRepository implements SecurityContextRepository {

    String headerName

    /**
     * Obtains the security context for the supplied request. For an unauthenticated user, an empty context
     * implementation should be returned. This method should not return null.
     * <p>
     * The use of the <tt>HttpRequestResponseHolder</tt> parameter allows implementations to return wrapped versions of
     * the request or response (or both), allowing them to access implementation-specific state for the request.
     * The values obtained from the holder will be passed on to the filter chain and also to the <tt>saveContext</tt>
     * method when it is finally called. Implementations may wish to return a subclass of
     * {@link SaveContextOnUpdateOrErrorResponseWrapper} as the response object, which guarantees that the context is
     * persisted when an error or redirect occurs.
     *
     * @param requestResponseHolder holder for the current request and response for which the context should be loaded.
     *
     * @return The security context which should be used for the current request, never null.
     */
    @Override
    SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {

        SecurityContext context = SecurityContextHolder.createEmptyContext()
        String tokenValue = requestResponseHolder.request.getHeader(headerName)

        //TODO make generic
        AuthenticationToken authenticationToken = AuthenticationToken.findByValue(tokenValue)

        if (authenticationToken) {
            context.authentication = new RestAuthenticationToken(tokenValue)
        }

        return context
    }

    /**
     * Stores the security context on completion of a request.
     *
     * @param context the non-null context which was obtained from the holder.
     * @param request
     * @param response
     */
    @Override
    void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        RestAuthenticationToken authenticationToken = context.authentication

        if (authenticationToken) {

            //TODO make generic
            AuthenticationToken existingToken = AuthenticationToken.findByValue(authenticationToken.tokenValue)

            if (!existingToken) {
                existingToken = new AuthenticationToken(value: authenticationToken.tokenValue)
            }

            AuthenticationToken.withNewSession {
                existingToken.save()
            }
        }
    }

    /**
     * Allows the repository to be queried as to whether it contains a security context for the
     * current request.
     *
     * @param request the current request
     * @return true if a context is found for the request, false otherwise
     */
    @Override
    boolean containsContext(HttpServletRequest request) {
        return false
    }

}
