/* Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.authentication.RestAuthenticationEventPublisher
import grails.plugin.springsecurity.rest.error.DefaultCallbackErrorHandler
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.testing.web.controllers.ControllerUnitTest
import groovy.transform.InheritConstructors
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Issue
import spock.lang.Specification

import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

@Issue("https://github.com/grails/grails-spring-security-rest/issues/237")
class RestOauthControllerSpec extends Specification implements ControllerUnitTest<RestOauthController> {

    /**
     * The frontend callback URL stored in config.groovy
     */
    private frontendCallbackBaseUrl = "http://config.com/welcome#token="

    def setup() {
        grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl = { String tokenValue ->
            frontendCallbackBaseUrl + tokenValue
        }

        params.provider = "google"
    }

    private Exception injectDependencies(Exception caughtException) {
        def stubRestOauthService = Stub(RestOauthService)
        stubRestOauthService.storeAuthentication(*_) >> { throw caughtException }
        controller.restOauthService = stubRestOauthService
        controller.callbackErrorHandler = new DefaultCallbackErrorHandler()
        caughtException
    }

    private String getExpectedUrl(Exception exception, expectedErrorValue, String baseUrl = frontendCallbackBaseUrl) {
        def message = exception.message
        "${baseUrl}&error=${expectedErrorValue}&message=${message}&error_description=${message}&error_code=${exception.class.simpleName}"
    }

    def 'UsernameNotFoundException caught within callback action with frontend closure URL in Config.groovy'() {

        def caughtException = injectDependencies(new UsernameNotFoundException('message'))

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, FORBIDDEN.value())
        response.redirectedUrl == expectedUrl
    }

    def 'UsernameNotFoundException caught within callback action with frontend string URL in Config.groovy'() {

        def caughtException = injectDependencies(new UsernameNotFoundException('message'))
        grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl = frontendCallbackBaseUrl

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, FORBIDDEN.value())
        response.redirectedUrl == expectedUrl
    }

    def 'UsernameNotFoundException caught within callback action with frontend URL in session'() {

        def caughtException = injectDependencies(new UsernameNotFoundException('message'))
        def frontendCallbackBaseUrlSession = "http://session.com/welcome#token="
        request.session[controller.CALLBACK_ATTR] = frontendCallbackBaseUrlSession

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, FORBIDDEN.value(), frontendCallbackBaseUrlSession)
        response.redirectedUrl == expectedUrl
    }

    def 'Non-UsernameNotFoundException with cause that has code caught within callback action'() {

        def caughtException = injectDependencies(new ExceptionWithCodedCause('message'))

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, 'cause.code')
        response.redirectedUrl == expectedUrl
    }

    def 'Non-UsernameNotFoundException without cause caught within callback action'() {

        def caughtException = injectDependencies(new Exception('message'))

        when:
        controller.callback()

        then:
        String expectedUrl = getExpectedUrl(caughtException, INTERNAL_SERVER_ERROR.value())
        response.redirectedUrl == expectedUrl
    }

    void "it publishes RestTokenCreationEvent's"() {
        given:
        TokenStorageService stubbedTokenStorageService = Stub(TokenStorageService)
        stubbedTokenStorageService.loadUserByToken(_) >> new User('foo', '', [])
        controller.tokenStorageService = stubbedTokenStorageService

        def stubbedTokenGenerator = [ generateAccessToken: { u,b -> new AccessToken('accessToken') }]
        controller.tokenGenerator = stubbedTokenGenerator

        controller.authenticationEventPublisher = Mock(RestAuthenticationEventPublisher)

        when:
        params.grant_type = 'refresh_token'
        params.refresh_token = 'refresh_token'
        request.method = 'POST'
        controller.accessToken()

        then:
        1 * controller.authenticationEventPublisher.publishTokenCreation(_)
    }
}

@InheritConstructors
class ExceptionWithCodedCause extends Exception {

    String getCode() {
        'cause.code'
    }

    @Override
    synchronized Throwable getCause() {
        return this
    }
}
