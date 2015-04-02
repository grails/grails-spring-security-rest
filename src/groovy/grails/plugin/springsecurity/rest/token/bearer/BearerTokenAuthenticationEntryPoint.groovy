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
import org.springframework.security.web.AuthenticationEntryPoint

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles prompting the client for authentication when using bearer tokens.
 */
@Slf4j
class BearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    BearerTokenReader tokenReader

    @Override
    void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        AccessToken accessToken = tokenReader.findToken(request)

        if (accessToken) {
            response.addHeader('WWW-Authenticate', 'Bearer error="invalid_token"')
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        } else {
            response.addHeader('WWW-Authenticate', 'Bearer')
            response.status = HttpServletResponse.SC_UNAUTHORIZED
        }

    }
}
