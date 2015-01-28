package com.odobo.grails.plugin.springsecurity.rest.token.reader

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

public interface TokenReader {

    /**
     * Reads a token (if any) from the request
     *
     * @param request the HTTP request
     * @param response the response, in case any status code has to be sent
     * @return the token when found, null otherwise
     */
    String findToken(HttpServletRequest request)

}