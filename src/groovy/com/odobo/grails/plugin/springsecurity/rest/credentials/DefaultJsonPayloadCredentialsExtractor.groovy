package com.odobo.grails.plugin.springsecurity.rest.credentials

import groovy.util.logging.Slf4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Extracts credentials from a JSON request like: <code>{"username": "foo", "password": "bar"}</code>
 */
@Slf4j
class DefaultJsonPayloadCredentialsExtractor extends AbstractJsonPayloadCredentialsExtractor {

    String usernamePropertyName
    String passwordPropertyName

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        def jsonBody = getJsonBody(httpServletRequest)

        if (jsonBody) {
            String username = jsonBody."${usernamePropertyName}"
            String password = jsonBody."${passwordPropertyName}"

            log.debug "Extracted credentials from JSON payload. Username: ${username}, password: ${password?.size()?'[PROTECTED]':'[MISSING]'}"

            new UsernamePasswordAuthenticationToken(username, password)
        } else {
            log.debug "No JSON body sent in the request"
            return null
        }
    }

}
