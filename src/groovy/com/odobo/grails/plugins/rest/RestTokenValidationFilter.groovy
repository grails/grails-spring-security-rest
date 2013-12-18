package com.odobo.grails.plugins.rest

import groovy.transform.CompileStatic
import org.omg.IOP.ServiceContextHolder
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * This filter
 */
@CompileStatic
class RestTokenValidationFilter extends GenericFilterBean {

    String headerName

    AuthenticationProvider tokenValidatorProvider

    /**
     * Override to extract the principal information from the current request
     */
    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        SecurityContext context = SecurityContextHolder.context
        HttpServletRequest servletRequest = request as HttpServletRequest

        if (!context.authentication?.authenticated) {

            String token = servletRequest.getHeader(headerName)

            if (token) {
                try {
                    Authentication authenticationRequest = new RestAuthenticationToken(token)
                    Authentication authenticationResponse = tokenValidatorProvider.authenticate(authenticationRequest)
                    SecurityContextHolder.context.setAuthentication(authenticationResponse)
                } catch (AuthenticationException ae) {
                    //Do nothing?
                }
            }
        }

        chain.doFilter(request, response)
    }


}
