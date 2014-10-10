package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import com.odobo.grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * Generates a JSON response like the following: <code>{"username":"john.doe","token":"1a2b3c4d","roles":["ADMIN","USER"]}</code>.
 * If the principal is an instance of {@link OauthUser}, also "email" ({@link CommonProfile#getEmail()}) and
 * "displayName" ({@link CommonProfile#getDisplayName()}) will be rendered
 */
@Slf4j
class DefaultRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {

    String usernamePropertyName
    String tokenPropertyName
    String authoritiesPropertyName

    Boolean useBearerToken

    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal as UserDetails

        def result = [
            (usernamePropertyName) : userDetails.username,
            (authoritiesPropertyName) : userDetails.authorities.collect {GrantedAuthority role -> role.authority }
        ]

        result["$tokenPropertyName"] = restAuthenticationToken.tokenValue

        if (userDetails instanceof OauthUser) {
            CommonProfile profile = (userDetails as OauthUser).userProfile
            result.with {
                email = profile.email
                displayName = profile.displayName
            }
        }

        if (useBearerToken) {
            result.token_type = 'Bearer'
        }

        def jsonResult = result as JSON

        log.debug "Generated JSON:\n${jsonResult.toString(true)}"

        return jsonResult.toString()
    }
}
