package org.grails.plugin.resource

import org.grails.plugin.resource.mapper.MapperPhase

/**
 * This mapper creates synthetic AggregatedResourceMeta instances for any bundle
 * names found in the resource declarations, and gathers up info about those resources
 * so that when the bundle itself is requested, the aggregated file is created and returned.
 * 
 * This sets any ResourceMeta to which this mapper applies, to be "delegating" to the new aggregated resource
 * so when those resources are rendered/requested, the bundle URI is written out.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 */
class BundleResourceMapper {
    
    def phase = MapperPhase.AGGREGATION
    
    def grailsResourceProcessor
    
    static MIMETYPE_TO_RESOURCE_META_CLASS = [
        'text/css': CSSBundleResourceMeta,
        'text/javascript': JavaScriptBundleResourceMeta,
        'application/javascript': JavaScriptBundleResourceMeta,
        'application/x-javascript': JavaScriptBundleResourceMeta
    ]
    
    /**
     * Find resources that belong in bundles, and create the bundles, and make the resource delegate to the bundle.
     * Creates a new aggregated resource for the bundle and shoves all the resourceMetas into it.
     * We rely on the smart linking stuff to avoid writing out the same bundle multiple times, so you still have
     * dependencies to the individual resources but these delegate to the aggregated resource, and hence all such
     * resources will return the same link url, and not be included more than once.
     */
    def map(resource, config) {
        def bundleId = resource.bundle
        if (bundleId) {
            def resType = MIMETYPE_TO_RESOURCE_META_CLASS[resource.contentType]
            if (!resType) {
                log.warn "Cannot create a bundle from resource [${resource.sourceUrl}], "+
                    "the content type [${resource.contentType}] is not supported. Set the resource to exclude bundle mapper."
                return
            }

            // Find/create bundle for this extension type
            def bundlename = "bundle-$bundleId.${resource.sourceUrlExtension}"
            def bundleURI = "/${bundlename}"

            def bundleResource = grailsResourceProcessor.findSyntheticResourceById(bundlename)
            if (!bundleResource) {
                // Creates a new resource and empty file
                bundleResource = grailsResourceProcessor.newSyntheticResource(bundleURI, resType)
                bundleResource.id = bundlename
                bundleResource.contentType = resource.contentType
                bundleResource.disposition = resource.disposition
            }

            // After we update this, the resource's linkUrl will return the url of the bundle
            bundleResource.add(resource)
        }
    }
}
