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