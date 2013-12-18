grails.project.work.dir = 'target'

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
    }

    plugins {
        compile ':spring-security-core:2.0-RC2'
        //compile ':spring-security-cas:2.0-RC1'
        //compile ':spring-security-ldap:2.0-RC2'

        build(":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }
    }
}
