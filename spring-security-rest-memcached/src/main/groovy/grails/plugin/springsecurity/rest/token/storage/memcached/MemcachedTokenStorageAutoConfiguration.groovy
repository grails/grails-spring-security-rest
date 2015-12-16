package grails.plugin.springsecurity.rest.token.storage.memcached

import grails.core.GrailsApplication
import groovy.util.logging.Slf4j
import net.spy.memcached.DefaultHashAlgorithm
import net.spy.memcached.MemcachedClient
import net.spy.memcached.spring.MemcachedClientFactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(MemcachedClient)
@Slf4j
class MemcachedTokenStorageAutoConfiguration {

    @Autowired
    GrailsApplication grailsApplication

    @Bean
    public MemcachedClientFactoryBean memcachedClient() {
        log.debug "Configuring memcachedClient"
        MemcachedClientFactoryBean memcachedClient = new MemcachedClientFactoryBean()
        memcachedClient.with {
            servers = grailsApplication.config.getProperty('grails.plugin.springsecurity.rest.token.storage.memcached.hosts', String)
            protocol = 'BINARY'
            transcoder = new CustomSerializingTranscoder()
            opTimeout = 1000
            timeoutExceptionThreshold = 1998
            hashAlg = DefaultHashAlgorithm.KETAMA_HASH
            locatorType = 'CONSISTENT'
            failureMode = 'Redistribute'
            useNagleAlgorithm = false

        }
        return memcachedClient
    }

    @Bean
    public MemcachedTokenStorageService tokenStorageService(MemcachedClient client) {
        //FIXME not possible to replace the existing tokenStorageService at runtime
        log.debug "Configuring tokenStorageService"
        MemcachedTokenStorageService tokenStorageService = new MemcachedTokenStorageService()
        tokenStorageService.with {
            memcachedClient = client
            expiration = grailsApplication.config.getProperty('grails.plugin.springsecurity.rest.token.storage.memcached.expiration', Integer)
        }
        return tokenStorageService
    }
}
