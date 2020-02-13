package grails.plugin.springsecurity.rest

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.http.HttpServletRequest

/**
 * Determines whether a given request matches against a configured endpoint URL
 *
 * @author Álvaro Sánchez-Mariscal
 */
@Slf4j
@CompileStatic
class SpringSecurityRestFilterRequestMatcher implements RequestMatcher {

    private String endpointUrl

    SpringSecurityRestFilterRequestMatcher(String endpointUrl) {
        this.endpointUrl = endpointUrl
    }

    @Override
    boolean matches(HttpServletRequest request) {
        String actualUri =  request.requestURI - request.contextPath
        log.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"
        return actualUri == endpointUrl
    }

}
