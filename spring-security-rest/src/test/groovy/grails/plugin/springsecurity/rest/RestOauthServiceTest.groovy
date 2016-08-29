/*
 * Copyright 2013-2016 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
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

import grails.test.mixin.TestFor
import grails.web.mapping.LinkGenerator
import org.pac4j.oauth.client.*
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by Svante on 2014-10-15.
 */
@TestFor(RestOauthService)
class RestOauthServiceTest extends Specification {

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
        providerConfig(provider).client = org.pac4j.oauth.client.CasOAuthWrapperClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'
        providerConfig(provider).casOAuthUrl = 'cas_oauth_url'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.CasOAuthWrapperClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

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
        assert client.class == clientClass
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

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
