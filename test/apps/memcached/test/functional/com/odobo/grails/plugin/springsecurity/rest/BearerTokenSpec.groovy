package com.odobo.grails.plugin.springsecurity.rest

import grails.plugins.rest.client.ErrorResponse
import grails.plugins.rest.client.RestResponse
import grails.util.Holders
import spock.lang.IgnoreIf
import spock.lang.IgnoreRest
import spock.lang.Issue

@IgnoreIf({ !Holders.config.grails.plugin.springsecurity.rest.token.validation.useBearerToken })
@Issue("https://github.com/alvarosanchez/grails-spring-security-rest/issues/73")
class BearerTokenSpec extends AbstractRestSpec {

    void "access token response is compliant with the specification"() {
        when:
        RestResponse response = sendCorrectCredentials()

        then:
        response.status == 200
        response.headers.getFirst('Content-Type') == 'application/json;charset=UTF-8'
        response.headers.getFirst('Cache-Control') == 'no-store'
        response.headers.getFirst('Pragma') == 'no-cache'
        response.json.access_token
        response.json.token_type == 'Bearer'

    }

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

    void "Form-Encoded body parameter can be used"() {
        given:
        RestResponse authResponse = sendCorrectCredentials()
        String token = authResponse.json.access_token

        when:
        def response = restBuilder.post("${baseUrl}/secured") {
            contentType 'application/x-www-form-urlencoded'
            body "access_token=${token}".toString()
        }

        then:
        response.status == 200
    }

    void "if credentials are required but missing, the response contains WWW-Authenticate header"() {
        when:
        ErrorResponse response = restBuilder.post("${baseUrl}/secured") {
            contentType 'application/x-www-form-urlencoded'
        }

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

    void "Content-Type 'application/x-www-form-urlencoded' is mandatory when sending form-encoded body parameter requests with the access token"() {
        when:
        ErrorResponse response = restBuilder.post("${baseUrl}/secured") {
            contentType 'text/plain'
        }

        then:
        response.status == 400
        response.responseHeaders.getFirst('WWW-Authenticate') == 'Bearer error="invalid_request"'
    }

    void "GET HTTP method must not be used when sending form-encoded body parameter requests with the access token"() {
        when:
        ErrorResponse response = restBuilder.get("${baseUrl}/secured") {
            contentType 'application/x-www-form-urlencoded'
        }

        then:
        response.status == 400
        response.responseHeaders.getFirst('WWW-Authenticate') == 'Bearer error="invalid_request"'
    }

}