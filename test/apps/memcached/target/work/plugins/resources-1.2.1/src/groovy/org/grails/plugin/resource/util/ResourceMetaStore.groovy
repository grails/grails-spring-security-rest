package org.grails.plugin.resource.util

import org.apache.commons.logging.LogFactory

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

import org.grails.plugin.resource.ResourceMeta

/**
 * A special URI -> ResourceMeta store that is non-reentrant and will create
 * entries on demand, causing other threads to wait until the resource has been created
 * if creation has already started
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 */
class ResourceMetaStore {
    def log = LogFactory.getLog(this.class)

    Map latches = new ConcurrentHashMap()
    Map resourcesByURI = new ConcurrentHashMap()
    
    static CLOSED_LATCH = new CountDownLatch(0)
    
    /**
     * Note that this is not re-entrant safe, and is only to be called at app startup, before requests come in
     */
    void addDeclaredResource(Closure resourceCreator) {
        def resource = resourceCreator()
        if (log.debugEnabled) {
            log.debug "Adding declared resource ${resource}"
        }
        
        // It may be null if it is not found / broken in some way
        if (resource) {
            addResource(resource, false)
        }
    }

    /**
     * For development reloading only, evict the meta for a URI so that we can regenerate it
     */
    void evict(String uri) {
        // @todo this probably creates a dev-time race condition where you get 404s
        resourcesByURI.remove(uri)
        latches.remove(uri)
    }
    
    private addResource(resource, boolean adHocResource = false) {
        def uris = new HashSet()

        // Add the actual linking URL to the cache so resourceLink resolves
        // ONLY if its not delegating, or we get a bunch of crap in here / hide the delegated resource
        if (!resource.delegating) {
            if (log.debugEnabled) {
                log.debug "Updating URI to resource cache for ${resource}"
            }
            uris << resource.actualUrl
        }

        // Add the original source url to the cache as well, if it was an ad-hoc resource
        // As the original URL is used, we need this to resolve to the actualUrl for redirect
        uris << resource.sourceUrl
        resource = resource.delegating ? resource.delegate : resource
        
        uris.each { u ->
            if (log.debugEnabled) {
                log.debug "Storing mapping for resource URI $u to ${resource}"
            }
            resourcesByURI[u] = resource
            latches[u] = CLOSED_LATCH // so that future calls for alternative URLs succeed
        }
    }
    
    /** 
     * A threadsafe synchronous method to get an existing resource or create an ad-hoc resource
     */
    ResourceMeta getOrCreateAdHocResource(String uri, Closure resourceCreator) {
        if (log.debugEnabled) {
            log.debug "getOrCreateAdHocResource for ${uri}"
        }

        def latch = latches.get(uri)

        if (latch == null) {
            if (log.debugEnabled) {
                log.debug "getOrCreateAdHocResource for ${uri}, latch is null"
            }
            def thisLatch = new CountDownLatch(1)
            def otherLatch = latches.putIfAbsent(uri, thisLatch)
            if (otherLatch == null) {
                // process resource
                def resource
                try {
                    if (log.debugEnabled) {
                        log.debug "getOrCreateAdHocResource for ${uri}, creating resource as not found"
                    }
                    resource = resourceCreator()
                    if (log.debugEnabled) {
                        log.debug "Creating resource for URI $uri returned ${resource}"
                    }
                } catch (Throwable t) {
                    thisLatch.countDown() // reset this in case anyone else has reference to it
                    latches.remove(uri) // Ditch the latch, so that next attempt will try again in case we are mid-reload/init
                    if (t instanceof FileNotFoundException) {
                        log.warn t.message
                    } else {
                        throw t
                    }
                }

                // It may be null if it is not found / broken in some way
                if (resource) {
                    addResource(resource, true)
                }
                
                // indicate that we are done
                thisLatch.countDown()
                return resource                
            } else {
                if (log.debugEnabled) {
                    log.debug "getOrCreateAdHocResource for ${uri}, waiting for latch, another thread has crept in and is creating resource"
                }
                otherLatch.await()
                return resourcesByURI[uri]
            }
        } else {
            if (log.debugEnabled) {
                log.debug "getOrCreateAdHocResource for ${uri}, waiting for latch, another thread is creating resource..."
            }
            latch.await()
            if (log.debugEnabled) {
                log.debug "getOrCreateAdHocResource for ${uri}, done waiting for latch, another thread created resource already"
            }
            return resourcesByURI[uri]
        }
    }
    
    def keySet() {
        resourcesByURI.keySet()
    }
    
    def getAt(String key) {
        resourcesByURI[key]
    }
}