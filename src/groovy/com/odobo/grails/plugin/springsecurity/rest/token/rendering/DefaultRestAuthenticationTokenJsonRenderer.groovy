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

    /**
     * Collect the user details which should be returned with the token, and return them as a map.
     * @param userDetails
     * @return
     */
    protected collectUserProperties( UserDetails userDetails ) {

        def conf = SpringSecurityUtils.securityConfig

        String usernameProperty = conf.rest.token.rendering.usernamePropertyName
        String authoritiesProperty = conf.rest.token.rendering.authoritiesPropertyName

        def result = [
                (usernameProperty) : userDetails.username,
                (authoritiesProperty) : userDetails.authorities.collect {GrantedAuthority role -> role.authority }
        ]

        if (userDetails instanceof OauthUser) {
            CommonProfile profile = (userDetails as OauthUser).userProfile
            result.with {
                email = profile.email
                displayName = profile.displayName
            }
        }

        result
    }

    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal

        def conf = SpringSecurityUtils.securityConfig
        String tokenProperty = conf.rest.token.rendering.tokenPropertyName

        def result = collectUserProperties( userDetails )
        result["$tokenProperty"] = restAuthenticationToken.tokenValue

        def jsonResult = result as JSON

        log.debug "Generated JSON:\n ${jsonResult.toString(true)}"

        return jsonResult.toString()
    }
}
