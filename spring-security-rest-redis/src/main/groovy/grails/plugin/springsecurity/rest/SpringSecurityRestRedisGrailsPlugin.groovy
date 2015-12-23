package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.rest.token.generation.SecureRandomTokenGenerator
import grails.plugin.springsecurity.rest.token.storage.RedisTokenStorageService
import grails.plugins.*

class SpringSecurityRestRedisGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "3.1.0 > *"
    List loadAfter = ['springSecurityRest']
    List pluginExcludes = [
            "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin - Redis support"
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
        if (!conf || !conf.active || !conf.rest.active) {
            return
        }

        boolean printStatusMessages = (conf.printStatusMessages instanceof Boolean) ? conf.printStatusMessages : true

        if (printStatusMessages) {
            println '\t... with Redis support'
        }

        SpringSecurityUtils.loadSecondaryConfig 'DefaultRestRedisSecurityConfig'
        conf = SpringSecurityUtils.securityConfig

        SpringSecurityUtils.registerFilter 'restLogoutFilter', SecurityFilterPosition.LOGOUT_FILTER.order - 1

        tokenStorageService(RedisTokenStorageService) {
            redisService = ref('redisService')
            userDetailsService = ref('userDetailsService')
            expiration = conf.rest.token.storage.redis.expiration
        }

        tokenGenerator(SecureRandomTokenGenerator)
    }}

    void doWithApplicationContext() {
        applicationContext.getBean(RestAuthenticationProvider).useJwt = false
    }

}
