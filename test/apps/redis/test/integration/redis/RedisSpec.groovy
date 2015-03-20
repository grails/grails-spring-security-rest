package redis

import com.odobo.grails.plugin.springsecurity.rest.token.storage.RedisTokenStorageService
import com.odobo.grails.plugin.springsecurity.rest.token.storage.TokenNotFoundException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import spock.lang.Shared
import spock.lang.Specification

class RedisSpec extends Specification {

    @Shared
    RedisTokenStorageService tokenStorageService

    def redisService

    def cleanup() {
        redisService.flushDB()
    }

    void "Objects stored expire after the expiration time"() {
        given:
        tokenStorageService.expiration = 1
        UserDetails principal = new User('username', 'password', [])
        String token = 'abcd'
        tokenStorageService.storeToken(token, principal)
        Thread.sleep(1000)

        when:
        tokenStorageService.loadUserByToken(token)

        then:
        thrown(TokenNotFoundException)
    }

    void "Objects are refreshed when accessed"() {
        given:
        tokenStorageService.expiration = 2
        UserDetails principal = new User('username', 'password', [])
        String token = 'abcd' + System.currentTimeMillis()
        tokenStorageService.storeToken(token, principal)
        Thread.sleep(1000)

        when: "it is accessed within the expiration time"
        Object details = tokenStorageService.loadUserByToken(token)

        then: "it is found, and expiration time reset to 2 sencods"
        details

        when: "it is accessed after one second"
        Thread.sleep(1000)
        tokenStorageService.loadUserByToken(token)

        then: "is still found"
        notThrown(TokenNotFoundException)
    }

}