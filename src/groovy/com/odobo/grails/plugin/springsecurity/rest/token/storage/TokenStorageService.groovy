package com.odobo.grails.plugin.springsecurity.rest.token.storage

import org.springframework.security.core.userdetails.UserDetails

/**
 * Implementations of this interface are responsible to load user information from a token storage system, and to store
 * token information into it.
 */
interface TokenStorageService {

    /**
     * Returns a UserDetails object given the passed token value
     * @throws TokenNotFoundException if no token is found in the storage
     */
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException

    /**
     * Stores a token. It receives the {@link UserDetails} to store any additional information together with the token,
     * like the username associated.
     */
    void storeToken(String tokenValue, UserDetails details)

    /**
     * Removes a token from the storage.
     * @throws TokenNotFoundException if the given token is not found in the storage
     */
    void removeToken(String tokenValue) throws TokenNotFoundException
}
