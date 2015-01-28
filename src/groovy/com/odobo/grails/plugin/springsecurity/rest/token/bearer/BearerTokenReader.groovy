package com.odobo.grails.plugin.springsecurity.rest.token.bearer

import com.odobo.grails.plugin.springsecurity.rest.token.reader.TokenReader
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * RFC 6750 implementation of a {@link com.odobo.grails.plugin.springsecurity.rest.token.reader.TokenReader}
 */
@Slf4j
class BearerTokenReader implements TokenReader {

    /**
     * Finds the bearer token within the specified request.  It will attempt to look in all places allowed by the
     * specification: Authorization header, form encoded body, and query string.
     *
     * @param request
     * @return the token if found, null otherwise
     */
    @Override
    String findToken(HttpServletRequest request) {
        log.debug "Looking for bearer token in Authorization header, query string or Form-Encoded body parameter"
        String tokenValue

        if (request.getHeader('Authorization')?.startsWith('Bearer')) {
            log.debug "Found bearer token in Authorization header"
            tokenValue = request.getHeader('Authorization').substring(7)
        } else if (isFormEncoded(request) && request.parts.size() <= 1 && !request.get) {
            log.debug "Found bearer token in request body"
            tokenValue = request.parameterMap['access_token']?.first()
        } else if (request.queryString?.contains('access_token')) {
            log.debug "Found bearer token in query string"
            tokenValue = request.getParameter('access_token')
        } else {
            log.debug "No token found"
        }
        return tokenValue
    }

    private boolean isFormEncoded(HttpServletRequest servletRequest) {
        servletRequest.contentType && MediaType.parseMediaType(servletRequest.contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
    }
}
