package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.client.BaseOAuth20Client
import org.pac4j.oauth.credentials.OAuthCredentials
import org.pac4j.oauth.profile.OAuth20Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class OauthService {

    TokenGenerator tokenGenerator
    TokenStorageService tokenStorageService
    UserDetailsService userDetailsService
    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator


    private BaseOAuth20Client<OAuth20Profile> getClient(String provider) {
        def providerConfig = grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
        def ClientClass = providerConfig.client

        BaseOAuth20Client<OAuth20Profile> client = ClientClass.newInstance(providerConfig.key, providerConfig.secret)
        client.callbackUrl = grailsLinkGenerator.link controller: 'oauth', action: 'callback', params: [provider: provider], mapping: 'oauth', absolute: true
        client.scope = providerConfig.scope
        return client
    }

    String storeAuthentication(String provider, WebContext context) {
        BaseOAuth20Client<OAuth20Profile> client = getClient(provider)
        OAuthCredentials credentials = client.getCredentials context
        OAuth20Profile profile = client.getUserProfile credentials

        log.debug "User's ID: ${profile.id}"

        String tokenValue = tokenGenerator.generateToken()
        log.debug "Generated token: ${tokenValue}"

        UserDetails userDetails = userDetailsService.loadUserByUsername profile.id

        tokenStorageService.storeToken(tokenValue, userDetails)

        Authentication authenticationResult = new RestAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, tokenValue)

        SecurityContextHolder.context.setAuthentication(authenticationResult)

        return tokenValue
    }
}
