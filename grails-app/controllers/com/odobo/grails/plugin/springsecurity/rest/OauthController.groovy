package com.odobo.grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.annotation.Secured
import org.pac4j.core.client.Client
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.client.BaseOAuth20Client

@Secured(['permitAll'])
class OauthController {

    def oauthService
    def grailsApplication

    /**
     * Starts the OAuth 2.0 authentication flow, redirecting to the provider's Login URL
     */
    def authenticate(String provider) {
        Client client = oauthService.getClient(provider)
        WebContext context = new J2EContext(request, response)

        def redirectionUrl = client.getRedirectionUrl(context)
        log.debug "Redirecting to ${redirectionUrl}"
        redirect url: redirectionUrl
    }

    /**
     * Handles the OAuth 2.0 provider callback. It uses {@link OauthService} to generate and store a token for that user,
     * and finally redirects to the configured frontend callback URL, where the token is in the URL. That way, the
     * frontend application can store the REST API token locally for subsequent API calls.
     */
    def callback(String provider) {
        WebContext context = new J2EContext(request, response)
        String tokenValue = oauthService.storeAuthentication(provider, context)

        def frontendCallbackUrl = grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl.call(tokenValue)
        log.debug "Redirecting to ${frontendCallbackUrl}"
        redirect url: frontendCallbackUrl
    }



}
