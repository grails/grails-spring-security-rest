package com.odobo.grails.plugin.springsecurity.rest.credentials

import groovy.util.logging.Slf4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Extracts credentials from request parameters
 */
@Slf4j
class RequestParamsCredentialsExtractor implements CredentialsExtractor {

    String usernamePropertyName
    String passwordPropertyName

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter(usernamePropertyName)
        String password = httpServletRequest.getParameter(passwordPropertyName)

        log.debug "Extracted credentials from request params. Username: ${username}, password: ${password?.size()?'[PROTECTED]':'[MISSING]'}"

        new UsernamePasswordAuthenticationToken(username, password)
    }

}
