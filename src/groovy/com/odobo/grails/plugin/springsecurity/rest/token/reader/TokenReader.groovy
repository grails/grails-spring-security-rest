package com.odobo.grails.plugin.springsecurity.rest.token.reader

import com.odobo.grails.plugin.springsecurity.rest.token.AccessToken

import javax.servlet.http.HttpServletRequest

public interface TokenReader {

    /**
     * Reads a token (if any) from the request
     *
     * @param request the HTTP request
     * @param response the response, in case any status code has to be sent
     * @return the token when found, null otherwise
     */
    AccessToken findToken(HttpServletRequest request)

}