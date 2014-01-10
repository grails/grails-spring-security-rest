package com.odobo.grails.plugin.springsecurity.rest.credentials

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Extracts username and password from the request and creates and {@link UsernamePasswordAuthenticationToken}
 * object
 */
public interface CredentialsExtractor {

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest)

}