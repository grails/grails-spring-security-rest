package com.odobo.grails.plugin.springsecurity.rest.token.reader

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Reads the token from a configurable HTTP Header
 */
class HttpHeaderTokenReader implements TokenReader {

    String headerName

    /**
     * @return the token from the header {@link #headerName}, null otherwise
     */
    @Override
    String findToken(HttpServletRequest request) {
        return request.getHeader(headerName)
    }
}
