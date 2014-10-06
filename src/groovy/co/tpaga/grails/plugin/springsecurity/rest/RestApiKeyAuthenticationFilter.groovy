package co.tpaga.grails.plugin.springsecurity.rest

import co.tpaga.grails.plugin.springsecurity.rest.token.reader.ApiKeyReader

import grails.plugin.springsecurity.authentication.GrailsAnonymousAuthenticationToken
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Sebastián Ortiz V. <sortiz@tappsi.co>
 */

/**
 * This filter starts the initial authentication flow. It uses the configured {@link AuthenticationManager} bean, allowing
 * to use any authentication provider defined by other plugins or by the application.
 *
 * If the authentication manager authenticates the request using the Api Key sent in the HTTP Authenticate Header.
 * Finally, a {@link AuthenticationSuccessHandler} is used to render the REST
 * response to the client.
 *
 * If there is an authentication failure, the configured {@link AuthenticationFailureHandler} will render the response.
 */
@Slf4j
class RestApiKeyValidationFilter extends GenericFilterBean {


    RestApiKeyAuthenticationProvider restApiKeyAuthenticationProvider
    AuthenticationSuccessHandler authenticationSuccessHandler
    AuthenticationFailureHandler authenticationFailureHandler
    Boolean active
    String validationEndpointUrl
    Boolean enableAnonymousAccess
    ApiKeyReader apiKeyReader


    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = request as HttpServletRequest
        HttpServletResponse httpResponse = response as HttpServletResponse

        try {
            String apiKeyValue = apiKeyReader.findApiKey(httpRequest, httpResponse)
            if (apiKeyValue) {
                log.debug "Api key found: ${apiKeyValue}"

                log.debug "Trying to authenticate the api key"
                RestApiKeyAuthenticationToken authenticationRequest = new RestApiKeyAuthenticationToken(apiKeyValue)
                RestApiKeyAuthenticationToken authenticationResult = restApiKeyAuthenticationProvider.authenticate(authenticationRequest) as RestApiKeyAuthenticationToken

                if (authenticationResult.authenticated) {
                    log.debug "Api key authenticated. Storing the authentication result in the security context"
                    log.debug "Authentication result: ${authenticationResult}"
                    SecurityContextHolder.context.setAuthentication(authenticationResult)

                    processFilterChain(request, response, chain, apiKeyValue, authenticationResult)
                }

            } else {
                log.debug "Api key not found"
                processFilterChain(request, response, chain, apiKeyValue, null)
            }
        } catch (AuthenticationException ae) {
            log.debug "Authentication failed: ${ae.message}"
            authenticationFailureHandler.onAuthenticationFailure(httpRequest, httpResponse, ae)
        }

    }

    private processFilterChain(ServletRequest request, ServletResponse response, FilterChain chain, String tokenValue, RestApiKeyAuthenticationToken authenticationResult) {
        HttpServletRequest httpRequest = request as HttpServletRequest
        HttpServletResponse httpResponse = response as HttpServletResponse

        def actualUri = httpRequest.requestURI - httpRequest.contextPath

        if (!active) {
            log.debug "Api key validation is disabled. Continuing the filter chain"
            chain.doFilter(request, response)
            return
        }

        if (tokenValue) {
            if (actualUri == validationEndpointUrl) {
                log.debug "Validation endpoint called. Generating response."
                authenticationSuccessHandler.onAuthenticationSuccess(httpRequest, httpResponse, authenticationResult)
            } else {
                log.debug "Continuing the filter chain"
                chain.doFilter(request, response)
                //TODO Deauthenticate after the response is send
            }
        } else if (enableAnonymousAccess) {
            log.debug "Anonymous access is enabled"
            Authentication authentication = SecurityContextHolder.context.authentication
            if (authentication && authentication instanceof GrailsAnonymousAuthenticationToken) {
                log.debug "Request is already authenticated as anonymous request. Continuing the filter chain"
                chain.doFilter(request, response)
            } else {
                log.debug "However, request is not authenticated as anonymous"
                throw new AuthenticationCredentialsNotFoundException("Api key is missing")
            }
        } else {
            throw new AuthenticationCredentialsNotFoundException("Api key is missing")
        }

    }
}
