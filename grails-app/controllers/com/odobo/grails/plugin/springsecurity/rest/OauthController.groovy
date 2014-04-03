package com.odobo.grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.annotation.Secured
import org.pac4j.core.client.RedirectAction
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.client.BaseOAuthClient
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Secured(['permitAll'])
class OauthController {

    final CALLBACK_ATTR = "spring-security-rest-callback"

    def oauthService
    def grailsApplication

    /**
     * Starts the OAuth authentication flow, redirecting to the provider's Login URL. An optional callback parameter
     * allows the frontend application to define the frontend callback URL on demand.
     */
    def authenticate(String provider, String callback) {
        BaseOAuthClient client = oauthService.getClient(provider)
        WebContext context = new J2EContext(request, response)

        RedirectAction redirectAction = client.getRedirectAction(context, true, false)

        if (callback) {
            log.debug "Trying to store in the HTTP session a user specified callback URL: ${callback}"
            try {
                new URL(callback)
                session[CALLBACK_ATTR] = callback
            } catch (MalformedURLException mue) {
                log.warn "The URL is malformed. Not storing it."
            }
        }

        log.debug "Redirecting to ${redirectAction.location}"
        redirect url: redirectAction.location
    }

    /**
     * Handles the OAuth provider callback. It uses {@link OauthService} to generate and store a token for that user,
     * and finally redirects to the configured frontend callback URL, where the token is in the URL. That way, the
     * frontend application can store the REST API token locally for subsequent API calls.
     */
    def callback(String provider) {
        WebContext context = new J2EContext(request, response)
        def frontendCallbackUrl
        if (session[CALLBACK_ATTR]) {
            log.debug "Found callback URL in the HTTP session"
            frontendCallbackUrl = session[CALLBACK_ATTR]
        } else {
            log.debug "Found callback URL in the configuration file"
            frontendCallbackUrl = grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl
        }

        try {
            String tokenValue = oauthService.storeAuthentication(provider, context)

            if (session[CALLBACK_ATTR]) {
                frontendCallbackUrl += tokenValue
                session[CALLBACK_ATTR] = null
            } else {
                frontendCallbackUrl = frontendCallbackUrl.call(tokenValue)
            }

        } catch (Exception e) {
            String errorParams

            if (e instanceof UsernameNotFoundException) {
                errorParams = "&error=403&message=${e.message?.encodeAsURL()?:''}"
            } else {
                errorParams = "&error=${e.cause?.code?:500}&message=${e.message?.encodeAsURL()?:''}"
            }

            if (session[CALLBACK_ATTR]) {
                frontendCallbackUrl += errorParams
                session[CALLBACK_ATTR] = null
            } else {
                frontendCallbackUrl = frontendCallbackUrl.call(errorParams)
            }

        }

        log.debug "Redirecting to ${frontendCallbackUrl}"
        redirect url: frontendCallbackUrl
    }



}
