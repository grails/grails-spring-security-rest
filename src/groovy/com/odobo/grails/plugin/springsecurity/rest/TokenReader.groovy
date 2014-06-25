package com.odobo.grails.plugin.springsecurity.rest

import javax.servlet.http.HttpServletRequest

/**
 * Created by ajbrown on 6/24/14.
 */
public interface TokenReader {

    def String findToken( HttpServletRequest request )
}