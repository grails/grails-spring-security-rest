package com.odobo.grails.plugin.springsecurity.rest.token.bearer

import groovy.util.logging.Slf4j
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandlerImpl

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class BearerTokenAccessDeniedHandler extends AccessDeniedHandlerImpl {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws java.io.IOException, javax.servlet.ServletException {
        response.addHeader('WWW-Authenticate', 'Bearer error="insufficient_scope"')
        super.handle(request, response, accessDeniedException)
    }
}
