/* Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.rest.credentials

import grails.converters.JSON
import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest

/**
 * Base class for JSON-based credentials extractors. It helps building a JSON object from the request body
 */
@CompileStatic
abstract class AbstractJsonPayloadCredentialsExtractor implements CredentialsExtractor {

    Object getJsonBody(HttpServletRequest httpServletRequest) {
        JSON.parse(httpServletRequest)
    }
}
