package org.grails.plugin.resource

import org.grails.plugin.resource.mapper.MapperPhase

/**
 * This mapper is the first phase of CSS rewriting.
 *
 * It will find any relative URIs in the CSS and convert them to a "resource:<originalURI-made-absolute>" 
 * so that later after mappers have been applied, the URIs can be fixed up and restored to URIs relative to the
 * new CSS output file's location. For example a bundle or "hashandcache" mapper may move the CSS file to a completely
 * different place, thus breaking all the relative links to images.
 *
 * @see CSSRewriter mapper for phase 2 of the process.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class CSSPreprocessorResourceMapper {

    def phase = MapperPhase.LINKNORMALISATION

    static defaultIncludes = ['**/*.css']

    def grailsResourceProcessor
    
    /**
     * Find all url() and fix up the url if it is not absolute
     * NOTE: This needs to run after any plugins that move resources around, but before any that obliterate
     * the content i.e. before minify or gzip
     */
    def map(resource, config) {
        if (resource instanceof AggregatedResourceMeta) {
            if (log.debugEnabled) {
                log.debug "CSS Preprocessor skipping ${resource} because it is aggregated (already processed each file in it)"
            }
            return null
        }
        
        def processor = new CSSLinkProcessor()
        
        if (log.debugEnabled) {
            log.debug "CSS Preprocessor munging ${resource}"
        }

        processor.process(resource, grailsResourceProcessor) { prefix, originalUrl, suffix ->
            
            if (log.debugEnabled) {
                log.debug "CSS Preprocessor munging url $originalUrl"
            }
            
            // We don't do absolutes or full URLs - perhaps we should do "/" at some point? If app 
            // is mapped to root context then some people might do this but its lame
            // Also skip already-processed resources (i.e. bundled CSS)
            if (!URLUtils.isRelativeURL(originalUrl)) {
                if (log.debugEnabled) {
                    log.debug "CSS Preprocessor leaving $originalUrl as is"
                }
                return "${prefix}${originalUrl}${suffix}"
            }

            def uri
            try {
                uri = 'resource:'+URLUtils.relativeURI(resource.originalUrl, originalUrl)
                if (log.debugEnabled) {
                    log.debug "CSS Preprocessor converted $originalUrl to $uri temporarily"
                }
            } catch (URISyntaxException sex) {
                if (log.warnEnabled) {
                    log.warn "Cannot resolve CSS resource [${originalUrl}] relative to [${resource.originalUrl}], leaving link as is: ${originalUrl}"
                }
            }

            if (uri) {
                if (uri.indexOf('/../') >= 0) {
                    if (log.debugEnabled) {
                        log.debug "CSS Preprocessor falling back to $originalUrl because $uri is above root"
                    }
                    uri = originalUrl // Fall back to original, its above processed root
                }
                if (log.debugEnabled) {
                    log.debug "Calculated absoluted URI of CSS resource [$originalUrl] as [$uri]"
                }
                return "${prefix}${uri}${suffix}"
            } else {
                return "${prefix}${originalUrl}${suffix}"
            }

        }
        return null
    }
}