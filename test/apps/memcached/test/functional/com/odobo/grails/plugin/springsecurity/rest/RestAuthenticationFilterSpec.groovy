package com.odobo.grails.plugin.springsecurity.rest

import geb.spock.GebReportingSpec
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

class RestAuthenticationFilterSpec extends GebReportingSpec {

    @Shared
    RestBuilder restBuilder = new RestBuilder()

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
}
