package com.odobo.grails.plugin.springsecurity.rest

import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import spock.lang.Specification

/**
 * Created by Svante on 2014-10-15.
 */
@TestFor(RestOauthService)
class RestOauthServiceTest extends Specification {

    def setup() {
        def grailsLinkGenerator = mockFor(LinkGenerator)
        grailsLinkGenerator.demand.link(1){Map params -> "callbackUrl"}
        service.grailsLinkGenerator = grailsLinkGenerator.createMock()
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

    def "it can create a client for DropBox"() {

        given:
        def provider = "dropbox"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.DropBoxClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.DropBoxClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for Foursquare"() {

        given:
        def provider = "foursquare"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.FoursquareClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.FoursquareClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for LinkedIn"() {

        given:
        def provider = "linkedin"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.LinkedIn2Client
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.LinkedIn2Client
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for PayPal"() {

        given:
        def provider = "paypal"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.PayPalClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.PayPalClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for Twitter"() {

        given:
        def provider = "twitter"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.TwitterClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.TwitterClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for Vk"() {

        given:
        def provider = "vk"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.VkClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.VkClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for WindowsLive"() {

        given:
        def provider = "windowslive"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.WindowsLiveClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.WindowsLiveClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for WordPress"() {

        given:
        def provider = "wordpress"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.WordPressClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.WordPressClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

    def "it can create a client for Yahoo"() {

        given:
        def provider = "yahoo"
        providerConfig(provider)
        providerConfig(provider).client = org.pac4j.oauth.client.YahooClient
        providerConfig(provider).key = 'my_key'
        providerConfig(provider).secret = 'my_secret'

        when:
        def client = service.getClient(provider);

        then:
        assert client instanceof org.pac4j.oauth.client.YahooClient
        assert client.key == providerConfig(provider).key
        assert client.secret == providerConfig(provider).secret

    }

}
