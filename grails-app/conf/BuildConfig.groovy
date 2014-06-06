grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'

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
        compile 'net.spy:spymemcached:2.10.3'
        compile 'com.google.guava:guava-io:r03'
        compile 'org.pac4j:pac4j-core:1.5.0'
        compile 'org.pac4j:pac4j-oauth:1.5.0'

        // Latest httpcore and httpmime for Coveralls plugin
        build 'org.apache.httpcomponents:httpcore:4.3.2'
        build 'org.apache.httpcomponents:httpclient:4.3.2'
        build 'org.apache.httpcomponents:httpmime:4.3.3'
    }

    plugins {
        compile ':spring-security-core:2.0-RC3'
        runtime(":cors:1.1.6") {
            exclude("spring-security-core")
            exclude("spring-security-web")
        }

        // Coveralls plugin
        build(':coveralls:0.1.2') {
            export = false
        }
        build(":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }

        test(':cache:1.1.1', ':hibernate:3.6.10.6') {
            export = false
        }
        test(':code-coverage:1.2.7') {
            export = false
        }
    }
}
