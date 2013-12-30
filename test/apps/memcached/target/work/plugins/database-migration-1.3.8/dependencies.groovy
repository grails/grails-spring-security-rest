grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()

		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile('org.liquibase:liquibase-core:2.0.5') {
			excludes 'junit', 'easymockclassextension', 'ant', 'servlet-api', 'spring'
		}
	}

	plugins {
		build(':release:2.2.1', ':rest-client-builder:1.0.3') {
			export = false
		}

		runtime ":hibernate:$grailsVersion", {
			export = false
		}
	}
}
