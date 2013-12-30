package org.grails.plugin.resource

import org.apache.commons.logging.LogFactory

import org.apache.commons.io.FilenameUtils

/**
 * Holder for info about a JS resource that is made up of other resources
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 */
class JavaScriptBundleResourceMeta extends AggregatedResourceMeta {

    def log = LogFactory.getLog(this.class)
}