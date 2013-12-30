grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {        
        grailsCentral()
        grailsRepo "http://grails.org/plugins"
        mavenCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
//        build 'org.codehaus.gpars:gpars:0.12'
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
    }
    plugins {        
        provided(":webxml:1.4.1") 
        compile(":tomcat:$grailsVersion") {
            export = false
        }
        compile(":release:2.2.1", ':rest-client-builder:1.0.3') {
            export = false
        }
        test(":spock:0.7") {
            exclude "spock-grails-support"
        }
    }
}
