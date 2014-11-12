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
     * @return
     */
    @Override
    String findToken(HttpServletRequest request, HttpServletResponse response) {
        log.debug "Looking for bearer token in Authorization header or Form-Encoded body parameter"
        String tokenValue

        if (request.getHeader('Authorization')?.startsWith('Bearer ')) {
            log.debug "Found bearer token in Authorization header"
            tokenValue = request.getHeader('Authorization').substring(7)
        } else if (matchesBearerSpecPreconditions(request, response)) {
            log.debug "Looking for token in request body"
            tokenValue = request.parameterMap['access_token']?.first()
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
    public boolean matchesBearerSpecPreconditions(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        boolean matches = true
        String message = ''

        boolean isFormEncoded = isFormEncoded(servletRequest)
        if (!isFormEncoded && containsAnAccessToken(servletRequest)) {
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

        log.debug message
        return matches
    }

    private boolean isFormEncoded(HttpServletRequest servletRequest) {
        servletRequest.contentType && MediaType.parseMediaType(servletRequest.contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
    }

    private boolean containsAnAccessToken(HttpServletRequest request) {
        BufferedReader reader = request.getReader();
        if(!reader)
            return false;

        def hasTokenInBody = {
            String line = null;
            reader.mark(1000);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("access_token")) {
                    return true;
                }
            }
        }()
        reader.reset()
        return hasTokenInBody
    }
}