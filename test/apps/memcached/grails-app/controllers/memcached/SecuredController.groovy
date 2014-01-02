package memcached

import grails.plugin.springsecurity.annotation.Secured

class SecuredController {

    def springSecurityService

    @Secured(['ROLE_USER'])
    def index() {
        render springSecurityService.principal.username
    }
}
