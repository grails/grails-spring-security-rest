package grails.plugin.springsecurity.rest.token.storage

import grails.plugin.cache.GrailsCacheManager
import grails.test.spock.IntegrationSpec
import org.springframework.security.core.userdetails.User

class GrailsCacheTokenStorageServiceIntegrationSpec extends IntegrationSpec {

    def grailsCacheManager

    private service

    void "store a principal for a given token"() {
        given:
        def tokenValue = '12345'
        def principal = new User('foo', 'bar', [])

        when:
        service.storeToken(tokenValue, principal)

        then:
        service.cache.get(tokenValue).get() == principal
    }

    void "load a principal by a given token"() {
        given:
        def tokenValue = '12345'
        def givenPrincipal = new User('foo', 'bar', [])
        service.storeToken(tokenValue, givenPrincipal)

        when:
        def loadedPrincipal = service.loadUserByToken(tokenValue)

        then:
        loadedPrincipal == givenPrincipal
    }

    void "throw token not found exception if cannot load a principal for a given token"() {
        when:
        service.loadUserByToken('token-does-not-exists')

        then:
        thrown(TokenNotFoundException)
    }

    void "remove a principal for a given token"() {
        given:
        def tokenValue = '12345'
        def givenPrincipal = new User('foo', 'bar', [])
        service.storeToken(tokenValue, givenPrincipal)

        when:
        service.removeToken(tokenValue)

        then:
        !service.cache.get(tokenValue)
    }

    void "throw illegal state exeption if grails cache manager was not injected"() {
        given:
        service.grailsCacheManager = null

        when:
        service.init()

        then:
        thrown(IllegalStateException)
    }

    void "throw illegal state exeption if cache name was not injected"() {
        given:
        service.cacheName = null

        when:
        service.init()

        then:
        thrown(IllegalStateException)
    }

    void "throw illegal state exeption if cache was not retrieved"() {
        given:
        service.grailsCacheManager = Mock(GrailsCacheManager)
        1 * service.grailsCacheManager.getCache(service.cacheName) >> null

        when:
        service.init()

        then:
        thrown(IllegalStateException)
    }

    def setup() {
        service = new GrailsCacheTokenStorageService(grailsCacheManager: grailsCacheManager, cacheName: 'tokenStorage')
        service.init()
    }
}
