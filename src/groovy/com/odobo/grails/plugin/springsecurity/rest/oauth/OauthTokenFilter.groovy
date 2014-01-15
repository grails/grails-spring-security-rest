package com.odobo.grails.plugin.springsecurity.rest.oauth

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Log4j
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.credentials.OAuthCredentials
import org.pac4j.oauth.profile.google2.Google2Profile
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.Google2Api
import org.scribe.model.Token
import org.scribe.oauth.OAuthService
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * TODO: write doc
 */
@Log4j
class OauthTokenFilter extends GenericFilterBean {

    String endpointUrl = '/oauth/filter'

    TokenGenerator tokenGenerator

    TokenStorageService tokenStorageService

    UserDetailsService userDetailsService

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = request
        HttpServletResponse httpServletResponse = response

        def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            log.debug "Applying OAuth token filter to this request"
            println httpServletRequest.parameterMap

            String code = httpServletRequest.getParameter 'code'
            log.debug "Found OAuth request token: ${code}"

            Google2Client google2Client = new Google2Client("1093785205845-hl3jv0rd8jfohkn55jchgmnpvdpsnal4.apps.googleusercontent.com", "sWXY3VMm4wKAGoRZg8r3ftZc")
            google2Client.callbackUrl = 'http://localhost:8080/memcached/oauth/filter'
            google2Client.scope = Google2Client.Google2Scope.EMAIL
            WebContext context = new J2EContext(httpServletRequest, httpServletResponse)

            OAuthCredentials credentials = google2Client.getCredentials context

            Google2Profile profile = google2Client.getUserProfile credentials

            log.debug "User's email: ${profile.email}"
            log.debug "User's ID: ${profile.id}"
            log.debug profile.properties

            String tokenValue = tokenGenerator.generateToken()
            log.debug "Generated token: ${tokenValue}"

            UserDetails userDetails = userDetailsService.loadUserByUsername profile.id

            tokenStorageService.storeToken(tokenValue, userDetails)

            Authentication authenticationResult = new RestAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, tokenValue)

            SecurityContextHolder.context.setAuthentication(authenticationResult)

            httpServletResponse.sendRedirect "http://example.org#token=${tokenValue}"
        } else {
            chain.doFilter(request, response)
        }
    }
}
