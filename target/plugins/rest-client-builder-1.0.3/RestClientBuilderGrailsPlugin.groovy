class RestClientBuilderGrailsPlugin {
    // the plugin version
    def version = "1.0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

	def title = 'REST Client Builder Plugin'
	def author = 'Graeme Rocher'
	def authorEmail = 'grocher@vmware.com'
	def description = 'Grails REST Client Builder Plugin'
	def documentation = 'http://grails.org/plugin/rest-client-builder'

	def license = 'APACHE'
	def organization = [name: 'SpringSource', url: 'http://www.springsource.org/']
	def developers = [[name: 'Graeme Rocher', email: 'grocher@vmware.com']]
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPRESTCLIENTBUILDER']
	def scm = [url: 'https://github.com/grails-plugins/grails-rest-client-builder']
}
