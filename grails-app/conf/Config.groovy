log4j = {

    error  'org.codehaus.groovy.grails',
           'org.springframework'

}

grails.doc.images = new File("src/docs/images")

coverage {
    exclusions = ["**/ApplicationResources*", "**/DefaultRestSecurityConfig*", "**/memcached/*Controller.groovy"]
}