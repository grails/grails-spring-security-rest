package com.odobo.grails.plugin.springsecurity.rest

import groovy.util.logging.Slf4j
import org.springframework.http.MediaType
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
abstract class AbstractRestFilter extends GenericFilterBean {

    protected String findBearerToken(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        log.debug "Looking for bearer token in Authorization header or Form-Encoded body parameter"
        String tokenValue

        if (servletRequest.getHeader('Authorization')?.startsWith('Bearer ')) {
            log.debug "Found bearer token in Authorization header"
            tokenValue = servletRequest.getHeader('Authorization').substring(7)
        } else if (matchesBearerSpecPreconditions(servletRequest, servletResponse)) {
            log.debug "Looking for token in request body"
            tokenValue = servletRequest.parameterMap['access_token']?.first()
        }
        return tokenValue
    }

    /**
     * Checks if the requests matches RFC 6750 Bearer Token specification
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     */
    protected boolean matchesBearerSpecPreconditions(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        boolean matches = true
        String message = ''
        if (!MediaType.parseMediaType(servletRequest.contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)) {
            log.debug "Invalid Content-Type: '${servletRequest.contentType}'. 'application/x-www-form-urlencoded' is mandatory"
            message = "Content-Type 'application/x-www-form-urlencoded' is mandatory when sending form-encoded body parameter requests with the access token (RFC 6750)"
            matches = false
        } else if (servletRequest.parts.size() > 1) {
            log.debug "Invalid request: it contains ${servletRequest.parts.size()}"
            message = "HTTP request entity-body has to be single-part when sending form-encoded body parameter requests with the access token (RFC 6750)"
            matches = false
        } else if (servletRequest.get) {
            log.debug "Invalid HTTP method: GET"
            message = "GET HTTP method must not be used when sending form-encoded body parameter requests with the access token (RFC 6750)"
            matches = false
        }
        if (!matches) {
            servletResponse.addHeader('WWW-Authenticate', 'Bearer error="invalid_request"')
            servletResponse.sendError HttpServletResponse.SC_BAD_REQUEST, message
        }
        return matches
    }

}
