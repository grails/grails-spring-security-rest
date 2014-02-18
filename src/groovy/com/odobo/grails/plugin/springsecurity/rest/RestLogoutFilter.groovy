package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Filter exposing an endpoint for deleting tokens. It will read the token from an HTTP header. If found, will delete it
 * from the storage, sending a 200 response. Otherwise, it will send a 404 response.
 */
@Slf4j
class RestLogoutFilter extends GenericFilterBean {

    String endpointUrl

    String headerName

    TokenStorageService tokenStorageService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request
        HttpServletResponse httpServletResponse = response

        def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            log.debug "Applying logout filter to this request"

            //Only POST is supported
            if (httpServletRequest.method != 'POST') {
                log.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
                httpServletResponse.setStatus HttpServletResponse.SC_METHOD_NOT_ALLOWED
                return
            }

            log.debug "Looking for a token value in the header '${headerName}'"
            String tokenValue = httpServletRequest.getHeader(headerName)

            if (tokenValue) {
                log.debug "Token found: ${tokenValue}"

                try {
                    log.debug "Trying to remove the token"
                    tokenStorageService.removeToken tokenValue
                } catch (TokenNotFoundException tnfe) {
                    httpServletResponse.sendError HttpServletResponse.SC_NOT_FOUND, "Token not found"
                }
            } else {
                log.debug "Token header is missing. Sending a 400 Bad Request response"
                httpServletResponse.sendError HttpServletResponse.SC_BAD_REQUEST, "Token header is missing"
            }

        } else {
            chain.doFilter(request, response)
        }
    }
}
