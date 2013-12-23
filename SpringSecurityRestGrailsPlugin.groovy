import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationFailureHandler
import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationSuccessHandler
import com.odobo.grails.plugin.springsecurity.rest.token.validator.GormTokenAuthenticationProvider
import com.odobo.grails.plugin.springsecurity.rest.token.validator.MemcachedTokenAuthenticationProvider
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.authentication.FilterProcessUrlRequestMatcher
import net.spy.memcached.DefaultHashAlgorithm
import net.spy.memcached.HashAlgorithm
import net.spy.memcached.spring.MemcachedClientFactoryBean
import net.spy.memcached.transcoders.SerializingTranscoder
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.authentication.NullRememberMeServices
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.NullSecurityContextRepository
import org.springframework.security.web.savedrequest.NullRequestCache

class SpringSecurityRestGrailsPlugin {

    String version = "1.0.0.M1"
    String grailsVersion = "2.0 > *"
    List loadAfter = ['springSecurityCore']
    List pluginExcludes = [
        "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin"
    String author = "Alvaro Sanchez-Mariscal"
    String authorEmail = "alvaro.sanchez@odobo.com"
    String description = 'Implements authentication for REST APIs based on Spring Security. It uses a token-based workflow'

    String documentation = "https://github.com/alvarosanchez/grails-spring-security-rest"

    String license = "APACHE"
    def organization = [ name: "Odobo Limited", url: "http://www.odobo.com" ]

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
        //SpringSecurityUtils.registerFilter 'restTokenValidationFilter', SecurityFilterPosition.FORM_LOGIN_FILTER
        //SpringSecurityUtils.registerProvider 'fakeAuthenticationProvider'

        //TODO to config file
        conf.filterChain.filterNames = ['securityContextPersistenceFilter', 'authenticationProcessingFilter',
                                        'anonymousAuthenticationFilter',
                                        'exceptionTranslationFilter', 'filterInvocationInterceptor']

        authenticationProcessingFilter(UsernamePasswordAuthenticationFilter) {
            authenticationManager = ref('authenticationManager')
            sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
            authenticationSuccessHandler = ref('authenticationSuccessHandler')
            authenticationFailureHandler = ref('authenticationFailureHandler')
            rememberMeServices = ref('rememberMeServices')
            authenticationDetailsSource = ref('authenticationDetailsSource')
            requiresAuthenticationRequestMatcher = ref('filterProcessUrlRequestMatcher')
            usernameParameter = conf.rest.login.usernameParameter // j_username
            passwordParameter = conf.rest.login.passwordParameter // j_password
            continueChainBeforeSuccessfulAuthentication = conf.apf.continueChainBeforeSuccessfulAuthentication // false
            allowSessionCreation = false
            postOnly = true
        }

        filterProcessUrlRequestMatcher(FilterProcessUrlRequestMatcher, conf.rest.login.endpointUrl)
        authenticationSuccessHandler(RestAuthenticationSuccessHandler)
        authenticationFailureHandler(RestAuthenticationFailureHandler)
        rememberMeServices(NullRememberMeServices)

        exceptionTranslationFilter(ExceptionTranslationFilter, ref('authenticationEntryPoint'), ref('requestCache')) {
            accessDeniedHandler = ref('accessDeniedHandler')
            authenticationTrustResolver = ref('authenticationTrustResolver')
            throwableAnalyzer = ref('throwableAnalyzer')
        }
        accessDeniedHandler(AccessDeniedHandlerImpl) {
            errorPage = null //403
        }

        requestCache(NullRequestCache)
        authenticationEntryPoint(Http403ForbiddenEntryPoint)

        securityContextRepository(NullSecurityContextRepository)

        if (conf.rest.token.storage.useMemcached) {
            memcachedClient(MemcachedClientFactoryBean) {
                servers = conf.rest.token.storage.memcached.hosts
                protocol = 'BINARY'
                transcoder = { SerializingTranscoder st ->
                    compressionThreshold = 1024
                }
                opTimeout = 1000
                timeoutExceptionThreshold = 1998
                hashAlg = DefaultHashAlgorithm.KETAMA_HASH
                locatorType = 'CONSISTENT'
                failureMode = 'Redistribute'
                useNagleAlgorithm = false
            }


            tokenAuthenticationProvider(MemcachedTokenAuthenticationProvider) {
                memcachedClient = ref('memcachedClient')
            }
        }

        if (conf.rest.token.storage.useGorm) {
            tokenAuthenticationProvider(GormTokenAuthenticationProvider) {

            }
        }

        //*/

        if (printStatusMessages) {
            println '... finished configuring Spring Security REST\n'
        }

    }


}
