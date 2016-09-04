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

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.authentication.DefaultRestAuthenticationEventPublisher
import grails.plugin.springsecurity.rest.authentication.NullRestAuthenticationEventPublisher
import grails.plugin.springsecurity.rest.credentials.DefaultJsonPayloadCredentialsExtractor
import grails.plugin.springsecurity.rest.credentials.RequestParamsCredentialsExtractor
import grails.plugin.springsecurity.rest.error.DefaultCallbackErrorHandler
import grails.plugin.springsecurity.rest.oauth.DefaultOauthUserDetailsService
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenAccessDeniedHandler
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenAuthenticationEntryPoint
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenAuthenticationFailureHandler
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader
import grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import grails.plugin.springsecurity.rest.token.generation.jwt.DefaultRSAKeyProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.EncryptedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.generation.jwt.FileRSAKeyProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.NoOpCustomClaimProvider
import grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.reader.HttpHeaderTokenReader
import grails.plugin.springsecurity.rest.token.rendering.DefaultAccessTokenJsonRenderer
import grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import grails.plugins.Plugin
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.savedrequest.NullRequestCache

class SpringSecurityRestGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "3.1.0 > *"
    List loadAfter = ['springSecurityCore']
    List pluginExcludes = [
        "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin"
    String author = "Alvaro Sanchez-Mariscal"
    String authorEmail = "alvaro.sanchezmariscal@gmail.com"
    String description = 'Implements authentication for REST APIs based on Spring Security. It uses a token-based workflow'

    def profiles = ['web']

    // URL to the plugin's documentation
    String documentation = "http://alvarosanchez.github.io/grails-spring-security-rest/"

    // Extra (optional) plugin metadata
    String license = "APACHE"
    def organization = [ name: "Object Computing, Inc.", url: "http://www.ociweb.com" ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/alvarosanchez/grails-spring-security-rest/issues" ]
    def scm = [ url: "https://github.com/alvarosanchez/grails-spring-security-rest" ]

    Closure doWithSpring() { {->
        def conf = SpringSecurityUtils.securityConfig
        if (!conf || !conf.active) {
            return
        }

        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestSecurityConfig'
        conf = SpringSecurityUtils.securityConfig

        if (!conf.rest.active) {
            return
        }

        boolean printStatusMessages = (conf.printStatusMessages instanceof Boolean) ? conf.printStatusMessages : true

        if (printStatusMessages) {
            println "\nConfiguring Spring Security REST ${plugin.version}..."
        }

        ///*
        SpringSecurityUtils.registerProvider 'restAuthenticationProvider'

        /* restAuthenticationFilter */
        if(conf.rest.login.active) {
            SpringSecurityUtils.registerFilter 'restAuthenticationFilter', SecurityFilterPosition.FORM_LOGIN_FILTER.order + 1

            restAuthenticationFilter(RestAuthenticationFilter) {
                authenticationManager = ref('authenticationManager')
                authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
                authenticationFailureHandler = ref('restAuthenticationFailureHandler')
                authenticationDetailsSource = ref('authenticationDetailsSource')
                credentialsExtractor = ref('credentialsExtractor')
                endpointUrl = conf.rest.login.endpointUrl
                tokenGenerator = ref('tokenGenerator')
                tokenStorageService = ref('tokenStorageService')
                authenticationEventPublisher = ref('authenticationEventPublisher')
            }

            def paramsClosure = {
                usernamePropertyName = conf.rest.login.usernamePropertyName // username
                passwordPropertyName = conf.rest.login.passwordPropertyName // password
            }

            if (conf.rest.login.useRequestParamsCredentials) {
                credentialsExtractor(RequestParamsCredentialsExtractor, paramsClosure)
            } else if (conf.rest.login.useJsonCredentials) {
                credentialsExtractor(DefaultJsonPayloadCredentialsExtractor, paramsClosure)
            }

            /* restLogoutFilter */
            restLogoutFilter(RestLogoutFilter) {
                endpointUrl = conf.rest.logout.endpointUrl
                headerName = conf.rest.token.validation.headerName
                tokenStorageService = ref('tokenStorageService')
                tokenReader = ref('tokenReader')
            }
        }


        restAuthenticationSuccessHandler(RestAuthenticationSuccessHandler) {
            renderer = ref('accessTokenJsonRenderer')
        }
        accessTokenJsonRenderer(DefaultAccessTokenJsonRenderer) {
            usernamePropertyName = conf.rest.token.rendering.usernamePropertyName
            tokenPropertyName = conf.rest.token.rendering.tokenPropertyName
            authoritiesPropertyName = conf.rest.token.rendering.authoritiesPropertyName
            useBearerToken = conf.rest.token.validation.useBearerToken
        }

        if(conf.rest.token.validation.useBearerToken ) {
            tokenReader(BearerTokenReader)
            restAuthenticationFailureHandler(BearerTokenAuthenticationFailureHandler){
                tokenReader = ref('tokenReader')
            }
            restAuthenticationEntryPoint(BearerTokenAuthenticationEntryPoint) {
                tokenReader = ref('tokenReader')
            }
            restAccessDeniedHandler(BearerTokenAccessDeniedHandler) {
                errorPage = null //403
            }

        } else {
            restAuthenticationEntryPoint(Http403ForbiddenEntryPoint)
            tokenReader(HttpHeaderTokenReader) {
                headerName = conf.rest.token.validation.headerName
            }
            restAuthenticationFailureHandler(RestAuthenticationFailureHandler) {
                statusCode = conf.rest.login.failureStatusCode?:HttpServletResponse.SC_UNAUTHORIZED
            }
            restAccessDeniedHandler(AccessDeniedHandlerImpl) {
                errorPage = null //403
            }
        }

        /* restTokenValidationFilter */
        SpringSecurityUtils.registerFilter 'restTokenValidationFilter', SecurityFilterPosition.ANONYMOUS_FILTER.order + 1
        SpringSecurityUtils.registerFilter 'restExceptionTranslationFilter', SecurityFilterPosition.EXCEPTION_TRANSLATION_FILTER.order - 5

        restTokenValidationFilter(RestTokenValidationFilter) {
            headerName = conf.rest.token.validation.headerName
            validationEndpointUrl = conf.rest.token.validation.endpointUrl
            active = conf.rest.token.validation.active
            tokenReader = ref('tokenReader')
            enableAnonymousAccess = conf.rest.token.validation.enableAnonymousAccess
            authenticationSuccessHandler = ref('restAuthenticationSuccessHandler')
            authenticationFailureHandler = ref('restAuthenticationFailureHandler')
            restAuthenticationProvider = ref('restAuthenticationProvider')
            authenticationEventPublisher = ref('authenticationEventPublisher')
        }

        restExceptionTranslationFilter(ExceptionTranslationFilter, ref('restAuthenticationEntryPoint'), ref('restRequestCache')) {
            accessDeniedHandler = ref('restAccessDeniedHandler')
            authenticationTrustResolver = ref('authenticationTrustResolver')
            throwableAnalyzer = ref('throwableAnalyzer')
        }

        restRequestCache(NullRequestCache)

        /* tokenGenerator */
        tokenGenerator(SecureRandomTokenGenerator)

        callbackErrorHandler(DefaultCallbackErrorHandler)

        /* tokenStorageService - defaults to JWT */
        jwtService(JwtService) {
            jwtSecret = conf.rest.token.storage.jwt.secret
        }
        tokenStorageService(JwtTokenStorageService) {
            jwtService = ref('jwtService')
        }

        customClaimProvider(NoOpCustomClaimProvider)

        if (conf.rest.token.storage.jwt.useEncryptedJwt) {
            jwtService(JwtService) {
                keyProvider = ref('keyProvider')
                jwtSecret = conf.rest.token.storage.jwt.secret
            }
            tokenGenerator(EncryptedJwtTokenGenerator) {
                jwtTokenStorageService = ref('tokenStorageService')
                keyProvider = ref('keyProvider')
                defaultExpiration = conf.rest.token.storage.jwt.expiration
                customClaimProvider = ref('customClaimProvider')
            }

            if (conf.rest.token.storage.jwt.privateKeyPath instanceof CharSequence &&
                    conf.rest.token.storage.jwt.publicKeyPath instanceof CharSequence) {
                keyProvider(FileRSAKeyProvider) {
                    privateKeyPath = conf.rest.token.storage.jwt.privateKeyPath
                    publicKeyPath = conf.rest.token.storage.jwt.publicKeyPath
                }
            } else {
                keyProvider(DefaultRSAKeyProvider)
            }

        } else {
            tokenGenerator(SignedJwtTokenGenerator) {
                jwtTokenStorageService = ref('tokenStorageService')
                jwtSecret = conf.rest.token.storage.jwt.secret
                defaultExpiration = conf.rest.token.storage.jwt.expiration
                customClaimProvider = ref('customClaimProvider')
            }
        }

        /* restAuthenticationProvider */
        restAuthenticationProvider(RestAuthenticationProvider) {
            tokenStorageService = ref('tokenStorageService')
            useJwt = true
            jwtService = ref('jwtService')
        }

        /* oauthUserDetailsService */
        oauthUserDetailsService(DefaultOauthUserDetailsService) {
            userDetailsService = ref('userDetailsService')
            preAuthenticationChecks = ref('preAuthenticationChecks')
        }

        // SecurityEventListener
        if (conf.useSecurityEventListener) {
            restSecurityEventListener(RestSecurityEventListener)

            authenticationEventPublisher(DefaultRestAuthenticationEventPublisher)
        } else {
            authenticationEventPublisher(NullRestAuthenticationEventPublisher)
        }

        if (printStatusMessages) {
            println '... finished configuring Spring Security REST\n'
        }
    }}

}
