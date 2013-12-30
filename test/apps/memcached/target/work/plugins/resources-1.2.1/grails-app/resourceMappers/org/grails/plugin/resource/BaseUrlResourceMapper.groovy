package org.grails.plugin.resource

import org.grails.plugin.resource.mapper.MapperPhase

/**
 * Mapper that applies an optional base url to resources, e.g. for putting content out to 
 * one or more pull CDNs
 * @author Tomas Lin
 * @since 1.2
 */
class BaseUrlResourceMapper {

    static priority = 0

    static phase = MapperPhase.ABSOLUTISATION

    def map(resource, config) {
        if (config.enabled) {
			def url

            if (isResourceBundle(resource)) {
                verifySameBaseUrlForAllModulesInBundle(resource, config)
            }

            String moduleName = getModuleName(resource)
            if (moduleName && config.modules[moduleName]) {
				url = getUrl(config.modules[moduleName], resource.linkUrl)
			}

			if (!url) {
				url = getUrl(config.default, resource.linkUrl)
			}		
			
            if (url) {
                if (url.endsWith('/')) {
                    url = url[0..-2]
                }
                resource.linkOverride = url + resource.linkUrl
            }
        }
    }
	
	private String getUrl(configItem, linkUrl){
		def url;
		if(configItem){
			if(configItem instanceof java.util.List && configItem.size()>0){
				int cdnNum = getHashedResourceNum(linkUrl, configItem.size());
				url = configItem[cdnNum]
			}
			else{
				url = configItem;
			}
		}
		return url;
	}
	
	private int getHashedResourceNum(String toHash, int maxNum){
		if(toHash.contains('/')){
			toHash = toHash.substring(toHash.lastIndexOf('/'));
		}
		return toHash.hashCode() % (maxNum+1);
	}

    void verifySameBaseUrlForAllModulesInBundle(AggregatedResourceMeta bundle, Map config) {
        def moduleNames = bundle.resources.collect this.&getModuleName
        def baseUrlsForBundleModules = moduleNames.collectEntries { [it, config.modules[it] ?: config.default] }
        def uniqueUrls = baseUrlsForBundleModules.values().unique(false)
        if (uniqueUrls.size() != 1) {
            def bundleName = bundle.resources.first().bundle
            throw new IllegalArgumentException("All modules bundled together must have the same baseUrl override. " +
                    "Offending bundle is [$bundleName] and baseUrl overrides for its' modules are: $baseUrlsForBundleModules")
        }
    }

    private String getModuleName(resource) {
        if (isResourceBundle(resource)) {
            resource = resource.resources.first()
        }
        resource.module?.name
    }

    private boolean isResourceBundle(resource) {
        resource instanceof AggregatedResourceMeta && resource.resources
    }
}
