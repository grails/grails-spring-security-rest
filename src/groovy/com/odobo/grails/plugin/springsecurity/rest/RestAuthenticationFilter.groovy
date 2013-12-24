package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
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
class RestAuthenticationFilter extends GenericFilterBean {

    String usernameParameter

    String passwordParameter

    String endpointUrl

    AuthenticationManager authenticationManager

    AuthenticationSuccessHandler authenticationSuccessHandler

    AuthenticationFailureHandler authenticationFailureHandler

    AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource

    TokenGenerator tokenGenerator

    TokenStorageService tokenStorageService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authenticationRequest = authenticationDetailsSource.buildDetails(request)
        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest)

        if (authenticationResult.authenticated) {
            String tokenValue = tokenGenerator.generateToken()

            tokenStorageService.storeToken(tokenValue, authenticationResult.details)

            SecurityContextHolder.context.setAuthentication(authenticationResult)

            authenticationSuccessHandler.onAuthenticationSuccess(request, response, authenticationResult)
        }

        chain.doFilter(request, response)
    }
}
