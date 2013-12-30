import grails.util.Environment

import org.grails.plugin.resource.*

/**
 * Bootstraps the plugin by loading the app resources from config
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ResourcesBootStrap {
 
    def grailsResourceProcessor
    
    def init = { servletContext ->
        /*grailsResourceProcessor.reload()*/
        if (Environment.current == Environment.DEVELOPMENT) {
            grailsResourceProcessor.dumpResources()
        }
    }
    
    def destroy = {
        
    }
}