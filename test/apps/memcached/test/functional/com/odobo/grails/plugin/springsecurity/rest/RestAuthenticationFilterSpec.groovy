package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class RestAuthenticationFilterSpec extends Specification {

    @Shared
    RestBuilder restBuilder = new RestBuilder()

    @Shared
    String baseUrl = "http://localhost:8080/memcached"

    @Unroll
	void "#httpMethod requests without parameters generate #statusCode responses"() {

        when:
        def response = restBuilder."${httpMethod}"("${baseUrl}/login")

        then:
        response.status == statusCode

        where:
        httpMethod  | statusCode
        'get'       | 405
        'post'      | 400
        'put'       | 405
        'delete'    | 405
	}


    @Unroll
    void "the filter is only applied to the configured URL"() {
        when:
        def response = restBuilder."${httpMethod}"("${baseUrl}/nothingHere")

        then:
        response.status == 403      // all URLs are locked down by default

        where:
        httpMethod << ['get', 'post', 'put', 'delete']

    }

    void "authentication attempt with wrong credentials returns a failure status code"() {
        when:
        def response = restBuilder.post("${baseUrl}/login?username=foo&password=bar")

        then:
        response.status == 403
    }

    void "authentication attempt with correct credentials returns a valid status code"() {
        when:
        RestResponse response = restBuilder.post("${baseUrl}/login?username=jimi&password=jimispassword")

        then:
        response.status == 200
        response.json.username == 'jimi'
        response.json.token
        response.json.roles.size() == 2
    }
}
