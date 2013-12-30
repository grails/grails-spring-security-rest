package org.grails.plugin.resource

class ResourceProcessorBatch {
    private List<ResourceMeta> dirtyResources = []
    
    void each(Closure c) {
        for (r in dirtyResources) {
            c(r)
        }
    }
    
    void add(ResourceMeta r) {
        dirtyResources << r
    }

    void add(List resources) {
        dirtyResources.addAll(resources)
    }
}