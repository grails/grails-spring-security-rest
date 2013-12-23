grails.project.work.dir = 'target'

def conf = grails.plugin.springsecurity.SpringSecurityUtils.securityConfig

grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo 'http://repo.spring.io/milestone' // TODO remove
    }

    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0", {
            export = false
        }

        compile 'net.spy:spymemcached:2.10.3', {
            if (!conf.rest.token.storage.useMemcached) {
                export = false
            }
        }
    }

    plugins {
        compile ':spring-security-core:2.0-RC2'

        build(":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }

        test(":spock:0.7") {
            exclude("spock-grails-support")
            export = false
        }
    }
}
