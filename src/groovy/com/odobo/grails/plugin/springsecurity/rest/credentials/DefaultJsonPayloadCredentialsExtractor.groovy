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

    UsernamePasswordAuthenticationToken extractCredentials(HttpServletRequest httpServletRequest) {
        def jsonBody = getJsonBody(httpServletRequest)

        log.debug "Extracted credentials from request params. Username: ${jsonBody.username}, password: ${jsonBody.password?.size()?'[PROTECTED]':'[MISSING]'}"

        // Retrieve from configuration username/email configuration
        def conf = SpringSecurityUtils.securityConfig

        String usernameParam = conf.rest.token.json.usernamePropertyName?:"username"
        String passwordParam = conf.rest.token.json.passwordPropertyName?:"password"

        new UsernamePasswordAuthenticationToken(jsonBody."$usernameParam", jsonBody."$passwordParam")
    }

}
