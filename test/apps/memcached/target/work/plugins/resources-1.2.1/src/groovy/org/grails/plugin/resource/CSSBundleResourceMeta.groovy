package org.grails.plugin.resource

import org.apache.commons.logging.LogFactory

import org.apache.commons.io.FilenameUtils

/**
 * Holder for info about a resource that is made up of other resources
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class CSSBundleResourceMeta extends AggregatedResourceMeta {

    def log = LogFactory.getLog(this.class)

    @Override
    void beginPrepare(grailsResourceProcessor) {
        initFile(grailsResourceProcessor)
        
        def out = getWriter()
        out << '@charset "UTF-8";\n'
        out.close()

        buildAggregateResource(grailsResourceProcessor)
    }
}