package com.odobo.grails.plugin.springsecurity.rest

import com.odobo.grails.plugin.springsecurity.rest.token.rendering.RestAuthenticationTokenJsonRenderer
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Generates a JSON response using a {@link RestAuthenticationTokenJsonRenderer}.
 */
class RestAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    RestAuthenticationTokenJsonRenderer renderer

    /**
     * Called when a user has been successfully authenticated.
     *
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during the authentication process.
     */
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.contentType = 'application/json'
        response.characterEncoding = 'UTF-8'
        response << renderer.generateJson(authentication)
    }
}
