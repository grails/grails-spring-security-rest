package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.RestResponse
import spock.lang.Unroll

class RestAuthenticationFilterSpec extends AbstractRestSpec {

    @Unroll
	void "#httpMethod requests without parameters/JSON generate #statusCode responses"() {

        when:
        def response = sendEmptyRequest(httpMethod)

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
        def response = sendWrongCredentials()

        then:
        response.status == 403
    }

    void "authentication attempt with correct credentials returns a valid status code"() {
        when:
        RestResponse response = sendCorrectCredentials()

        then:
        response.status == 200
        response.json.username == 'jimi'
        response.json.token
        response.json.roles.size() == 2
    }

    private sendEmptyRequest(httpMethod) {
        if (config.grails.plugin.springsecurity.rest.login.useRequestParamsCredentials == true) {
            restBuilder."${httpMethod}"("${baseUrl}/login")
        } else {
            restBuilder."${httpMethod}"("${baseUrl}/login") {
                json {  }
            }
        }
    }
}
