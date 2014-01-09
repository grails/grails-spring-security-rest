package com.odobo.grails.plugin.springsecurity.rest

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Extracts credentials from request parameters
 */
class RequestParamsCredentialsExtractor implements CredentialsExtractor {

    String usernameParameter
    String passwordParameter

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter(usernameParameter)
        String password = httpServletRequest.getParameter(passwordParameter)

        new UsernamePasswordAuthenticationToken(username, password)
    }

}
