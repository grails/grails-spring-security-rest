package org.grails.plugin.resource

import grails.util.GrailsUtil
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.grails.plugin.resource.util.DispositionsUtils

/**
 * This taglib handles creation of all the links to resources, including the smart de-duping of them.
 *
 * This is also a general-purpose linking tag library for writing <head> links to resources. See resourceLink.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ResourceTagLib {
	
    static namespace = "r"
    
    static REQ_ATTR_PREFIX_PAGE_FRAGMENTS = 'resources.plugin.page.fragments'
    static REQ_ATTR_PREFIX_AUTO_DISPOSITION = 'resources.plugin.auto.disposition'
    
    static STASH_WRITERS = [
        'script': { out, stash ->
            out << "<script type=\"text/javascript\">"
            for (s in stash) {
                out << s
            }
            out << "</script>"

        },
        'style': { out, stash ->
            out << "<style type=\"text/css\">"
            for (s in stash) {
                out << s
            }
            out << "</style>"

        }
    ]
    
    static writeAttrs( attrs, output) {
        // Output any remaining user-specified attributes
        attrs.each { k, v ->
            if (v != null) {
               output << k
               output << '="'
               output << v.encodeAsHTML()
               output << '" '    
           }
        }
    }

	// Closures to write links of different types
    static LINK_WRITERS = [
        js: { url, constants, attrs ->
            def o = new StringBuilder()
            o << "<script src=\"${url}\" "

            // Output info from the mappings
            writeAttrs(constants, o)
            writeAttrs(attrs, o)

            o << '></script>'
            return o    
        },
        
        link: { url, constants, attrs ->
            def o = new StringBuilder()
            o << "<link href=\"${url}\" "

            // Output info from the mappings
            writeAttrs(constants, o)
            writeAttrs(attrs, o)

            o << '/>'
            return o
        }
    ]

    static SUPPORTED_TYPES = [
        css:[type:"text/css", rel:'stylesheet', media:'screen, projection'],
        js:[type:'text/javascript', writer:'js'],

        gif:[rel:'shortcut icon'],
        jpg:[rel:'shortcut icon'],
        png:[rel:'shortcut icon'],
        ico:[rel:'shortcut icon'],
        appleicon:[rel:'apple-touch-icon']
    ]
    
    def grailsResourceProcessor
    
    def grailsLinkGenerator
    
	/**
	 * Check if a url has already been rendered.
	 * 
	 * @param url
	 * @return true if not already rendered
	 */
    boolean notAlreadyIncludedResource(url) {
        url = url.toString()
        if (log.debugEnabled) {
            log.debug "Checking if this request has already pulled in [$url]"
        }
        def trk = request.resourceTracker
        if (trk == null) {
            trk = new HashSet()
            request.resourceTracker = trk
        }
        
        if (!trk.contains(url)) {
            trk.add(url)
            if (log.debugEnabled) {
                log.debug "This request has not already pulled in [$url]"
            }
            return true
        } else {
            if (log.debugEnabled) {
                log.debug "This request has already pulled in [$url], we'll be smart and skip it"
            }
            return false
        }
    }
    
    boolean declareModuleRequiredByPage(name, boolean mandatory = true) {
        def trk = request.resourceModuleTracker
        if (trk == null) {
            if (log.debugEnabled) {
                log.debug "creating new resource module tracker for request ${request}"
            }
            trk = new HashMap()
            request.resourceModuleTracker = trk
        }
        
        // If not already added, or is there but not mandatory and setting mandatory...
        if (!trk.containsKey(name) || (!trk[name] && mandatory)) {
            if (log.debugEnabled) {
                log.debug "adding module [$name] (mandatory:${mandatory}) to resource module tracker for request ${request}"
            }
            trk[name] = mandatory
            
            grailsResourceProcessor.addModuleDispositionsToRequest(request, name)
            return true
        } else {
            if (log.debugEnabled) {
                log.debug "skipping adding module [$name] to resource module tracker for request ${request} - already included"
            }
            return false
        }
    }
    
    /**
     * Render a link for a resource.
     * 
     * @attr uri to be written as the actual reference
     * @attr type of link to produce, must be one of SUPPORTED_TYPES
     * @attr ... other attributes which will override constant attributes for the link type
     */
    def doResourceLink = { attrs ->
        def uri = attrs.remove('uri')
        def type = attrs.remove('type')
        def urlForExtension = ResourceProcessor.removeQueryParams(uri)
        if (!type) {
            type = FilenameUtils.getExtension(urlForExtension)
        }
        
        def typeInfo = SUPPORTED_TYPES[type]?.clone()
        if (!typeInfo) {
            throwTagError "I can't work out the type of ${uri} with type [${type}]. Please check the URL, resource definition or specify [type] attribute"
        }
        
        def writerName = typeInfo.remove('writer')
        def writer = LINK_WRITERS[writerName ?: 'link']

        // Allow attrs to overwrite any constants
        attrs.each { typeInfo.remove(it.key) }

        out << writer(uri, typeInfo, attrs)
    }
    
    /**
     * Render an appropriate resource link for a resource - WHETHER IT IS PROCESSED BY THIS PLUGIN OR NOT.
     *
     * IMPORTANT: The point is that devs can use this for smart links in <head> whether or not they are using the resource
     * processing mechanisms. This gives utility to all, and allows us to have a single tag in Grails, meaning
     * that users need make no changes when they move to install processing plugins like zipped-resources.
     *
     * This accepts a "url" attribute which is a Map like that passed to g.resource,
     * or "uri" attribute which is an app-relative uri e.g. 'js/main.js
     * or "plugin"/"dir"/"file" attributes like g.resource
     *
     * This is *not* just for use with declared resources, you can use it for anything e.g. feeds.
     * The "type" attribute can override the type e.g. "rss" if the type cannot be extracted from the extension of
     * the url.
     */
    def resourceLink = { attrs ->
        GrailsUtil.deprecated "Tag [r:resourceLink] is deprecated please use [r:external] instead"
        out << external(attrs)
    }

    // Set the flag used by def filter to sanity check
    private void needsResourceLayout() {
        request.setAttribute('resources.need.layout', true)
    }

    def external = { attrs ->
        if (log.debugEnabled) {
            log.debug "external with $attrs"
        }

        if (!attrs.url && !attrs.uri && !attrs.file) {
            throw new GrailsTagException('For the &lt;r:external /&gt; tag, one of the attributes [uri, url, file] must be present')
        }
        
        def url = attrs.remove('url')
        def disposition = attrs.remove('disposition')

        def info 

        def type = attrs.remove('type')
        def resolveArgs = determineResourceResolutionArguments(url, attrs)

        // If a disposition specified, we may be ad-hoc so use that, else revert to default for type
        if (disposition == null) {
            // Get default disposition for this type
            disposition = 'head'
        }
        resolveArgs.disposition = disposition

        info = resolveLinkUriToUriAndResource(resolveArgs)

        // Copy in the tag attributes from the resource's declaration
        if (info.resource && info.resource.tagAttributes) {
            attrs.putAll(info.resource.tagAttributes)
        }

        // If we found a resource (i.e. not debug mode) and disposition is not what we're rendering, skip
        if (info.resource && (disposition != info.resource.disposition)) {
            // Just get out, we've called r.resource which has created the implicit resource and added it to implicit module
            // and layoutResources will render the implicit module
            return
        }
        
        // Output link if image disposition, or if not already included
        if (disposition == 'image' || notAlreadyIncludedResource(info.resource?.linkUrl ?: info.uri)) {
            attrs.type = type
            if (info.debug) {
                attrs.uri = info.resource?.linkUrl
            }
            if (!attrs.uri) {
                attrs.uri = info.uri
            }

            def wrapper = attrs.remove('wrapper') ?: info.resource?.prePostWrapper

            def output = doResourceLink(attrs).toString()

            if (wrapper) {
                out << wrapper(output)
            } else {
                out << output
            }
        }
    }

	/**
	 * Produce standard map of arguments to use in resolving resource.
	 * 
	 * @param url
	 * @param attrs
	 * @return 
	 */
    private determineResourceResolutionArguments(url, Map attrs) {

        def resolveArgs = [:]
        if (url == null) {
            if (attrs.uri) {
                // Might be app-relative resource URI
                resolveArgs.uri = attrs.remove('uri')
            } else {
                resolveArgs.plugin = attrs.remove('plugin')
                resolveArgs.dir = attrs.remove('dir')
                resolveArgs.file = attrs.remove('file')
            }
        } else if (url instanceof Map) {
            resolveArgs.putAll(url)
        }
        return resolveArgs
    }

    def use = { attrs ->
        GrailsUtil.deprecated "Tag [r:use] is deprecated please use [r:require] instead"
        out << r.require(attrs)
    }
    
    /**
     * Indicate that a page requires a named resource module
     * This is stored in the request until layoutResources is called, we then sort out what needs rendering or not later
     */
    def require = { attrs ->
        if (log.debugEnabled) {
            log.debug "require (request ${request}): ${attrs}"
        }
        
        needsResourceLayout()
        
        def trk = request.resourceModuleTracker
        def mandatory = attrs.strict == null ? true : attrs.strict.toString() != 'false'
        def moduleNames
        if (attrs.module) {
            moduleNames = [attrs.module]
        } else {
            if (attrs.modules instanceof List) {
                moduleNames = attrs.modules
            } else {
                moduleNames = attrs.modules.split(',')*.trim()
            }
        }

        if (log.debugEnabled) {
            log.debug "requiring modules: ${moduleNames} (mandatory: ${mandatory})"
        }

        moduleNames?.each { name ->
            if (log.debugEnabled) {
                log.debug "Checking if module [${name}] is already declared for this page..."
            }
            declareModuleRequiredByPage(name, mandatory)
        }
    }
    
    private stashPageFragment(String type, String disposition, def fragment) {
        if (log.debugEnabled) {
            log.debug "stashing request script for disposition [${disposition}]: ${ fragment}"
        }

        needsResourceLayout()
        
        def trkName = ResourceTagLib.makePageFragmentKey(type, disposition)
        DispositionsUtils.addDispositionToRequest(request, disposition, '-page-fragments-')

        def trk = request[trkName]
        if (!trk) {
            trk = []
            request[trkName] = trk
        }
        trk << fragment
    }
    
    private static String makePageFragmentKey(String type, String disposition) {
        "${REQ_ATTR_PREFIX_PAGE_FRAGMENTS}:${type}:${disposition}"
    }
    
    private List consumePageFragments(String type, String disposition) {
        return (List) request[ResourceTagLib.makePageFragmentKey(type, disposition)] ?: Collections.EMPTY_LIST
    }
    
    private static String makeAutoDispositionKey( String disposition) {
        "${REQ_ATTR_PREFIX_AUTO_DISPOSITION}:${disposition}"
    }

    private isAutoLayoutDone(disposition) {
        request[ResourceTagLib.makeAutoDispositionKey(disposition)]
    }
    
    private autoLayoutDone(disposition) {
        request[ResourceTagLib.makeAutoDispositionKey(disposition)] = true
    }
    
    /**
     * Render the resources. First invocation renders head JS and CSS, second renders deferred JS only, and any more spews.
     */
    def layoutResources = { attrs ->

        if (log.debugEnabled) {
            log.debug "laying out resources for request ${request}: ${attrs}"
        }

        def remainingDispositions = DispositionsUtils.getRequestDispositionsRemaining(request)
        def dispositionToRender = attrs.disposition
        if (!dispositionToRender) {
            if (!isAutoLayoutDone(DispositionsUtils.DISPOSITION_HEAD)) {
                dispositionToRender = DispositionsUtils.DISPOSITION_HEAD
                autoLayoutDone(DispositionsUtils.DISPOSITION_HEAD)
            } else if (!isAutoLayoutDone(DispositionsUtils.DISPOSITION_DEFER)) {
                dispositionToRender = DispositionsUtils.DISPOSITION_DEFER
                autoLayoutDone(DispositionsUtils.DISPOSITION_DEFER)
            } else {
                if (log.warnEnabled) {
                    log.warn "You seem to have too many r:layoutResources invocations with no disposition specified. It has already been called twice."
                }
                return
            }
        } else if (!remainingDispositions.contains(dispositionToRender)) {
            if (log.warnEnabled) {
                log.warn "A request was made to render resources for disposition [${dispositionToRender}] but there are no resources scheduled for that disposition, or it has already been rendered"
            }
            return
        }
        
        if (log.debugEnabled) {
            log.debug "Rendering resources for disposition [${dispositionToRender}]"
        }
        
        def trk = request.resourceModuleTracker
        if (log.debugEnabled) {
            log.debug "Rendering resources, modules in tracker: ${trk}"
        }
        def modulesNeeded = trk ? grailsResourceProcessor.getAllModuleNamesRequired(trk) : []
        if (log.debugEnabled) {
            log.debug "Rendering resources, modules needed: ${modulesNeeded}"
        }

        def modulesInOrder = grailsResourceProcessor.getModulesInDependencyOrder(modulesNeeded)
        if (log.debugEnabled) {
            log.debug "Rendering non-deferred resources, modules: ${modulesInOrder}..."
        }

        for (module in modulesInOrder) {
            // @todo where a module resource is bundled, need to satisfy deps of all resources in the bundle first!
            out << r.renderModule(name:module, disposition:dispositionToRender)
        }
        
        if (log.debugEnabled) {
            log.debug "Rendering page fragments for disposition: ${dispositionToRender}"
        }

        layoutPageStash(dispositionToRender)

        if (log.debugEnabled) {
            log.debug "Removing outstanding request disposition: ${dispositionToRender}"
        }
        
        DispositionsUtils.doneDispositionResources(request, dispositionToRender)
    }

    private layoutPageStash(final String disposition) {
        final Set<String> fragmentTypes = STASH_WRITERS.keySet()
        for (final String type in fragmentTypes) {
            final List stash = consumePageFragments(type, disposition)
            if (stash) {
                STASH_WRITERS[type](out, stash)
            }
        }
    }

    /**
     * For inline javascript that needs to be executed in the <head> section after all dependencies
     * @todo Later, we implement ESP hooks here and add scope="user" or scope="shared"
     */
    def script = { attrs, body ->
        final String disposition = attrs.remove('disposition') ?: 'defer'
        stashPageFragment('script', disposition, body())
    }

    def style = { attributes, body ->
        final String disposition = attributes.remove("disposition") ?: DispositionsUtils.DISPOSITION_HEAD
        stashPageFragment("style", disposition, body())
    }

    def stash = { attrs, body ->
        stashPageFragment(attrs.type, attrs.disposition, body())
    }
    
    protected getModuleByName(name) {
        def module = grailsResourceProcessor.getModule(name)
        if (!module) {
            if (name != ResourceProcessor.ADHOC_MODULE) {
                throw new IllegalArgumentException("No module found with name [$name]")
            }
        }
        return module
    }

    /**
     * Render the resources of the given module, and all its dependencies
     * Boolean attribute "deferred" determines whether or not the JS with "defer:true" gets rendered or not
     */
    def renderModule = { attrs ->
        if (log.debugEnabled) {
            log.debug "renderModule ${attrs}"
        }

        def name = attrs.name
        def module = attrs.module
        if (!module) {
            module = getModuleByName(name)
        }
        if (!module) {
            return
        }
        
        def s = new StringBuilder()
        
        def renderingDisposition = attrs.remove('disposition')

        if (log.debugEnabled) {
            log.debug "Rendering the resources of module [${name}]: ${module.dump()}"
        }
        
        def debugMode = grailsResourceProcessor.isDebugMode(request)
        
        for (r in module.resources) { 
            if (!r.exists() && !r.actualUrl?.contains('://')) {
                throw new IllegalArgumentException("Module [$name] depends on resource [${r.sourceUrl}] but the file cannot be found")
            }
            if (log.debugEnabled) {
                log.debug "Resource: ${r.sourceUrl} - disposition ${r.disposition} - rendering disposition ${renderingDisposition}"
            }
            if (r.disposition == renderingDisposition) {
                def args = [:]
                // args.uri needs to be the source uri used to identify the resource locally
                args.uri = debugMode ? r.originalUrl : "${r.actualUrl}"
                args.wrapper = r.prePostWrapper
                args.disposition = r.disposition
                
                if (r.tagAttributes) {
                    args.putAll(r.tagAttributes) // Copy the attrs originally provided e.g. type override
                }
                
                if (log.debugEnabled) {
                    log.debug "Rendering one of the module's resource links: ${args}"
                }
                s << external(args)
                s << '\n'
            }
        }
        out << s
    }

    /**
     * Get the uri to use for linking, and - if relevant - the resource instance.
     * 
     * NOTE: The URI handling mechanics in here are pretty evil and nuanced (i.e. 
     * ad-hoc vs declared, ad-hoc and not found, ad-hoc and excluded etc).
     * There is reasonable test coverage, but only fools rush in.
     * 
     * @attr uri - to be resolved, i.e. id of the ResourceMeta
     * @attr disposition - of the resource
     * 
     * @return Map with uri/url property and *maybe* a resource property
     */
    def resolveLinkUriToUriAndResource(attrs) {
        if (log.debugEnabled) {
            log.debug "resolveResourceAndURI: ${attrs}"
        }
        def ctxPath = request.contextPath

        def uri = attrs.remove('uri')
        def abs = uri?.indexOf('://') >= 0

        if (!uri) {
            // use the link generator to avoid stack overflow calling back into us
            // via g.resource
            attrs.contextPath = ctxPath
            uri = grailsLinkGenerator.resource(attrs)
            abs = uri.contains('://') 
        } else {
            if (!abs) {
                uri = ctxPath + uri
            }
        }
        
        def debugMode = grailsResourceProcessor.isDebugMode(request)

        // Get out quick and add param to tell filter we don't want any fancy stuff
        if (debugMode) {
            
            // Some JS libraries can't handle different query params being sent to other dependencies
            // so we reuse the same timestamp for the lifecycle of the request
    
            // Here we allow a refresh arg that will generate a new timestamp, normally we used the last we 
            // generated. Otherwise, you can't debug anything in a JS debugger as the URI of the JS 
            // is different every time.
            if (params._refreshResources && !request.'grails-resources.debug-timestamp-refreshed') {
                // Force re-generation of a new timestamp in debug mode
                session.removeAttribute('grails-resources.debug-timestamp')
                request.'grails-resources.debug-timestamp-refreshed' = true
            }
            
            def timestamp = session['grails-resources.debug-timestamp']
            if (!timestamp) {
                timestamp = System.currentTimeMillis()
                session['grails-resources.debug-timestamp'] = timestamp
            }

            uri += (uri.indexOf('?') >= 0) ? "&_debugResources=y&n=$timestamp" : "?_debugResources=y&n=$timestamp"
            return [uri:uri, debug:true]
        } 
        
        def disposition = attrs.remove('disposition')

        if (!abs) {
            uri = forcePrefixedWithSlash(uri)
        }
        
        // If its a bad or empty URI, get out of here. It must at least contain the context path if it is relative
        if (!abs && (uri.size() <= ctxPath.size())) {
            return [uri:uri]
        }

        def contextRelUri = abs ? uri : uri[ctxPath.size()..-1]
        def reluri = ResourceProcessor.removeQueryParams(contextRelUri)
        
        // Get ResourceMeta or create one if uri is not absolute
        def res = grailsResourceProcessor.getExistingResourceMeta(reluri)
        if (!res && !abs) {
            res = grailsResourceProcessor.getResourceMetaForURI(reluri, true, null, { r ->
                // If this is an ad hoc resource, we need to store if it can be deferred or not
                if (disposition != null) {
                    r.disposition = disposition
                }
            })
        }

        // If the link has to support linkUrl for override, or fall back to the full requested url
        // we resolve without query params, but must keep them for linking        
        def linkUrl = res ? res.linkUrl : contextRelUri

        if (linkUrl.contains('://')) {
            // @todo do we need to toggle http/https here based on current request protocol?
            return [uri:linkUrl, resource:res]
        } else {
            // Only apply static prefix if the resource actually has ResourceMeta created for it
            uri = res ? ctxPath+grailsResourceProcessor.staticUrlPrefix+linkUrl : ctxPath+linkUrl
            return [uri:uri, resource:res]
        }
    }
     
    /**
     * Get the URL for a resource
     * @todo this currently won't work for absolute="true" invocations, it should just passthrough these
     */
    def resource = { attrs ->
        def info = resolveLinkUriToUriAndResource(attrs)
        if (info.resource) {
            // We know we located the resource
            out << info.uri
        } else {
            // We don't know about this, back out and use grails URI but warn
            if (!info.debug && log.warnEnabled) {
                log.warn "Invocation of <r:resource> for a resource that apparently doesn't exist: ${info.uri}"
            }
            out << info.uri
        }
    }
    
    /**
     * Write out an HTML <img> tag using resource processing for the image
     */
    def img = { attrs ->
        if (!attrs.uri && !attrs.dir) {
            attrs.dir = "images"
        }
        def args = attrs.clone()
        args.disposition = "image"
        
        def info = resolveLinkUriToUriAndResource(args)
        def res = info.resource

        attrs.remove('uri')
        def o = new StringBuilder()
        o << "<img src=\"${info.uri.encodeAsHTML()}\" "
        def attribs = res?.tagAttributes ? res.tagAttributes.clone() : [:]
		def excludes = ['dir', 'uri', 'file', 'plugin']
        attribs += attrs.findAll { !(it.key in excludes) }
        attrs = attribs

        writeAttrs(attrs, o)
        o << "/>"
        out << o
    }
    
    protected forcePrefixedWithSlash(uri) {
        if (uri) {
            if (uri[0] != '/') {
                uri = '/' + uri
            }
        }
        return uri
    }
}
