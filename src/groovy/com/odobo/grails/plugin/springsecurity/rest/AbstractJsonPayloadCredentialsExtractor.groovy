package com.odobo.grails.plugin.springsecurity.rest

import com.google.common.io.CharStreams
import groovy.json.JsonSlurper
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

import javax.servlet.http.HttpServletRequest

/**
 * Base class for JSON-based credentials extractors. It helps building a JSON object from the request body
 */
abstract class AbstractJsonPayloadCredentialsExtractor implements CredentialsExtractor {

    Object getJsonBody(HttpServletRequest httpServletRequest) {
        String body = CharStreams.toString(httpServletRequest.reader)
        JsonSlurper slurper = new JsonSlurper()
        slurper.parseText(body)
    }
}
