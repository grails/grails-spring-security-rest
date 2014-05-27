package com.odobo.grails.plugin.springsecurity.rest.credentials

import grails.plugin.springsecurity.SpringSecurityUtils

import groovy.util.logging.Slf4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Extracts credentials from a JSON request like: <code>{"username": "foo", "password": "bar"}</code>
 */
@Slf4j
class DefaultJsonPayloadCredentialsExtractor extends AbstractJsonPayloadCredentialsExtractor {

    String usernameParameter
    String passwordParameter

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        def jsonBody = getJsonBody(httpServletRequest)

        String username = jsonBody."${usernameParameter}"
        String password = jsonBody."${passwordParameter}"

        log.debug "Extracted credentials from JSON payload. Username: ${username}, password: ${password?.size()?'[PROTECTED]':'[MISSING]'}"

        new UsernamePasswordAuthenticationToken(username, password)
    }

}
