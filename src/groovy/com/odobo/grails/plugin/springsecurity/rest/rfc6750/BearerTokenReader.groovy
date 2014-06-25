package com.odobo.grails.plugin.springsecurity.rest.rfc6750

import com.odobo.grails.plugin.springsecurity.rest.TokenReader
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType

import javax.servlet.http.HttpServletRequest

/**
 *
 */
@Slf4j
class BearerTokenReader implements TokenReader {

    /**
     * Find the bearer token within the specified request.  It will attempt to look in all places allowed by the
     * specification: Authorization header, form encoded body, and query string.
     *
     * @param request
     * @return
     */
    @Override
    String findToken( HttpServletRequest request ) {
        log.debug "Looking for bearer token in Authorization header or Form-Encoded body parameter"
        def tokenValue = ''
        def isFormEncoded = false

        try {
            def contentType = request.contentType ? MediaType.parseMediaType( request.contentType ) : null
            isFormEncoded = contentType?.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
        } catch( e ) {
            log.debug "invalid media type specified: ${request.contentType}", e
        }

        if ( request.getHeader('Authorization')?.startsWith('Bearer ') ) {
            log.debug "Looking for bearer token in Authorization header"
            tokenValue = request.getHeader( 'Authorization' ).substring(7)

        } else if ( isFormEncoded && request.method != 'GET' && request.parts.size() <= 1 ) {
            log.debug "Looking for bearer token in query string or form body"
            tokenValue = request.parameterMap['access_token']?.first()

        } else {
            log.debug "Looking for bearer token in the access_token query string value"
            tokenValue = getQueryAsMap( request.queryString )?.get('access_token')

        }

        tokenValue
    }

    /**
     * Returns the specified queryString as a map.
     * @param queryString
     * @return
     */
    private static Map<String, String> getQueryAsMap(String queryString) {
        queryString?.split('&').inject([:]) { map, token ->
            token?.split('=').with { map[it[0]] = it[1] }
            map
        }
    }
}
