package com.odobo.grails.plugin.springsecurity.rest.token.storage

import groovy.util.logging.Slf4j
import org.springframework.cache.Cache

import javax.annotation.PostConstruct

/**
 * Uses <a href="http://grails.org/plugin/cache">Grails Cache plugin</a> to store and retrieve tokens.
 */
@Slf4j
class GrailsCacheTokenStorageService implements TokenStorageService {

    def grailsCacheManager
    String cacheName

    Cache cache

    @Override
    void storeToken(String tokenValue, Object principal) {
        cache.put(tokenValue, principal)
        log.debug "Stored principal $principal for token ${tokenValue.mask()}"
    }

    @Override
    Object loadUserByToken(String tokenValue) throws TokenNotFoundException {
        def principal = cache.get(tokenValue)?.get()
        if (principal) {
            log.debug "Got principal $principal for token ${tokenValue.mask()}"
            return principal
        }
        def tokenNotFoundMsg = "No principal found for token $tokenValue"
        log.debug tokenNotFoundMsg
        throw new TokenNotFoundException(tokenNotFoundMsg)
    }

    @Override
    void removeToken(String tokenValue) throws TokenNotFoundException {
        cache.evict(tokenValue)
        log.debug "Removed principal for token ${tokenValue.mask()}"
    }

    @PostConstruct
    void init() {
        if (!grailsCacheManager) {
            throw new IllegalStateException('GrailsCacheManager was not injected. ' +
                'Install cache plugin to use this implementation of TokenStorageService')
        }
        if (!cacheName) {
            throw new IllegalStateException('Cache name for TokenStorageService was not injected. ' +
                'Use grails.plugin.springsecurity.rest.token.storage.grailsCacheName to specify a cache name')
        }
        cache = grailsCacheManager.getCache(cacheName)
        if (!cache) {
            throw new IllegalStateException("Could not retrieve a cache for name $cacheName. " +
                "Did you specify a cache '$cacheName' in the cache configuration?")
        }
        log.debug "${this.class.simpleName} initialized successfully"
    }
}
