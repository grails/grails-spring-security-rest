package co.tpaga.grails.plugin.springsecurity.rest.token.bearer

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Sebastián Ortiz V. <sortiz@tappsi.co>
 */
class HTTPBasicBearerApiKeyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    String realm

    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

            response.addHeader('WWW-Authenticate', "Basic realm=\"$realm\"")
        if (response.status in 200..299) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        }
    }
}
