import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.*
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
import grails.plugin.springsecurity.rest.token.generation.jwt.SignedJwtTokenGenerator
import grails.plugin.springsecurity.rest.token.reader.HttpHeaderTokenReader
import grails.plugin.springsecurity.rest.token.rendering.DefaultAccessTokenJsonRenderer
import grails.plugin.springsecurity.rest.token.storage.GormTokenStorageService
import grails.plugin.springsecurity.rest.token.storage.GrailsCacheTokenStorageService
import grails.plugin.springsecurity.rest.token.storage.MemcachedTokenStorageService
import grails.plugin.springsecurity.rest.token.storage.RedisTokenStorageService
import grails.plugin.springsecurity.rest.token.storage.jwt.JwtTokenStorageService
import net.spy.memcached.DefaultHashAlgorithm
import net.spy.memcached.spring.MemcachedClientFactoryBean
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.savedrequest.NullRequestCache

import javax.servlet.http.HttpServletResponse

class SpringSecurityRestGrailsPlugin {

    String version = "1.5.3"
    String grailsVersion = "2.0 > *"
    List loadAfter = ['springSecurityCore']
    List pluginExcludes = [
        "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin"
    String author = "Alvaro Sanchez-Mariscal"
    String authorEmail = "alvaro.sanchezmariscal@gmail.com"
    String description = 'Implements authentication for REST APIs based on Spring Security. It uses a token-based workflow'

    String documentation = "http://alvarosanchez.github.io/grails-spring-security-rest/"

    String license = "APACHE"
    def organization = [ name: "Odobo", url: "http://www.odobo.com" ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/alvarosanchez/grails-spring-security-rest/issues" ]
    def scm = [ url: "https://github.com/alvarosanchez/grails-spring-security-rest" ]

    def doWithSpring = {


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
            println '\nConfiguring Spring Security REST ...'
        }

        ///*
        SpringSecurityUtils.registerProvider 'restAuthenticationProvider'

        /* restAuthenticationFilter */
        if(conf.rest.login.active) {
            SpringSecurityUtils.registerFilter 'restAuthenticationFilter', SecurityFilterPosition.FORM_LOGIN_FILTER.order + 1
            SpringSecurityUtils.registerFilter 'restLogoutFilter', SecurityFilterPosition.LOGOUT_FILTER.order - 1

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

        if( conf.rest.token.validation.useBearerToken ) {
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

        /* tokenStorageService */
        if (conf.rest.token.storage.useMemcached) {
            conf.rest.token.storage.useJwt = false

            Properties systemProperties = System.properties
            systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger")
            System.setProperties(systemProperties)

            memcachedClient(MemcachedClientFactoryBean) {
                servers = conf.rest.token.storage.memcached.hosts
                protocol = 'BINARY'
                transcoder = new CustomSerializingTranscoder()
                opTimeout = 1000
                timeoutExceptionThreshold = 1998
                hashAlg = DefaultHashAlgorithm.KETAMA_HASH
                locatorType = 'CONSISTENT'
                failureMode = 'Redistribute'
                useNagleAlgorithm = false
            }

            tokenStorageService(MemcachedTokenStorageService) {
                memcachedClient = ref('memcachedClient')
                expiration = conf.rest.token.storage.memcached.expiration
            }
        } else if (conf.rest.token.storage.useGrailsCache) {
            conf.rest.token.storage.useJwt = false
            tokenStorageService(GrailsCacheTokenStorageService) {
                grailsCacheManager = ref('grailsCacheManager')
                cacheName = conf.rest.token.storage.grailsCacheName
            }
        } else if (conf.rest.token.storage.useGorm) {
            conf.rest.token.storage.useJwt = false
            tokenStorageService(GormTokenStorageService) {
                userDetailsService = ref('userDetailsService')
            }
        }else if (conf.rest.token.storage.useRedis) {
            conf.rest.token.storage.useJwt = false
            tokenStorageService(RedisTokenStorageService) {
                redisService = ref('redisService')
                userDetailsService = ref('userDetailsService')
                expiration = conf.rest.token.storage.redis.expiration
            }
        } else if (conf.rest.token.storage.useJwt) {
            jwtService(JwtService) {
                jwtSecret = conf.rest.token.storage.jwt.secret
            }
            tokenStorageService(JwtTokenStorageService) {
                jwtService = ref('jwtService')
            }

            if (conf.rest.token.storage.jwt.useEncryptedJwt) {
                jwtService(JwtService) {
                    keyProvider = ref('keyProvider')
                    jwtSecret = conf.rest.token.storage.jwt.secret
                }
                tokenGenerator(EncryptedJwtTokenGenerator) {
                    jwtTokenStorageService = ref('tokenStorageService')
                    keyProvider = ref('keyProvider')
                    defaultExpiration = conf.rest.token.storage.jwt.expiration
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
                }
            }

            SpringSecurityUtils.orderedFilters.remove(SecurityFilterPosition.LOGOUT_FILTER.order - 1)
        }

        /* restAuthenticationProvider */
        restAuthenticationProvider(RestAuthenticationProvider) {
            tokenStorageService = ref('tokenStorageService')
            useJwt = conf.rest.token.storage.useJwt
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

    }

}
