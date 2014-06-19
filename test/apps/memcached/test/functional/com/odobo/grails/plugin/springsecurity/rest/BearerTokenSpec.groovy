package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.ErrorResponse
import grails.plugins.rest.client.RestResponse
import grails.util.Holders
import spock.lang.IgnoreIf

@IgnoreIf({ !Holders.config.grails.plugin.springsecurity.rest.token.validation.useBearerToken })
class BearerTokenSpec extends AbstractRestSpec {

    void "authorisation header is checked to read token value"() {
        given:
        RestResponse authResponse = sendCorrectCredentials()
        String token = authResponse.json.access_token

        when:
        def response = restBuilder.get("${baseUrl}/secured") {
            header 'Authorization', "Bearer ${token}"
        }

        then:
        response.status == 200
    }

    void "if credentials are required but missing, the response contains WWW-Authenticate header"() {
        when:
        ErrorResponse response = restBuilder.get("${baseUrl}/secured")

        then:
        response.status == 401
        response.responseHeaders.getFirst('WWW-Authenticate') == 'Bearer'
    }

    void "if the token is invalid, it is indicated in the header"() {
        when:
        ErrorResponse response = restBuilder.get("${baseUrl}/secured") {
            header 'Authorization', "Bearer wrongTokenValue"
        }

        then:
        response.status == 401
        response.responseHeaders.getFirst('WWW-Authenticate') == 'Bearer error="invalid_token"'
    }


}