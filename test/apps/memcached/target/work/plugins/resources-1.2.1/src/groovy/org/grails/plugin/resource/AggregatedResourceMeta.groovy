package org.grails.plugin.resource

import org.apache.commons.logging.LogFactory

import org.apache.commons.io.FilenameUtils

/**
 * Holder for info about a resource that is made up of other resources
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class AggregatedResourceMeta extends ResourceMeta {

    def log = LogFactory.getLog(this.class)

    def resources = []
    def inheritedModuleDependencies = new HashSet()
    
    void reset() {
        super.reset()
    }

    boolean containsResource(ResourceMeta r) {
        resources.find { r.sourceUrl == it.sourceUrl }
    }
    
    @Override
    boolean isDirty() {
        resources.any { it.dirty }
    }

    void add(ResourceMeta r, Closure postProcessor = null) {
        r.delegateTo(this)

        if (!containsResource(r)) {
            resources << r
            inheritedModuleDependencies << r.module
        
            // Update our aggregated sourceUrl
            sourceUrl = "${sourceUrl}, ${r.sourceUrl}"
        }

        if (postProcessor) {
            postProcessor(this)
        }
    }

    Writer getWriter() {
        processedFile.newWriter('UTF-8', true)
    }

    protected initFile(grailsResourceProcessor) {
        def commaPos = sourceUrl.indexOf(',') 
        if (commaPos == -1) {
            commaPos = sourceUrl.size()
        }
        actualUrl = commaPos ? sourceUrl[0..commaPos-1] : sourceUrl

        processedFile = grailsResourceProcessor.makeFileForURI(actualUrl)
        processedFile.createNewFile()

        this.contentType = grailsResourceProcessor.getMimeType(actualUrl)
    }

    @Override
    void beginPrepare(grailsResourceProcessor) {
        initFile(grailsResourceProcessor)

        this.originalSize = resources.originalSize.sum()
        
        buildAggregateResource(grailsResourceProcessor)
    }

    void buildAggregateResource(grailsResourceProcessor) {
        def moduleOrder = grailsResourceProcessor.modulesInDependencyOrder

        def newestLastMod = 0
        
        def bundledContent = new StringBuilder()
        
        // Add the resources to the file in the order determined by module dependencies!
        moduleOrder.each { m ->
            resources.each { r ->
                if (r.module.name == m) {
                    // Append to the existing file
                    if (log.debugEnabled) {
                        log.debug "Appending contents of ${r.processedFile} to ${processedFile}"
                    }
                    bundledContent << r.processedFile.getText("UTF-8")
                    bundledContent << "\r\n"
                    
                    if (r.originalLastMod > newestLastMod) {
                        newestLastMod = r.originalLastMod
                    }
                }
            }
        }
        
        def out = getWriter()
        out << bundledContent
        out << "\r\n"
        out.close()
        
        this.originalLastMod = newestLastMod
    }
}