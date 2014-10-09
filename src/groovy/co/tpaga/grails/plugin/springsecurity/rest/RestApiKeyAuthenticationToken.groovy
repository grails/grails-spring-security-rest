package co.tpaga.grails.plugin.springsecurity.rest

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Holds the API token, passed by the client via a custom HTTP header
 */
class RestApiKeyAuthenticationToken extends UsernamePasswordAuthenticationToken {

    String apiKeyValue

    RestApiKeyAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String apiKeyValue) {
        super(principal, credentials, authorities)
        this.apiKeyValue = apiKeyValue
    }

    RestApiKeyAuthenticationToken(String apiKeyValue) {
        super("N/A", "N/A")
        this.apiKeyValue = apiKeyValue
    }

    RestApiKeyAuthenticationToken(String apiKeyValue, Collection<? extends GrantedAuthority> authorities) {
        super("N/A", "N/A")
        this.apiKeyValue = apiKeyValue
    }

}
