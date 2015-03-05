package com.odobo.grails.plugin.springsecurity.rest.token.reader

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken

import javax.servlet.http.HttpServletRequest

/**
 * Reads the token from a configurable HTTP Header
 */
class HttpHeaderTokenReader implements TokenReader {

    String headerName

    /**
     * @return the token from the header {@link #headerName}, null otherwise
     */
    @Override
    AccessToken findToken(HttpServletRequest request) {
        String tokenValue = request.getHeader(headerName)
        return tokenValue ? new AccessToken(tokenValue) : null
    }
}
