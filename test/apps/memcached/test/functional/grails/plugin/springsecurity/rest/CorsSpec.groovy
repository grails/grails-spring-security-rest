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
package grails.plugin.springsecurity.rest

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpOptions
import org.apache.http.impl.client.DefaultHttpClient

/**
 * Specification to test CORS support
 *
 * @see https://github.com/alvarosanchez/grails-spring-security-rest/issues/4
 */
class CorsSpec extends AbstractRestSpec {

    void "OPTIONS requests are allowed"() {

        given:
        HttpClient client = new DefaultHttpClient()
        HttpOptions options = new HttpOptions("${baseUrl}/api/login")
        options.addHeader 'Origin', 'http://www.example.com'
        options.addHeader 'Access-Control-Request-Method', 'POST'

        when:
        HttpResponse response = client.execute(options)

        then:
        response.getHeaders('Access-Control-Allow-Origin').first().value == 'http://www.example.com'

    }

}