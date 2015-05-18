package grails.plugin.springsecurity.rest

import grails.plugin.springsecurity.rest.token.AccessToken
import org.springframework.context.ApplicationEvent

class RestAuthenticationSuccessEvent extends ApplicationEvent {
    AccessToken accessToken

    RestAuthenticationSuccessEvent(Object source) {
        super(source)
        this.accessToken = (AccessToken)source
    }
}
