package redis

import grails.plugin.springsecurity.annotation.Secured

/**
 * Created by mariscal on 11/06/14.
 */
@Secured(['permitAll'])
class AnonymousController {

    def index() {
        render "Hi"
    }
}
