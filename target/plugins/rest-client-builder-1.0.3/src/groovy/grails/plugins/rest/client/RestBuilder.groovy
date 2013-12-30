package grails.plugins.rest.client

import org.springframework.core.io.*
import org.springframework.web.client.RestTemplate
import org.springframework.http.*
import org.springframework.util.*
import static org.springframework.http.MediaType.*

import grails.converters.*
import grails.web.*
import org.springframework.http.client.*
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import groovy.util.slurpersupport.*
import org.codehaus.groovy.grails.web.json.*

class RestBuilder {

    RestTemplate restTemplate

    RestBuilder() {
        restTemplate = new RestTemplate()
    }

    RestBuilder(Map settings) {
        this()

        def proxyHost = System.getProperty("http.proxyHost")
        def proxyPort = System.getProperty("http.proxyPort")

        if(proxyHost && proxyPort) {
            if(settings.proxy == null) {
                settings.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort.toInteger()))
            }
        }
        if(settings.proxy instanceof Map) {
            def ps = settings.proxy.entrySet().iterator().next()
            if(ps.value) {
                def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ps.key, ps.value.toInteger()))
                settings.proxy = proxy
            }
        }


        restTemplate = new RestTemplate()
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory(settings))
    }

    /**
     * Issues a GET request and returns the response in the most appropriate type
     * @param url The URL
     * @param url The closure customizer used to customize request attributes
     */
    def get(String url, Closure customizer=null) {
        doRequestInternal( url, customizer, HttpMethod.GET)
    }

    /**
     * Issues a PUT request and returns the response in the most appropriate type
     *
     * @param url The URL
     * @param customizer The clouser customizer
     */
    def put(String url, Closure customizer = null) {
        doRequestInternal( url, customizer, HttpMethod.PUT)
    }

    /**
     * Issues a POST request and returns the response
     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    def post(String url, Closure customizer = null) {
        doRequestInternal( url, customizer, HttpMethod.POST)
    }

    /**
     * Issues DELETE a request and returns the response

     * @param url The URL
     * @param customizer (optional) The closure customizer
     */
    def delete(String url, Closure customizer = null) {
        doRequestInternal( url, customizer, HttpMethod.DELETE)
    }

    protected doRequestInternal(String url, Closure customizer, HttpMethod method) {

        def requestCustomizer = new RequestCustomizer()
        if(customizer != null) {
            customizer.delegate = requestCustomizer
            customizer.call()
        }
        try {
            def responseEntity = restTemplate.exchange(url, method,requestCustomizer.createEntity(),
					String, requestCustomizer.getVariables())
            handleResponse(responseEntity)
        }
        catch(org.springframework.web.client.HttpStatusCodeException e) {
            return new ErrorResponse(error:e)
        }
    }
    protected handleResponse(ResponseEntity responseEntity) {
        return new RestResponse(responseEntity: responseEntity)
    }
}
class ErrorResponse {
    @Delegate org.springframework.web.client.HttpStatusCodeException error
    @Lazy String text = {
        error.responseBodyAsString
    }()
    @Lazy JSONElement json = {
        def body = error.responseBodyAsString
        if(body) {
            return JSON.parse(body)
        }
    }()
    @Lazy GPathResult xml = {
        def body = error.responseBodyAsString
        if(body) {
            return XML.parse(body)
        }
    }()     
    
    byte[] getBody() {
        error.responseBodyAsByteArray
    }

    int getStatus() {
        error.statusCode?.value() ?: 200
    }
}
class RestResponse {
    @Delegate ResponseEntity responseEntity
    @Lazy JSONElement json = {
        def body = responseEntity.body
        if(body) {
            return JSON.parse(body)
        }
    }()
    @Lazy GPathResult xml = {
        def body = responseEntity.body
        if(body) {
            return XML.parse(body)
        }
    }()

    @Lazy String text = {
        def body = responseEntity.body
        if(body) {
            return body.toString()
        }
        else {
            responseEntity.statusCode.reasonPhrase
        }
    }()

    int getStatus() {
        responseEntity?.statusCode?.value() ?: 200
    }

}
class RequestCustomizer {
    HttpHeaders headers = new HttpHeaders()
    def body
    MultiValueMap<String, Object> mvm = new LinkedMultiValueMap<String, Object>()
	Map<String, Object> variables = [:]

    // configures basic author
    RequestCustomizer auth(String username, String password) {
        String authStr = "$username:$password"
        String encoded = Base64Codec.encode(authStr)
        headers["Authorization"] = "Basic $encoded".toString()
        return this
    }

    RequestCustomizer contentType(String contentType) {
        headers.setContentType(MediaType.valueOf(contentType))
        return this
    }

    RequestCustomizer accept(String...contentTypes) {
        def list = contentTypes.collect { MediaType.valueOf(it) }
        headers.setAccept(list)
        return this
    }

    RequestCustomizer header(String name, String value) {
        headers[name] = value
        return this
    }

    RequestCustomizer json(Closure callable) {
        def builder = new JSONBuilder()
        callable.resolveStrategy = Closure.DELEGATE_FIRST
        JSON j = builder.build(callable) 
        json(j)
    }

    RequestCustomizer json(JSON json) {
        body = json.toString()
        if(!headers.contentType) {
            contentType "application/json"
        }        
        return this
    }    
    RequestCustomizer json(String json) {
        body = json
        if(!headers.contentType) {
            contentType "application/json"
        }        
        return this
    }       
    RequestCustomizer json(object) {
        def json = object as JSON
        body = json.toString()
        if(!headers.contentType) {
            contentType "application/json"
        }        
        return this
    }      

    RequestCustomizer xml(Closure closure) {
        def b = new groovy.xml.StreamingMarkupBuilder()
        def markup = b.bind(closure)
        def StringWriter sw = new StringWriter()
        markup.writeTo(sw)
        this.body = sw.toString()
        return this
    }

    RequestCustomizer xml(object) {
        def xml = object as XML
        this.body = xml.toString()
        return this
    }
	RequestCustomizer urlVariables(Map<String, Object> variables) {
		if (variables!=null)
			this.variables = variables
		return this
	}

    RequestCustomizer body(content) {
        if(content instanceof JSON) {
            if(!headers.contentType) {
                contentType "application/json"
            }
            this.body = content.toString()
        }
        else {
            this.body = content    
        }
        

        return this
    }

    HttpEntity createEntity() {
        if(mvm) {
            return new HttpEntity(mvm, headers)
        }
        else {
            return new HttpEntity(body, headers)
        }
    }

    void setProperty(String name, value) {
        if(value instanceof File) {
            value = new FileSystemResource(value)
        }
        else if(value instanceof URL) {
            value = new UrlResource(value)
        }
        else if(value instanceof InputStream) {
            value = new InputStreamResource(value)
        }
        mvm[name] = value
    }
}
