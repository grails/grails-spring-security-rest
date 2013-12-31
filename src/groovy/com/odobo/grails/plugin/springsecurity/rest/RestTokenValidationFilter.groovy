package com.odobo.grails.plugin.springsecurity.rest

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 * Created by mariscal on 23/12/13.
 */
class RestTokenValidationFilter extends GenericFilterBean {

    String headerName

    RestAuthenticationProvider restAuthenticationProvider

    AuthenticationSuccessHandler authenticationSuccessHandler

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = request

        String tokenValue = servletRequest.getHeader(headerName)

        //TODO implement failure handler

        if (tokenValue) {
            RestAuthenticationToken authenticationRequest = new RestAuthenticationToken(tokenValue)
            RestAuthenticationToken authenticationResult = restAuthenticationProvider.authenticate(authenticationRequest)

            SecurityContextHolder.context.setAuthentication(authenticationResult)

            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticationResult)
        }

        chain.doFilter(request, response)
    }
}
