package com.odobo.grails.plugin.springsecurity.rest

import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

class RestTokenReaderSpec extends Specification {

    def tokenReader = new RestTokenReader()
    def request = new MockHttpServletRequest()


    def "token value can be read from a custom header in a #method request"() {
        def token  = 'mytokenvalue'
        def header = 'mycustomheader'

        tokenReader.headerName = header
        request.addHeader( header, token )
        request.method = method

        expect:

        tokenReader.findToken( request ) == token

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

}
