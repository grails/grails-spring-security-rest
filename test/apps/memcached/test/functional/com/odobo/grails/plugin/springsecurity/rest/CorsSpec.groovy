package com.odobo.grails.plugin.springsecurity.rest

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpOptions
import org.apache.http.impl.client.DefaultHttpClient

/**
 * Specification to test CORS support
 *
 * @see https://github.com/alvarosanchez/grails-spring-security-rest/issues/4
 */
class CorsSpec extends AbstractRestSpec {

    void "OPTIONS requests are allowed"() {

        given:
        HttpClient client = new DefaultHttpClient()
        HttpOptions options = new HttpOptions("${baseUrl}/login")
        options.addHeader 'Origin', 'http://www.example.com'
        options.addHeader 'Access-Control-Request-Method', 'POST'

        when:
        HttpResponse response = client.execute(options)

        then:
        response.getHeaders('Access-Control-Allow-Origin').first().value == 'http://www.example.com'

    }

}