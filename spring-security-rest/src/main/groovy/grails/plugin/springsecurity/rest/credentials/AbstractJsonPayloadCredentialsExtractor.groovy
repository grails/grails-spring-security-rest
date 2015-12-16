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
package grails.plugin.springsecurity.rest.credentials

import com.google.common.io.CharStreams
import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest

/**
 * Base class for JSON-based credentials extractors. It helps building a JSON object from the request body
 */
abstract class AbstractJsonPayloadCredentialsExtractor implements CredentialsExtractor {

    Object getJsonBody(HttpServletRequest httpServletRequest) {
        try {
            String body = CharStreams.toString(httpServletRequest.reader)
            JsonSlurper slurper = new JsonSlurper()
            slurper.parseText(body)
        } catch (exception) {
            [:]
        }
    }
}
