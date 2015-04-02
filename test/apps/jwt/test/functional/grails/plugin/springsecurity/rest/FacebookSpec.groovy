package grails.plugin.springsecurity.rest

import geb.spock.GebReportingSpec
import grails.plugin.springsecurity.SpringSecurityUtils
import spock.lang.IgnoreIf

@IgnoreIf({ !SpringSecurityUtils.securityConfig.rest.oauth.facebook || !System.getenv('FB_PASSWORD') })
class FacebookSpec extends GebReportingSpec {

    void "it can sign users in with Facebook"() {
        when: "a user clicks on a 'Sign In with Facebook' button"
        go "/jwt/oauth/authenticate/facebook"

        then: "its redirected to Facebook Sign In page"
        FacebookSignInPage facebookSignInPage = at FacebookSignInPage

        when: "credentials are entered"
        facebookSignInPage.login 'open_pmazedy_user@tfbnw.net', System.getenv('FB_PASSWORD')

        then: "is redirected to the frontend callback URL, with a token"
        FrontendCallbackPage frontendCallbackPage = at FrontendCallbackPage
        frontendCallbackPage.jsUrl.contains("token")
    }

}
