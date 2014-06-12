package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * Generates a JSON response that meets the RFC6750 specification for Bearer tokens.
 *
 * @see DefaultRestAuthenticationTokenJsonRenderer
 */
@Slf4j
class BearerTokenJsonRenderer extends DefaultRestAuthenticationTokenJsonRenderer {

    @Override
    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal

        def result = collectUserProperties( userDetails )
        result.access_token = restAuthenticationToken.tokenValue
        result.token_type = 'Bearer'

        def json = result as JSON

        if( log.debugEnabled ) {
            log.debug "Generated token JSON:\n${json.toString( true )}"
        }

        json
    }
}
