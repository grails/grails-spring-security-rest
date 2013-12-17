class SpringSecurityRestGrailsPlugin {

    String version = "1.0.0.M1"
    String grailsVersion = "2.0 > *"
    List loadAfter = ['springSecurityCore']
    List pluginExcludes = [
        "grails-app/views/**"
    ]

    String title = "Spring Security REST Plugin"
    String author = "Alvaro Sanchez-Mariscal"
    String authorEmail = "alvaro.sanchez@odobo.com"
    String description = 'Implements authentication for REST APIs based on Spring Security. It uses a token-based workflow'

    String documentation = "http://grails.org/plugin/spring-security-rest"

    String license = "APACHE"
    def organization = [ name: "Odobo Limited", url: "http://www.odobo.com" ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/grails-plugins/grails-spring-security-openid/issues" ]
    def scm = [ url: "https://github.com/alvarosanchez/grails-spring-security-rest" ]

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }


}
