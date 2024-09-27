package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import grails.plugin.springsecurity.rest.token.storage.memcached.CustomSerializingTranscoder
import grails.plugin.springsecurity.rest.token.storage.memcached.MemcachedTokenStorageService
import grails.plugins.*
import net.spy.memcached.DefaultHashAlgorithm
import net.spy.memcached.spring.MemcachedClientFactoryBean

class SpringSecurityRestMemcachedGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "6.1.1 > *"
    List loadAfter = ['springSecurityRest']
    List pluginExcludes = [
        "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin - Memcached support"
    String author = "Alvaro Sanchez-Mariscal"
    String authorEmail = ""
    String description = 'Implements authentication for REST APIs based on Spring Security. It uses a token-based workflow'

    def profiles = ['web']

    // URL to the plugin's documentation
    String documentation = "https://grails-plugins.github.io/grails-spring-security-rest/"

    // Extra (optional) plugin metadata
    String license = "APACHE"
    def organization = [name: 'Grails', url: 'https://www.grails.org/']

    def issueManagement = [ system: "GitHub", url: "https://github.com/grails/grails-spring-security-rest/issues" ]
    def scm = [ url: "https://github.com/grails/grails-spring-security-rest" ]

    Closure doWithSpring() { {->
        def conf = SpringSecurityUtils.securityConfig
        if (!conf || !conf.active || !conf.rest.active) {
            return
        }

        boolean printStatusMessages = (conf.printStatusMessages instanceof Boolean) ? conf.printStatusMessages : true

        if (printStatusMessages) {
            println '\t... with Memcached support'
        }

        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestMemcachedSecurityConfig'
        conf = SpringSecurityUtils.securityConfig

        Properties systemProperties = System.properties
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger")
        System.setProperties(systemProperties)

        SpringSecurityUtils.registerFilter 'restLogoutFilter', SecurityFilterPosition.LOGOUT_FILTER.order - 1

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

        tokenGenerator(SecureRandomTokenGenerator)

    }}

    void doWithApplicationContext() {
        def conf = SpringSecurityUtils.securityConfig
        if (!conf || !conf.active || !conf.rest.active) {
            return
        }

        applicationContext.getBean(RestAuthenticationProvider).useJwt = false
    }
}
