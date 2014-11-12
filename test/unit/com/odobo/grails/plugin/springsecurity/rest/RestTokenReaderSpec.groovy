package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.reader.HttpHeaderTokenReader
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification
import spock.lang.Unroll

class RestTokenReaderSpec extends Specification {

    def tokenReader = new HttpHeaderTokenReader()
    def request = new MockHttpServletRequest()
    def response = new MockHttpServletResponse()


    @Unroll
    def "token value can be read from a custom header in a #method request"() {
        def token  = 'mytokenvalue'
        def header = 'mycustomheader'

        tokenReader.headerName = header
        request.addHeader( header, token )
        request.method = method

        expect:
        tokenReader.findToken(request) == token

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

}
