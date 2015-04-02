package grails.plugin.springsecurity.rest

import geb.Page

class FrontendCallbackPage extends Page {

    static at = {
        jsUrl.startsWith "http://example.org/"
    }

    static content = {
        jsUrl { js."window.document.location.toString()" }
    }
}
