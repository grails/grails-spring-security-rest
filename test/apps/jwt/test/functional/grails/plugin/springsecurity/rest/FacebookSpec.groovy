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
