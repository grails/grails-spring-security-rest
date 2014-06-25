package com.odobo.grails.plugin.springsecurity.rest

import javax.servlet.http.HttpServletRequest

/**
 *
 */
class RestTokenReader implements TokenReader {

    String headerName

    @Override
    String findToken(HttpServletRequest request) {
        request.getHeader( headerName )
    }
}
