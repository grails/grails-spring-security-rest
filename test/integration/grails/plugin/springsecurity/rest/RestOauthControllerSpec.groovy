package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.error.CallbackErrorHandler
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.test.spock.IntegrationSpec
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Log4j
class RestOauthControllerSpec extends IntegrationSpec {

    private RestOauthController controller

    CallbackErrorHandler callbackErrorHandler
    RestOauthService restOauthService
    GrailsApplication grailsApplication

    TokenStorageService tokenStorageService
    TokenGenerator tokenGenerator
    AccessTokenJsonRenderer accessTokenJsonRenderer

    private callbackUrlConfig = "http://config.com/welcome#token="

    def setup() {

        controller = new RestOauthController(
                callbackErrorHandler: callbackErrorHandler,
                restOauthService: restOauthService,
                grailsApplication: grailsApplication,
                tokenStorageService: tokenStorageService,
                tokenGenerator: tokenGenerator,
                accessTokenJsonRenderer: accessTokenJsonRenderer
        )

        grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl = { String tokenValue ->
            callbackUrlConfig + tokenValue
        }
    }

    private Exception stubService(Exception caughtException) {
        def stubRestOauthService = Stub(RestOauthService)
        stubRestOauthService.storeAuthentication(*_) >> { throw caughtException }
        controller.restOauthService = stubRestOauthService
        caughtException
    }

    def 'UsernameNotFoundException caught within callback action with frontend URL in Config.groovy'() {

        def caughtException = stubService(new UsernameNotFoundException('message'))

        when:
        controller.params.provider = "google"
        controller.callback()

        then:
        def message = caughtException.message
        controller.response.redirectedUrl == "${callbackUrlConfig}&error=403&message=${message}&error_description=${message}&error_code=${caughtException.class.simpleName}"
    }

//    def 'UsernameNotFoundException caught within callback action with frontend URL in session'() {
//
//        def callbackUrlSession = { String tokenValue -> "http://session.com/welcome#token=${tokenValue}" }
//        controller.session[controller.CALLBACK_ATTR] = callbackUrlSession
//        def caughtException = stubService(new UsernameNotFoundException('message'))
//
//        when:
//        controller.params.provider = "google"
//        controller.callback()
//
//        then:
//        def message = caughtException.message
//        controller.response.redirectedUrl == "${callbackUrlSession}&error=403&message=${message}&error_description=${message}&error_code=${caughtException.class.simpleName}"
//    }
}
