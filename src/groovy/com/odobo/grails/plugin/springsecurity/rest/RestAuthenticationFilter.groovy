package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.credentials.CredentialsExtractor
import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
 * This filter starts the initial authentication flow. It uses the configured {@link AuthenticationManager} bean, allowing
 * to use any authentication provider defined by other plugins or by the application.
 *
 * If the authentication manager authenticates the request, a token is generated using a {@link TokenGenerator} and
 * stored via {@link TokenStorageService}. Finally, a {@link AuthenticationSuccessHandler} is used to render the REST
 * response to the client.
 *
 * If there is an authentication failure, the configured {@link AuthenticationFailureHandler} will render the response.
 */
@Slf4j
class RestAuthenticationFilter extends GenericFilterBean {

    CredentialsExtractor credentialsExtractor

    String endpointUrl

    AuthenticationManager authenticationManager

    AuthenticationSuccessHandler authenticationSuccessHandler
    AuthenticationFailureHandler authenticationFailureHandler

    AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource

    TokenGenerator tokenGenerator
    TokenStorageService tokenStorageService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = request
        HttpServletResponse httpServletResponse = response

        def actualUri =  httpServletRequest.requestURI - httpServletRequest.contextPath

        logger.debug "Actual URI is ${actualUri}; endpoint URL is ${endpointUrl}"

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {
            log.debug "Applying authentication filter to this request"

            //Only POST is supported
            if (httpServletRequest.method != 'POST') {
                log.debug "${httpServletRequest.method} HTTP method is not supported. Setting status to ${HttpServletResponse.SC_METHOD_NOT_ALLOWED}"
                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
                return
            }

            UsernamePasswordAuthenticationToken authenticationRequest = credentialsExtractor.extractCredentials(httpServletRequest)

            //Request must contain parameters
            if (!authenticationRequest.principal || !authenticationRequest.credentials) {
                log.debug "Username and/or password parameters are missing. Setting status to ${HttpServletResponse.SC_BAD_REQUEST}"
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
                return
            }

            authenticationRequest.details = authenticationDetailsSource.buildDetails(request)

            try {

                log.debug "Trying to authenticate the request"
                Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest)

                if (authenticationResult.authenticated) {
                    log.debug "Request authenticated. Storing the authentication result in the security context"
                    log.debug "Authentication result: ${authenticationResult}"

                    SecurityContextHolder.context.setAuthentication(authenticationResult)

                    String tokenValue = tokenGenerator.generateToken()
                    log.debug "Generated token: ${tokenValue}"

                    tokenStorageService.storeToken(tokenValue, authenticationResult.principal)

                    RestAuthenticationToken restAuthenticationToken = new RestAuthenticationToken(authenticationResult.principal, authenticationResult.credentials, authenticationResult.authorities, tokenValue)
                    authenticationSuccessHandler.onAuthenticationSuccess(request, response, restAuthenticationToken)
                }

            } catch (AuthenticationException ae) {
                log.debug "Authentication failed: ${ae.message}"
                authenticationFailureHandler.onAuthenticationFailure(request, response, ae)
            }
        } else {
            chain.doFilter(request, response)
        }


    }
}
