package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.RestResponse
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import spock.lang.Specification


/**
 * Specification to support CORS requests
 *
 * @see https://github.com/alvarosanchez/grails-spring-security-rest/issues/4
 */
class CorsSpec extends AbstractRestSpec {

    void "OPTIONS requests are allowed"() {

        given:
        RestTemplate restTemplate = new RestTemplate()
        def headers = restTemplate.execute("${baseUrl}/login", HttpMethod.OPTIONS, null, restTemplate.headersExtractor, [])
        def methods = headers['Access-Control-Allow-Methods'].first()

        println methods[0]

        expect:
        methods == 'GET, POST, PUT, DELETE, OPTIONS'

    }

}