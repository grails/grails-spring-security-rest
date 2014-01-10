package com.odobo.grails.plugin.springsecurity.rest.credentials

import groovy.util.logging.Log4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Extracts credentials from a JSON request like: <code>{"username": "foo", "password": "bar"}</code>
 */
@Log4j
class DefaultJsonPayloadCredentialsExtractor extends AbstractJsonPayloadCredentialsExtractor {

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        def jsonBody = getJsonBody(httpServletRequest)

        log.debug "Extracted credentials from request params. Username: ${jsonBody.username}, password: ${jsonBody.password.size()?'[PROTECTED]':'[MISSING]'}"

        new UsernamePasswordAuthenticationToken(jsonBody.username, jsonBody.password)
    }

}
