package co.tpaga.grails.plugin.springsecurity.rest.apiKey.basic

import co.tpaga.grails.plugin.springsecurity.rest.apiKey.storage.ApiKeyNotFoundException
import groovy.util.logging.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class HTTPBasicAuthApiKeyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    String realm
    Integer statusCode

    /**
     * Sends the proper response code and headers, as defined by RFC26147.
     *
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws javax.servlet.ServletException
     */

    @Override
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {


        if (!response.containsHeader('WWW-Authenticate')) {

            if(e instanceof ApiKeyNotFoundException) {
                log.debug "Bad Credentials: ${e.message}"
                response.addHeader('WWW-Authenticate', "Basic realm=\"$realm\"")
            }
        }

        response.status = statusCode

        log.debug "Sending status code ${response.status} and header WWW-Authenticate: ${response.getHeader('WWW-Authenticate')}"
    }
}
