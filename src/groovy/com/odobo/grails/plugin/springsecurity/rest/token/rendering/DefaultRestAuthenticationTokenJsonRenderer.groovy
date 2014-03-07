package com.odobo.grails.plugin.springsecurity.rest.token.rendering

import grails.plugin.springsecurity.SpringSecurityUtils
import com.odobo.grails.plugin.springsecurity.rest.RestAuthenticationToken
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * Generates a JSON response like the following: <code>{"username":"john.doe","token":"1a2b3c4d","roles":["ADMIN","USER"]}</code>
 */
@Slf4j
class DefaultRestAuthenticationTokenJsonRenderer implements RestAuthenticationTokenJsonRenderer {

    String generateJson(RestAuthenticationToken restAuthenticationToken) {
        Assert.isInstanceOf(UserDetails, restAuthenticationToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = restAuthenticationToken.principal

        def conf = SpringSecurityUtils.securityConfig

        String usernameProperty = conf.rest.response.usernamePropertyName
        String tokenProperty = conf.rest.response.tokenPropertyName
        String authoritiesProperty = conf.rest.response.authoritiesPropertyName

        def result = [:]
        result["$usernameProperty"] = userDetails.username
        result["$tokenProperty"] = restAuthenticationToken.tokenValue
        result["$authoritiesProperty"] = userDetails.authorities.collect {GrantedAuthority role -> role.authority }

        def jsonResult = result as JSON

        log.debug "Generated JSON:\n ${jsonResult.toString(true)}"

        return jsonResult.toString()
    }
}
