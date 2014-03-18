package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.client.BaseOAuthClient
import org.pac4j.oauth.profile.OAuth20Profile
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Deals with pac4j library to fetch a user profile from the selected OAuth provider, and stores it on the security context
 */
class OauthService {

    TokenGenerator tokenGenerator
    TokenStorageService tokenStorageService
    UserDetailsService userDetailsService
    GrailsApplication grailsApplication
    LinkGenerator grailsLinkGenerator
    OauthUserDetailsService oauthUserDetailsService


    BaseOAuthClient getClient(String provider) {
        log.debug "Creating OAuth client for provider: ${provider}"
        def providerConfig = grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
        def ClientClass = providerConfig.client

        BaseOAuthClient client = ClientClass.newInstance(providerConfig.key, providerConfig.secret)

        String callbackUrl = grailsLinkGenerator.link controller: 'oauth', action: 'callback', params: [provider: provider], mapping: 'oauth', absolute: true
        log.debug "Callback URL is: ${callbackUrl}"
        client.callbackUrl = callbackUrl

        if (providerConfig.scope) client.scope = providerConfig.scope
        if (providerConfig.fields) client.fields = providerConfig.fields

        return client
    }

    String storeAuthentication(String provider, WebContext context) {
        BaseOAuthClient client = getClient(provider)
        Credentials credentials = client.getCredentials context

        log.debug "Querying provider to fetch User ID"
        CommonProfile profile = client.getUserProfile credentials, null
        log.debug "User's ID: ${profile.id}"

        def providerConfig = grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
        List defaultRoles = providerConfig.defaultRoles.collect { new SimpleGrantedAuthority(it) }
        OauthUser userDetails = oauthUserDetailsService.loadUserByUserProfile(profile, defaultRoles)

        String tokenValue = tokenGenerator.generateToken()
        log.debug "Generated REST authentication token: ${tokenValue}"

        log.debug "Storing token on the token storage"
        tokenStorageService.storeToken(tokenValue, userDetails)
        Authentication authenticationResult = new RestAuthenticationToken(userDetails, userDetails.password, userDetails.authorities, tokenValue)
        SecurityContextHolder.context.setAuthentication(authenticationResult)

        return tokenValue
    }
}
