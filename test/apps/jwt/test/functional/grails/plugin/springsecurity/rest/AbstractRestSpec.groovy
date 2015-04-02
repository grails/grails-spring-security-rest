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

import grails.plugins.rest.client.RestBuilder
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractRestSpec extends Specification {


    @Shared
    ConfigObject config = new ConfigSlurper().parse(new File('grails-app/conf/Config.groovy').toURL())

    @Shared
    RestBuilder restBuilder = new RestBuilder()

    @Shared
    String baseUrl = "http://localhost:8080/jwt"

    def sendWrongCredentials() {
        if (config.grails.plugin.springsecurity.rest.login.useRequestParamsCredentials == true) {
            restBuilder.post("${baseUrl}/api/login?username=foo&password=bar")
        } else {
            restBuilder.post("${baseUrl}/api/login") {
                json {
                    username = 'foo'
                    password = 'bar'
                }
            }
        }
    }

    def sendCorrectCredentials() {
        if (config.grails.plugin.springsecurity.rest.login.useRequestParamsCredentials == true) {
            restBuilder.post("${baseUrl}/api/login?username=jimi&password=jimispassword")
        } else {
            restBuilder.post("${baseUrl}/api/login") {
                json {
                    username = 'jimi'
                    password = 'jimispassword'
                }
            }
        }
    }

}