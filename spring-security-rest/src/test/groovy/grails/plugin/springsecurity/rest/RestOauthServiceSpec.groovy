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

import grails.testing.services.ServiceUnitTest
import grails.web.mapping.LinkGenerator
import org.pac4j.oauth.client.*
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by Svante on 2014-10-15.
 */
class RestOauthServiceSpec extends Specification implements ServiceUnitTest<RestOauthService> {

    def setup() {
        def grailsLinkGenerator = Mock(LinkGenerator)
        1 * grailsLinkGenerator.link(_ as Map) >> "callbackUrl"
        service.grailsLinkGenerator = grailsLinkGenerator
    }

    def providerConfig(String provider) {
        grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
    }

    def "it can create a client for CasOAuthWrapper"() {

        given:
        def provider = "cas"
        providerConfig(provider)
        providerConfig(provider).client = CasOAuthWrapperClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'
        providerConfig(provider).casOAuthUrl = 'cas_oauth_url'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof CasOAuthWrapperClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret
        assert client.casOAuthUrl == providerConfig(provider).casOAuthUrl

    }

    @Unroll
    def "it can create a client for #provider"() {
        given:
        providerConfig(provider)
        providerConfig(provider).client = clientClass
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        client.class == clientClass
        client.key == providerConfig(provider).key
        client.secret == providerConfig(provider).secret

        where:
        provider        | clientClass
        'dropbox'       | DropBoxClient
        'foursquare'    | FoursquareClient
        'linkedin'      | LinkedIn2Client
        'paypal'        | PayPalClient
        'twitter'       | TwitterClient
        'vk'            | VkClient
        'windowslive'   | WindowsLiveClient
        'wordpress'     | WordPressClient
        'yahoo'         | YahooClient
    }


}
