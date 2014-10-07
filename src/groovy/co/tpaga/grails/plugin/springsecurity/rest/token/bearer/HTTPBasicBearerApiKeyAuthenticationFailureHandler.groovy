package co.tpaga.grails.plugin.springsecurity.rest.token.bearer

import co.tpaga.grails.plugin.springsecurity.rest.token.storage.ApiKeyNotFoundException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HTTPBasicBearerApiKeyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    String realm

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

        if (response.status == 200) {
            response.status = 401
        }

        log.debug "Sending status code ${response.status} and header WWW-Authenticate: ${response.getHeader('WWW-Authenticate')}"
    }
}
