package com.odobo.grails.plugin.springsecurity.rest.oauth

import groovy.util.logging.Log4j
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.Google2Api
import org.scribe.model.Token
import org.scribe.oauth.OAuthService
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.pac4j.oauth.client.*

/**
 * TODO: write doc
 */
@Log4j
class OauthFilter extends GenericFilterBean {

    String endpointUrl = '/oauth'

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request
        HttpServletResponse httpServletResponse = response

        def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            log.debug "Applying OAuth filter to this request"

            Google2Client google2Client = new Google2Client("1093785205845-hl3jv0rd8jfohkn55jchgmnpvdpsnal4.apps.googleusercontent.com", "sWXY3VMm4wKAGoRZg8r3ftZc")
            google2Client.callbackUrl = 'http://localhost:8080/memcached/oauth/filter'
            google2Client.scope = Google2Client.Google2Scope.EMAIL

            WebContext context = new J2EContext(httpServletRequest, httpServletResponse)

            String authUrl = google2Client.getRedirectionUrl context

            httpServletResponse.sendRedirect authUrl

        } else {
            chain.doFilter(request, response)
        }
    }
}
