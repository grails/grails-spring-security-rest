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

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenAuthenticationFailureHandler
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader
import grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import spock.lang.Specification
import spock.lang.Subject

@Subject(BearerTokenAuthenticationFailureHandler)
class BearerTokenAuthenticationFailureHandlerSpec extends Specification {

    def handler = new BearerTokenAuthenticationFailureHandler()
    def mockBearerTokenReader = Mock(BearerTokenReader)

    def setup() {
        handler.tokenReader = mockBearerTokenReader
    }

    def "when bad credentials credentials are sent, it responds 401"() {
        given:
        def request  = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()

        when:
        def exception = new AuthenticationCredentialsNotFoundException('No credentials :-(')
        handler.onAuthenticationFailure(request, response, exception)

        then:
        response.status == 401
        response.getHeader( 'WWW-Authenticate' ) == 'Bearer'
    }

    def "it will send a 401 status and WWW-Authenticate header with an error param when credentials are invalid"() {
        given:
        def request  = new MockHttpServletRequest()
        def response = new MockHttpServletResponse()

        when:
        def exception = new TokenNotFoundException('Bad token :-(')
        handler.onAuthenticationFailure(request, response, exception)

        then:
        1 * mockBearerTokenReader.findToken(request) >> new AccessToken("wrongToken")
        response.status == 401
        response.getHeader('WWW-Authenticate') == 'Bearer error="invalid_token"'
    }

}
