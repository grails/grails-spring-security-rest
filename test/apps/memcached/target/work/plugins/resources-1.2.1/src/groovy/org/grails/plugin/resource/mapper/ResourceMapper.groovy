package org.grails.plugin.resource.mapper

import org.codehaus.groovy.grails.*
import org.grails.plugin.resources.artefacts.ResourceMapperArtefactHandler
import org.grails.plugin.resource.ResourceMeta
import org.springframework.util.AntPathMatcher
import org.slf4j.LoggerFactory
import grails.util.GrailsNameUtils

/**
 * The artefact facade used by the service to communicate with resource mapper artefacts.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ResourceMapper {

    static final DEFAULT_PRIORITY = 0
    static final PATH_MATCHER = new AntPathMatcher()
    
    final artefact
    final config
    final log
    
    @Lazy phase = {
        try {
            artefact.phase
        } catch (MissingPropertyException e) {
            throw new IllegalArgumentException("Resource mapper ${name} must have a phase property defined")
        }
    }()
    
    @Lazy operation = {
        try {
            artefact.operation
        } catch (MissingPropertyException e) {
            null
        }
    }()
    
    @Lazy priority = {
        try {
            artefact.priority
        } catch (MissingPropertyException e) {
            DEFAULT_PRIORITY
        }
    }()
    
    @Lazy name = {
        try {
            artefact.name
        } catch (MissingPropertyException e) {
            GrailsNameUtils.getLogicalName(artefact.class, ResourceMapperArtefactHandler.SUFFIX).toLowerCase()
        }
    }()
    
    @Lazy defaultExcludes = {
        try {
            toStringList(artefact.defaultExcludes)
        } catch (MissingPropertyException e) {
            []
        }
    }()

    @Lazy defaultIncludes = {
        try {
            toStringList(artefact.defaultIncludes)
        } catch (MissingPropertyException e) {
            ['**/*']
        }
    }()

    @Lazy excludes = {
        if (config?.excludes) {
            toStringList(config.excludes)
        } else {
            defaultExcludes
        }
    }()
    
    @Lazy includes = {
        if (config?.includes) {
            toStringList(config.includes)
        } else {
            defaultIncludes
        }
    }()
    
    /**
     * @param artefact an instance of the resource mapper artefact
     * @param mappersConfig the config object that is the config for all mappers
     *                      this object is responsible for getting the specific
     *                      config object for this mapper
     */
    ResourceMapper(artefact, mappersConfig) {
        this.artefact = artefact
        this.config = mappersConfig[getName()]

        // @todo why are we doing this, why isn't logging plugin doing it?
        // Even though we load after logging, it seems to not apply it to our artefacts
        log = LoggerFactory.getLogger('org.grails.plugin.resource.mapper.' + getName())
        artefact.metaClass.getLog = { it }.curry(log)
    }

    boolean invokeIfNotExcluded(ResourceMeta resource) {
        def includingPattern = getIncludingPattern(resource)
        def excludingPattern = getExcludingPattern(resource)
        if (!includingPattern) {
            if (log.debugEnabled) {
                log.debug "Skipping ${resource.sourceUrl} due to includes pattern ${includes} not including it"
            }
            return false
        }
        
        if (excludingPattern) {
            if (log.debugEnabled) {
                log.debug "Skipping ${resource.sourceUrl} due to excludes pattern ${excludes}"
            }
            
            return false
        } else if (resource.excludesMapperOrOperation(name, operation)) {
            if (log.debugEnabled) {
                log.debug "Skipping ${resource.sourceUrl} due to definition excluding mapper"
            }
            
            return false
        } else {
            invoke(resource)
            return true
        }
    }
    
    private invoke(ResourceMeta resource) {
        if (log.debugEnabled) {
            log.debug "Beginning mapping ${resource.dump()}"
        }
        
        try {
            artefact.map(resource, config)
        } catch (MissingMethodException e) {
            if (artefact.class == e.type && e.method == "map") {
                throw new Exception("The resource mapper '$name' does not implement the appropriate map method")
            } else {
                throw e
            }
            
        }
        
        if (log.debugEnabled) {
            log.debug "Done mapping ${resource.dump()}"
        }
    }

    String stripLeadingSlash(s) {
        s.startsWith("/") ? s.substring(1) : s
    }

    String getExcludingPattern(ResourceMeta resource) {
        // The path matcher won't match **/* against a path starting with /, so it makes sense to remove it.
        def sourceUrl = stripLeadingSlash(resource.sourceUrl)

        excludes.find { PATH_MATCHER.match(it, sourceUrl) }
    }
    
    String getIncludingPattern(ResourceMeta resource) {
        def sourceUrl = stripLeadingSlash(resource.sourceUrl)

        includes.find { PATH_MATCHER.match(it, sourceUrl) }
    }
    
    private toStringList(value) {
        value instanceof Collection ? value*.toString() : value.toString()
    }
}