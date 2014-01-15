package com.odobo.grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.annotation.Secured
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.client.BaseOAuth20Client

@Secured(['permitAll'])
class OauthController {

    def oauthService
    def grailsApplication

    def authenticate(String provider) {
        BaseOAuth20Client client = oauthService.getClient(provider)
        WebContext context = new J2EContext(request, response)
        redirect url: client.getRedirectionUrl(context)
    }


    def callback(String provider) {
        BaseOAuth20Client client = oauthService.getClient(provider)
        WebContext context = new J2EContext(request, response)
        String tokenValue = oauthService.storeAuthentication(provider, context)
        redirect url: grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl.call(tokenValue)
    }



}
