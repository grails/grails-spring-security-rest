package co.tpaga.grails.plugin.springsecurity.rest

import co.tpaga.grails.plugin.springsecurity.rest.token.reader.HTTPBasicApiKeyReader
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

/**
 * Created by sortiz on 6/10/14.
 */

class HTTPBasicApiKeyReaderSpec extends Specification {

    def apiKeyReader = new HTTPBasicApiKeyReader()
    def request = new MockHttpServletRequest()
    def response = new MockHttpServletResponse()


    def "api key value can be read from Authorization header in a #method request"() {
        def token  = 'my_api_key'

        //base64.encode(my_api_key:) includes :

        request.addHeader('Authorization', 'Basic bXlfYXBpX2tleTo=')
        request.method = method

        expect:
        apiKeyReader.findApiKey(request, response) == token

        where:
        method << [ 'GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS' ]
    }

}
