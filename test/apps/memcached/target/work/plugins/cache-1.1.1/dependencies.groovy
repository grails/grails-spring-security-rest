import grails.util.Environment

grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for gh-pages branch
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
	}

	dependencies {
		build('net.sourceforge.nekohtml:nekohtml:1.9.14') {
			excludes "xml-apis"
			export = false
		}

		test 'org.codehaus.gpars:gpars:1.0.0', {
			export = false
		}
		test 'org.codehaus.jsr166-mirror:jsr166y:1.7.0', {
			export = false
		}
	}

	plugins {
		build(":tomcat:$grailsVersion") {
			export = false
		}
		runtime(":hibernate:$grailsVersion") {
			export = false
		}
		if (Environment.current != Environment.TEST) {
			build ':release:2.2.1', ':rest-client-builder:1.0.3', {
				export = false
			}
		}
		test(':functional-test:1.2.7', ':spock:0.6') {
			export = false
		}
		compile ':webxml:1.4.1'
	}
}
