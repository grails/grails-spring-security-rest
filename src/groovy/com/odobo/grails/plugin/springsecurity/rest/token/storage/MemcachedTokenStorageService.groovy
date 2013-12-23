package com.odobo.grails.plugin.springsecurity.rest.token.storage

import net.spy.memcached.MemcachedClient
import org.springframework.security.core.userdetails.UserDetails

/**
 * Stores and retrieves tokens in a memcached server. This implementation stores the whole {@link UserDetails} object
 * in memcached, leveraging it is serializable.
 */
class MemcachedTokenStorageService implements TokenStorageService {

    MemcachedClient memcachedClient

    Integer expiration = 3600

    @Override
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def userDetails = memcachedClient.get(tokenValue)
        if (userDetails) {
            return userDetails
        } else {
            throw new TokenNotFoundException("Token ${tokenValue} not found")
        }
    }

    @Override
    void storeToken(String tokenValue, UserDetails details) {
        memcachedClient.set tokenValue, expiration, details
    }

}
