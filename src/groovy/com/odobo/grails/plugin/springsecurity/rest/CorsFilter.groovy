package com.odobo.grails.plugin.springsecurity.rest

import groovy.util.logging.Log4j
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Supports CORS requests
 */
@Log4j
class CorsFilter extends GenericFilterBean {

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request
        HttpServletResponse httpServletResponse = response

        log.debug "Setting common CORS headers"
        httpServletResponse.setHeader 'Access-Control-Allow-Origin', '*'
        httpServletResponse.setHeader 'Access-Control-Allow-Credentials', 'true'


        if ('OPTIONS'.equals(httpServletRequest.method)) {
            log.debug "OPTIONS request received. Setting CORS headers"

            httpServletResponse.setHeader 'Access-Control-Allow-Headers', 'true'
            httpServletResponse.setHeader 'Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS'
            httpServletResponse.setHeader 'Access-Control-Max-Age', '3600'


        } else {
            chain.doFilter(request, response)
        }
    }
}
