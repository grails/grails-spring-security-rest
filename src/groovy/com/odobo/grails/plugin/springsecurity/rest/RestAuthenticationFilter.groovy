package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.springframework.security.authentication.AuthenticationDetailsSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
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
 * This filter performs the initial authentication. It uses the configured {@link AuthenticationManager} bean, allowing
 * to use any authentication provider defined by other plugins or by the application.
 *
 * If the authentication manager authenticates the request, a token is generated using a {@link TokenGenerator} and
 * stored via {@link TokenStorageService}. Finally, a {@link AuthenticationSuccessHandler} is used to render the REST
 * response to the client.
 */
class RestAuthenticationFilter extends GenericFilterBean {

    String usernameParameter
    String passwordParameter
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

        //Only apply filter to the configured URL
        if (actualUri == endpointUrl) {

            //Only POST is supported
            if (httpServletRequest.method != 'POST') {
                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED)
                return
            }

            String username = request.getParameter(usernameParameter)
            String password = request.getParameter(passwordParameter)

            //Request must contain parameters
            if (!username || !password) {
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST)
                return
            }

            Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(username, password)
            authenticationRequest.details = authenticationDetailsSource.buildDetails(request)

            try {
                Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest)

                if (authenticationResult.authenticated) {

                    SecurityContextHolder.context.setAuthentication(authenticationResult)

                    String tokenValue = tokenGenerator.generateToken()

                    tokenStorageService.storeToken(tokenValue, authenticationResult.principal)

                    RestAuthenticationToken restAuthenticationToken = new RestAuthenticationToken(authenticationResult.principal, authenticationResult.credentials, authenticationResult.authorities, tokenValue)

                    authenticationSuccessHandler.onAuthenticationSuccess(request, response, restAuthenticationToken)

                    return
                }

            } catch (AuthenticationException ae) {
                authenticationFailureHandler.onAuthenticationFailure(request, response, ae)
                return
            }
        }


        chain.doFilter(request, response)
    }
}
