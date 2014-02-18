package com.odobo.grails.plugin.springsecurity.rest.token.storage

import groovy.util.logging.Slf4j
import net.spy.memcached.MemcachedClient
import org.springframework.security.core.userdetails.UserDetails

/**
 * Stores and retrieves tokens in a memcached server. This implementation stores the whole {@link UserDetails} object
 * in memcached, leveraging it is serializable.
 */
@Slf4j
class MemcachedTokenStorageService implements TokenStorageService {

    MemcachedClient memcachedClient

    /** Expiration in seconds */
    Integer expiration = 3600

    Object loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def userDetails = findExistingUserDetails(tokenValue)
        if (userDetails) {
            return userDetails
        } else {
            throw new TokenNotFoundException("Token ${tokenValue} not found")
        }
    }

    void storeToken(String tokenValue, Object principal) {
        log.debug "Storing principal for token: ${tokenValue} with expiration of ${expiration} seconds"
        log.debug "Principal: ${principal}"

        memcachedClient.set tokenValue, expiration, principal
    }

    void removeToken(String tokenValue) throws TokenNotFoundException {
        def userDetails = findExistingUserDetails(tokenValue)
        if (userDetails) {
            memcachedClient.delete tokenValue
        } else {
            throw new TokenNotFoundException("Token ${tokenValue} not found")
        }
    }

    private UserDetails findExistingUserDetails(String tokenValue) {
        log.debug "Searching in Memcached for UserDetails of token ${tokenValue}"
        def userDetails = memcachedClient.get(tokenValue)
        if (userDetails) {
            log.debug "UserDetails found: ${userDetails}"
        } else {
            log.debug "UserDetails not found"
        }
        return userDetails
    }
}
