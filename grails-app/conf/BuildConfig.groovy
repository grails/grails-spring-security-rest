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
        compile 'net.spy:spymemcached:2.11.6'
        compile 'com.google.guava:guava-io:r03'
        compile 'org.pac4j:pac4j-core:1.6.0'
        compile 'org.pac4j:pac4j-oauth:1.6.0'
        compile 'com.nimbusds:nimbus-jose-jwt:3.9'

        build("com.lowagie:itext:2.0.8") { excludes "bouncycastle:bcprov-jdk14:138", "org.bouncycastle:bcprov-jdk14:1.38" }

        test 'org.gperfutils:gbench:0.4.3-groovy-2.3'
    }

    plugins {
        compile ':spring-security-core:2.0-RC4'
        runtime(":cors:1.1.6") {
            exclude("spring-security-core")
            exclude("spring-security-web")
        }

        build(":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }

        build(":improx:0.3") {
            export = false
        }

        test(':cache:1.1.1', ':hibernate4:4.3.6.1') {
            export = false
        }
    }
}
