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
        Set<HttpMethod> methods = restTemplate.optionsForAllow "${baseUrl}/login", []

        expect:
        methods

    }

}