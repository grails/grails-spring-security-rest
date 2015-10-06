package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.error.CallbackErrorHandler
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.rendering.AccessTokenJsonRenderer
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.test.spock.IntegrationSpec
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Issue

@Log4j
@Issue("https://github.com/alvarosanchez/grails-spring-security-rest/issues/237")
class RestOauthControllerSpec extends IntegrationSpec {

    private RestOauthController controller

    CallbackErrorHandler callbackErrorHandler
    RestOauthService restOauthService
    GrailsApplication grailsApplication

    TokenStorageService tokenStorageService
    TokenGenerator tokenGenerator
    AccessTokenJsonRenderer accessTokenJsonRenderer

    /**
     * The frontend callback URL stored in config.groovy
     */
    private frontendCallbackBaseUrl = "http://config.com/welcome#token="

    def setup() {
        grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl = { String tokenValue ->
            frontendCallbackBaseUrl + tokenValue
        }

        controller = new RestOauthController(
                callbackErrorHandler: callbackErrorHandler,
                restOauthService: restOauthService,
                grailsApplication: grailsApplication,
                tokenStorageService: tokenStorageService,
                tokenGenerator: tokenGenerator,
                accessTokenJsonRenderer: accessTokenJsonRenderer
        )
    }

    private Exception stubService(Exception caughtException) {
        def stubRestOauthService = Stub(RestOauthService)
        stubRestOauthService.storeAuthentication(*_) >> { throw caughtException }
        controller.restOauthService = stubRestOauthService
        caughtException
    }

    private String getExpectedUrl(Exception exception, expectedErrorValue, String baseUrl = frontendCallbackBaseUrl) {
        def message = exception.message
        "${baseUrl}&error=${expectedErrorValue}&message=${message}&error_description=${message}&error_code=${exception.class.simpleName}"
    }

    def 'UsernameNotFoundException caught within callback action with frontend URL in Config.groovy'() {

        def caughtException = stubService(new UsernameNotFoundException('message'))
        controller.params.provider = "google"

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, 403)
        controller.response.redirectedUrl == expectedUrl
    }

    def 'UsernameNotFoundException caught within callback action with frontend URL in session'() {

        def caughtException = stubService(new UsernameNotFoundException('message'))
        def frontendCallbackBaseUrlSession = "http://session.com/welcome#token="
        controller.session[controller.CALLBACK_ATTR] = frontendCallbackBaseUrlSession
        controller.params.provider = "google"

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, 403, frontendCallbackBaseUrlSession)
        controller.response.redirectedUrl == expectedUrl
    }
}
