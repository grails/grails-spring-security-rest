/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.client.BaseOAuthClient
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Deals with pac4j library to fetch a user profile from the selected OAuth provider, and stores it on the security context
 */
class RestOauthService {

    static transactional = false

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

        BaseOAuthClient client
        if (ClientClass?.toString()?.endsWith("CasOAuthWrapperClient")) {
            client = ClientClass.newInstance(providerConfig.key, providerConfig.secret, providerConfig.casOAuthUrl)
        } else {
            client = ClientClass.newInstance(providerConfig.key, providerConfig.secret)
        }

        String callbackUrl = grailsLinkGenerator.link controller: 'restOauth', action: 'callback', params: [provider: provider], mapping: 'oauth', absolute: true
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

        AccessToken accessToken = tokenGenerator.generateAccessToken(userDetails)
        log.debug "Generated REST authentication token: ${accessToken}"

        log.debug "Storing token on the token storage"
        tokenStorageService.storeToken(accessToken.accessToken, userDetails)

        SecurityContextHolder.context.setAuthentication(accessToken)

        return accessToken.accessToken
    }
}
