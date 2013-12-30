package org.grails.plugin.resource

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Holder for info about a module declaration at runtime
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ResourceModule {
    String name
    String cachedMarkup // Saves calling the tags every time
    
    List<ResourceMeta> resources = new CopyOnWriteArrayList<ResourceMeta>()

    List<String> dependsOn = []
    def defaultBundle
    
    
    def pluginManager
    
    private HashSet<String> dispositions

    /**
     * Constructor for testing only
     */
    ResourceModule() {
    }

    ResourceModule(name, svc) {
        this.name = name
        this.pluginManager = svc.pluginManager
        this.defaultBundle = false
    }
    
    ResourceModule(name, Map resourceInfo, defBundle, svc) {
        this(name, svc)
        this.defaultBundle = defBundle
        def args = [:]
        args.putAll(resourceInfo)

        if (args.url == null) {
            throw new IllegalArgumentException("Cannot create resource with arguments ${args}, url is not set")
        }

        if (args.url instanceof Map) {
            args.url = svc.buildLinkToOriginalResource(args.url)
        }
        this.resources << newResourceFromArgs(args, svc, true)
        lockDown()
    }

    ResourceModule(name, List resourceInfoList, defBundle, svc) {
        this(name, svc)
        this.defaultBundle = defBundle
        resourceInfoList.each { i ->
            if (i instanceof Map) {
                def args = i.clone()
                if (args.url instanceof Map) {
                    args.url = svc.buildLinkToOriginalResource(args.url)
                }
                def r = newResourceFromArgs(args, svc, resourceInfoList.size()==1)
                this.resources << r
            } else if (i instanceof String) {
                this.resources << newResourceFromArgs(url:i, svc, resourceInfoList.size()==1)
            } else {
                throw new IllegalArgumentException("I don't understand this resource: ${i}")
            }
        }
        lockDown()
    }
    
    void addModuleDependency(String name) {
        dependsOn << name
    }
    
    def getBundleTypes() {
        ['css', 'js']
    }
    
    ResourceMeta addNewSyntheticResource(Class<ResourceMeta> type, String uri, resSvc) {
        def agg = type.newInstance(module:this)
        agg.sourceUrl = uri // Hack
        agg.actualUrl = uri
        agg.workDir = resSvc.workDir
        
        resources << agg
        
        agg
    }
    
    ResourceMeta newResourceFromArgs(Map args, svc, boolean singleResourceModule) {
        def url = args.remove('url')
        if (url) {
            if (!url.contains('://') && !url.startsWith('/')) {
                url = '/'+url
            }
        }
        if (url == null) {
            throw new IllegalArgumentException("Cannot create resource with arguments ${args}, url is not set")
        }
        def r = new ResourceMeta(sourceUrl: url , workDir: svc.workDir, module:this)
        def ti = svc.getDefaultSettingsForURI(url, args.attrs?.type)
        if (ti == null) {
            throw new IllegalArgumentException("Cannot create resource $url, is not a supported type")
        }

        // Default the resource id for overrides to the url if no id supplied
        r.id = args.id ?: r.sourceUrl // minus the params etc

        r.disposition = args.remove('disposition') ?: ti.disposition
        r.linkOverride = args.remove('linkOverride')
        r.bundle = args.remove('bundle')
        def excludedMappers = args.remove('exclude')
        if (excludedMappers) {
            if (excludedMappers instanceof List) {
                r.excludedMappers = excludedMappers as Set
            } else if (!(excludedMappers instanceof Set)) {
                r.excludedMappers = excludedMappers.toString().split(',')*.trim() as Set
            }
        } 
        
        // We cannot auto bundle this if attrs, wrapper are set, or its a single resource module, or its not
        // a bundle-able type
        def canAutoBundle = 
            (!singleResourceModule || (singleResourceModule && defaultBundle)) && // single resource with defaultbundle specified is OK
            !r.bundle && 
            !args.wrapper && 
            !args.attrs && 
            (r.sourceUrlExtension in bundleTypes)
            
        if (canAutoBundle) {
            if (defaultBundle == null) {
                // use module name by default
                r.bundle = "bundle_$name"
            } else if (defaultBundle) { 
                // use supplied value as a default
                r.bundle = defaultBundle.toString()
            }
        }
        
        // Namespace bundle by disposition
        if (r.bundle) { 
            r.bundle += '_'+r.disposition
        }
        
        r.prePostWrapper = args.remove('wrapper')
        def resattrs = ti.attrs?.clone() ?: [:]
        def attrs = args.remove('attrs')
        if (attrs) {
            attrs.each { k, v ->
                resattrs[k] = v
            }
        }
        r.tagAttributes = resattrs
        r.attributes.putAll(args)
        return r        
    }
    
    void lockDown() {
        this.resources = this.resources.asImmutable()
    }
    
    Set<String> getRequiredDispositions() {
        if (!dispositions) {
            dispositions = (resources.findAll { r -> 
                r.disposition
            }).disposition as Set
        }
        return dispositions
    }
}