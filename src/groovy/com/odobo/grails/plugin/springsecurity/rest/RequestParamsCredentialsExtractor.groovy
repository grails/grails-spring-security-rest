package com.odobo.grails.plugin.springsecurity.rest

import groovy.util.logging.Log4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Extracts credentials from request parameters
 */
@Log4j
class RequestParamsCredentialsExtractor implements CredentialsExtractor {

    String usernameParameter
    String passwordParameter

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter(usernameParameter)
        String password = httpServletRequest.getParameter(passwordParameter)

        log.debug "Extracted credentials from request params. Username: ${username}, password: ${password?.size()?'[PROTECTED]':'[MISSING]'}"

        new UsernamePasswordAuthenticationToken(username, password)
    }

}
