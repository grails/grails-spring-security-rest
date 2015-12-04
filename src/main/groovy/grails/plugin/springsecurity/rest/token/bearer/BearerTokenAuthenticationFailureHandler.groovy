/*
 * Copyright 2013-2015 Alvaro Sanchez-Mariscal <alvaro.sanchezmariscal@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package grails.plugin.springsecurity.rest.token.bearer

import grails.plugin.springsecurity.rest.token.AccessToken
import groovy.util.logging.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles authentication failure when BearerToken authentication is enabled.
 */
@Slf4j
class BearerTokenAuthenticationFailureHandler implements AuthenticationFailureHandler {

    BearerTokenReader tokenReader

    /**
     * Sends the proper response code and headers, as defined by RFC6750.
     *
     * @param request
     * @param response
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        String headerValue
        AccessToken accessToken = tokenReader.findToken(request)

        if (accessToken) {
            headerValue = 'Bearer error="invalid_token"'
        } else {
            headerValue = 'Bearer'
        }

        response.addHeader('WWW-Authenticate', headerValue)
        response.status = HttpServletResponse.SC_UNAUTHORIZED

        log.debug "Sending status code ${response.status} and header WWW-Authenticate: ${response.getHeader('WWW-Authenticate')}"
    }
}
