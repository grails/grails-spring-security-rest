package memcached

import grails.test.spock.IntegrationSpec
import net.spy.memcached.MemcachedClient
import spock.lang.Unroll

class MemcachedSpec extends IntegrationSpec {

    MemcachedClient memcachedClient

    @Unroll
	void "Memcached connection works for storing #key's"() {

        when:
        memcachedClient.set(key, 3600, object)

        then:
        memcachedClient.get(key) == object

        where:
        key         | object
        'String'    | 'My cool string value'
        'Date'      | new Date()
	}
}
