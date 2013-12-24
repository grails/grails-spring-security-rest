package com.odobo.grails.plugin.springsecurity.rest

import geb.spock.GebReportingSpec
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import spock.lang.Shared

class RestAuthenticationFilterSpec extends GebReportingSpec {

    @Shared
    RestBuilder restBuilder = new RestBuilder()

	void "only post requests are supported"() {

        when:
        def response = restBuilder.get("${baseUrl}/login")

        then:
        response.status == 405
	}
}
