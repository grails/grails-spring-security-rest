package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.RestResponse
import grails.util.Holders
import spock.lang.IgnoreIf

@IgnoreIf({ Holders.config.grails.plugin.springsecurity.rest.token.validation.useBearerToken })
class ValidateEndpointSpec extends AbstractRestSpec {

    void "calling /api/validate with a valid token returns a JSON representation"() {
        given:
        RestResponse authResponse = sendCorrectCredentials()
        String token = authResponse.json.access_token

        when:
        def response = restBuilder.get("${baseUrl}/api/validate") {
            header 'X-Auth-Token', token
        }

        then:
        response.status == 200
        response.json.username == 'jimi'
        response.json.access_token
        response.json.roles.size() == 2
    }

    void "calling /api/validate with an invalid token returns 401"() {
        when:
        def response = restBuilder.get("${baseUrl}/api/validate") {
            header 'X-Auth-Token', 'something-else'
        }

        then:
        response.status == 401
    }

    void "calling /api/validate without token returns 401"() {
        when:
        def response = restBuilder.get("${baseUrl}/api/validate")

        then:
        response.status == 401
    }


}