package org.grails.plugin.resource.util

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.util.Environment

/**
 * This is a horrible hack to replicate what we need from g.resource() for pre-Grails 1.4 apps
 * where in Grails 1.4 we now have the wonderful grailsLinkGenerator bean
 *
 * NOTE this is a lame implementation that NEVER adds servletContextPath!
 */
class HalfBakedLegacyLinkGenerator {
    
    def pluginManager
    
    String resource(Map args) {
        getResourceUrl(args)
    }
    

    // ********************* EVIL - I HATE INABILITY TO REUSE! ***********************
    /**
     * Copied from ApplicationTagLib
     */
    String makeServerURL() {
        def u = ConfigurationHolder.config?.grails?.serverURL
        if (!u) {
            // Leave it null if we're in production so we can throw
            if (Environment.current != Environment.PRODUCTION) {
                u = "http://localhost:" +(System.getProperty('server.port') ? System.getProperty('server.port') : "8080")
            }
        }
        return u
    }

    /**
     * Resolve the normal link/resource attributes map (plugin, dir, file) to a link
     * relative to the host (not app context)
     * This is basically g.resource copied and pasted
     */
    def getResourceUrl(Map args) {
        def s = new StringBuilder() // Java 5? bite me

        // Ugly copy and paste from ApplicationTagLib
        def base = args.remove('base')
        if (base) {
            s << base
        } else {
            def abs = args.remove("absolute")
            if (Boolean.valueOf(abs)) {
                def u = makeServerURL()
                if (u) {
                    s << u
                } else {
                    throw new IllegalArgumentException("Attribute absolute='true' specified but no grails.serverURL set in Config")
                }
            }
            else {
                // @todo work out how to get servlet context path
                // For servlets SDK 2.5 you can servletContext.getContextPath()
                s << ''
            }
        }

        if (args.contextPath) {
            s << args.contextPath
        }
        
        def dir = args['dir']
        if (args.plugin) {
            s << pluginManager.getPluginPath(args.plugin) ?: ''
        }
        if (dir) {
            s << (dir.startsWith("/") ?  dir : "/${dir}")
        }
        def file = args['file']
        if (file) {
            s << (file.startsWith("/") || dir?.endsWith('/') ?  file : "/${file}")
        }    
        return s.toString()
    }
    
}