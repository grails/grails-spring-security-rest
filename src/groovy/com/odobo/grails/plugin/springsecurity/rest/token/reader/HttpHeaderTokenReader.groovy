package com.odobo.grails.plugin.springsecurity.rest.token.reader

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Reads the token from a configurable HTTP Header
 */
class HttpHeaderTokenReader implements TokenReader {

    String headerName
    String tokenArgName

    /**
     * @return the token from the header {@link #headerName}, null otherwise
     */
    @Override
    String findToken(HttpServletRequest request, HttpServletResponse response) {
        // only GET request potentially contain a token arg in the queryString
        if (request.method != 'GET') {
            return request.getHeader(headerName)
        }
        String tokenValue = request.getHeader(headerName)
        if (!tokenValue && tokenArgName) {
            //log.debug "Looking for token in query string"
            def queryParams = getQueryAsMap(request.getQueryString())
            queryParams.each{ k, v->
                if (k.equals(tokenArgName)) tokenValue = v
            }
        }
        return tokenValue
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
