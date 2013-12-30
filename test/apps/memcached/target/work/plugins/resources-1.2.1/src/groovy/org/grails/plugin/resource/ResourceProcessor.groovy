package org.grails.plugin.resource

import grails.util.Environment
import groovy.util.logging.Commons
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.grails.plugin.resource.mapper.ResourceMapper
import org.grails.plugin.resource.mapper.ResourceMappersFactory
import org.grails.plugin.resource.module.ModuleDeclarationsFactory
import org.grails.plugin.resource.module.ModulesBuilder
import org.grails.plugin.resource.util.DispositionsUtils
import org.grails.plugin.resource.util.ResourceMetaStore
import org.grails.plugin.resource.util.StatsManager
import org.springframework.beans.factory.InitializingBean
import org.springframework.util.AntPathMatcher
import org.springframework.web.util.WebUtils

import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * Primary service facade to actions on resources.
 *
 * This class loads resource declarations (see reload()) and receives requests from the servlet filter to
 * serve resources. It will process the resource if necessary the first time, and then return the file.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
@Commons
class ResourceProcessor implements InitializingBean {

    static transactional = false

    boolean reloading

    static final PATH_MATCHER = new AntPathMatcher()
    static ADHOC_MODULE = "__@adhoc-files@__"        // The placeholder for all undeclared resources that are linked to
    static SYNTHETIC_MODULE = "__@synthetic-files@__"   // The placeholder for all the generated (i.e. aggregate) resources

    static DEFAULT_ADHOC_INCLUDES = [
            '**/*.*'
    ]

    static DEFAULT_ADHOC_EXCLUDES = [
    ]

    static DEFAULT_MODULE_SETTINGS = [
            css: [disposition: 'head'],
            rss: [disposition: 'head'],
            gif: [disposition: 'head'],
            jpg: [disposition: 'head'],
            png: [disposition: 'head'],
            ico: [disposition: 'head'],
            js: [disposition: 'defer']
    ]

    def grailsLinkGenerator
    def grailsResourceLocator // A Grails 2-only bean
    def grailsApplication

    StatsManager statsManager = new StatsManager()

    def staticUrlPrefix

    private File workDir

    def modulesByName = new ConcurrentHashMap()

    def resourceInfo = new ResourceMetaStore()
    def allResourcesByOriginalSourceURI = new ConcurrentHashMap()

    def modulesInDependencyOrder = []

    def resourceMappers

    @Lazy servletContext = { grailsApplication.mainContext.servletContext }()

    boolean processingEnabled

    List adHocIncludes
    List adHocExcludes

    List optionalDispositions

    /**
     * Initialize bean after properties have been set.
     */
    void afterPropertiesSet() {
        processingEnabled = getConfigParamOrDefault('processing.enabled', true)
        adHocIncludes = getConfigParamOrDefault('adhoc.includes', DEFAULT_ADHOC_INCLUDES)
        adHocIncludes = adHocIncludes.collect { it.startsWith('/') ? it : '/' + it }

        adHocExcludes = getConfigParamOrDefault('adhoc.excludes', DEFAULT_ADHOC_EXCLUDES)
        adHocExcludes = adHocExcludes.collect { it.startsWith('/') ? it : '/' + it }

        optionalDispositions = getConfigParamOrDefault('optional.dispositions', ['inline', 'image'])

    }

    /**
     * Returns the config object under 'grails.resources'
     */
    ConfigObject getConfig() {
        grailsApplication.config.grails.resources
    }

    /**
     * Used to retrieve a resources config param, or return the supplied
     * default value if no explicit value was set in config
     */
    def getConfigParamOrDefault(String key, defaultValue) {
        // move along the key's path from ConfigObject to ConfigObject
        // until the end was reached, which is the value for the provided key
        // (similar to to access the config with a flat key without the need to flatten the config)
        def param = key.tokenize('.').inject(config) { conf, v -> conf[v] }

        if (param instanceof ConfigObject) {
            param.size() == 0 ? defaultValue : param
        } else {
            param
        }
    }

    boolean isInternalModule(def moduleOrName) {
        def n = moduleOrName instanceof ResourceModule ? moduleOrName.name : moduleOrName
        return n in [ADHOC_MODULE, SYNTHETIC_MODULE]
    }

    /**
     * Topographic Sort ordering for a Directed Acyclic Graph of dependencies.
     * Less scary than it seemed, but still a bit hard to read. Think about vectors and edges.
     * Visit wikipedia.
     */
    void updateDependencyOrder() {
        def modules = (modulesByName.collect { it.value }).findAll { !isInternalModule(it) }
        def noIncomingEdges = modules.findAll { l -> !modules.any { l.name in it.dependsOn } }
        def ordered = []
        Set visited = new HashSet()

        def visit
        visit = { n ->
            if (!(n.name in visited)) {
                visited << n.name
                def incomingEdges = []
                n.dependsOn?.each { d ->
                    def m = modules.find { mod -> mod.name == d }
                    if (!m) {
                        log.warn "There is a dependency on module [${d}] by module [${n.name}] but no such module has been defined"
                    } else {
                        incomingEdges << m
                    }
                }
                for (m in incomingEdges) {
                    visit(m)
                }
                ordered << n.name
            }
        }

        for (module in noIncomingEdges) {
            visit(module)
        }

        ordered << ADHOC_MODULE
        ordered << SYNTHETIC_MODULE

        modulesInDependencyOrder = ordered
    }

    File getWorkDir() {
        // @todo this isn't threadsafe at startup if its lazy. We should change it.
        if (!this.@workDir) {
            def d = getConfigParamOrDefault('work.dir', null)
            this.@workDir = d ? new File(d) : new File(WebUtils.getTempDir(servletContext), "grails-resources")
        }
        assert this.@workDir
        return this.@workDir
    }

    def getPluginManager() {
        // The plugin manager bean configured in integration testing is not the real thing and causes errors.
        // Using the pluginManager from the holder means that we always get a legit instance.
        // http://jira.codehaus.org/browse/GRAILSPLUGINS-2712
        PluginManagerHolder.pluginManager
    }

    def extractURI(request, adhoc) {
        def uriStart = (adhoc ? request.contextPath : request.contextPath + staticUrlPrefix).size()
        return uriStart < request.requestURI.size() ? request.requestURI[uriStart..-1] : ''
    }

    boolean canProcessLegacyResource(uri) {
        // Apply our own url filtering rules because servlet mapping uris are too lame
        boolean included = adHocIncludes.find { p ->
            PATH_MATCHER.match(p, uri)
        }
        log.debug "Legacy resource ${uri} matched includes? ${included}"

        if (included) {
            included = !(adHocExcludes.find { PATH_MATCHER.match(it, uri) })
            log.debug "Legacy resource ${uri} passed excludes? ${included}"
        }

        return included
    }

    /**
     * Process a legacy URI that points to a normal resource, not produced with our
     * own tags, and likely not referencing a declared resource.
     * Therefore the URI may not be build-unique and cannot reliably be cached so
     * we have to redirect "Moved Temporarily" to it in case another plugin causes eternal caching etc.
     *
     * To do this, we simply search the cache based on sourceUrl instead of actualUrl
     *
     * This is not recommended, its just a quick out of the box fix for legacy (or pre-"resources plugin" plugin) code.
     *
     * So a request for <ctx>/css/main.css comes in. This needs to redirect to e.g. <ctx>/static/css/342342353345343534.css
     * This involves looking it up by source uri. Therefore the same resource may have multiple mappings in the 
     * resourceInfo map but they should not be conflicting.
     */
    boolean processLegacyResource(request, response) {
        log.debug "Handling Legacy resource ${request.requestURI}"
        def uri = removeQueryParams(extractURI(request, true))

        // Only handle it if it should be included in processing
        if (canProcessLegacyResource(uri)) {
            log.debug "Attempting to render legacy resource ${request.requestURI}"

            // @todo query params are lost at this point for ad hoc resources, this needs fixing?
            ResourceMeta res
            try {
                res = getResourceMetaForURI(uri, true)

            } catch (FileNotFoundException fnfe) {
                response.sendError(404, fnfe.message)
                return
            }

            if (Environment.current == Environment.DEVELOPMENT) {
                if (res) {
                    response.setHeader('X-Grails-Resources-Original-Src', res?.sourceUrl)
                }
            }
            if (res?.exists()) {
                redirectToActualUrl(res, request, response)

            } else {
                response.sendError(404)
            }
            return true

        } else {
            return false // we didn't handle this
        }
    }

    /**
     * Redirect the client to the actual processed Url, used for when an ad-hoc resource is accessed
     */
    void redirectToActualUrl(ResourceMeta res, HttpServletRequest request, HttpServletResponse response) {
        // Now redirect the client to the processed url
        String url
        if (res.linkUrl.contains("://")) {
            url = res.linkUrl

        } else {
            url = request.contextPath + staticUrlPrefix + res.linkUrl
        }

        log.debug "Redirecting ad-hoc resource ${request.requestURI} " +
                "to $url which makes it UNCACHEABLE - declare this resource " +
                "and use resourceLink/module tags to avoid redirects and enable client-side caching"

        response.sendRedirect url
    }

    /**
     * Process a URI where the input URI matches a cached and declared resource URI,
     * without any redirects. This is the real deal
     */
    void processModernResource(request, response) {
        log.debug "Handling resource ${request.requestURI}"

        // Find the ResourceMeta for the request, or create it
        def uri = removeQueryParams(extractURI(request, false))
        def inf
        try {
            // Allow ad hoc creation of these resources too, incase they are requested directly 
            // across multiple nodes, even if this node has not yet created a link to that resource
            inf = getResourceMetaForURI(uri, true)
        } catch (FileNotFoundException fnfe) {
            response.sendError(404, fnfe.message)
            return
        }

        if (inf) {
            if (Environment.current == Environment.DEVELOPMENT) {
                if (inf) {
                    response.setHeader('X-Grails-Resources-Original-Src', inf.sourceUrl)
                }
            }

            // See if its an ad-hoc resource that has come here via a relative link
            // @todo make this development mode only by default?
            if (inf.actualUrl != uri) {
                redirectToActualUrl(inf, request, response)
                return
            }
        }

        // If we have a file, go for it
        if (inf?.exists()) {
            log.debug "Returning processed resource ${request.requestURI}"

            def data = inf.newInputStream()
            try {
                // Now set up the response
                response.contentType = inf.contentType
                response.setContentLength(inf.contentLength)
                response.setDateHeader('Last-Modified', inf.originalLastMod)

                // Here we need to let the mapper add headers etc
                if (inf.requestProcessors) {
                    log.debug "Running request processors on ${request.requestURI}"

                    for (processor in inf.requestProcessors) {
                        log.debug "Applying request processor on ${request.requestURI}: " + processor.class.name

                        def p = processor.clone()
                        p.delegate = inf
                        p(request, response)
                    }
                }

                // @todo Could we do something faster here? Feels wrong, buffer size is tiny in Groovy
                response.outputStream << data
            } finally {
                data?.close()
            }
        } else {
            response.sendError(404)
        }
    }

    /**
     * See if we have a ResourceMeta for this URI.
     * @return null if not processed/created yet, the instance if it exists
     */
    ResourceMeta findResourceForURI(String uri) {
        resourceInfo[uri]
    }

    ResourceMeta findSyntheticResourceById(bundleId) {
        def mod = modulesByName[SYNTHETIC_MODULE]
        if (mod) {
            return mod.resources.find { r -> r.id == bundleId }
        } else {
            return null
        }
    }

    ResourceMeta newSyntheticResource(String uri, Class<ResourceMeta> type) {
        log.debug "Creating synthetic resource of type ${type} for URI [${uri}]"

        def synthModule = getOrCreateSyntheticOrImplicitModule(true)
        def agg = synthModule.addNewSyntheticResource(type, uri, this)

        log.debug "synthetic module resources: ${synthModule.resources}"

        return agg
    }

    ResourceModule getOrCreateSyntheticOrImplicitModule(boolean synthetic) {
        def mod
        def moduleName = synthetic ? SYNTHETIC_MODULE : ADHOC_MODULE
        // We often get multiple simultaneous requests at startup and this causes
        // multiple creates and loss of concurrently processed resources
        synchronized (moduleName) {
            mod = getModule(moduleName)
            if (!mod) {
                log.debug "Creating module: $moduleName"

                defineModule(moduleName)
                mod = getModule(moduleName)
            }
        }
        return mod
    }

    ResourceMeta getExistingResourceMeta(uri) {
        resourceInfo[uri]
    }

    /**
     * Get the existing or create a new ad-hoc ResourceMeta for the URI.
     * @returns The resource instance - which may have a null processedFile if the resource cannot be found
     */
    ResourceMeta getResourceMetaForURI(uri, Boolean createAdHocResourceIfNeeded = true, String declaringResource = null,
                                       Closure postProcessor = null) {

        // Declared resources will already exist, but ad-hoc or synthetic may need to be created
        def res = resourceInfo.getOrCreateAdHocResource(uri) {->
            if (!createAdHocResourceIfNeeded) {
                log.warn("We can't create resources on the fly unless they are 'ad-hoc', we're going to 404. Resource URI: $uri")
                return null
            }

            if (!canProcessLegacyResource(uri)) {
                log.debug("Skipping ad-hoc resource $uri as it is excluded")
                return null
            }

            // If we don't already have it, its either not been declared in the DSL or its Synthetic and its
            // not already been retrieved
            ResourceModule mod = getOrCreateSyntheticOrImplicitModule(false)

            // Need to create ad-hoc resource, its not synthetic
            log.debug "Creating new implicit resource for ${uri}"
            ResourceMeta r = new ResourceMeta(sourceUrl: uri, workDir: getWorkDir(), module: mod)
            r.declaringResource = declaringResource

            r = prepareResource(r, true)

            // Only if the URI mapped to a real file, do we add the resource
            // Prevents DoS with zillions of 404s
            if (r.exists()) {
                if (postProcessor) {
                    postProcessor(r)
                }
                synchronized (mod.resources) {
                    // Prevent concurrent requests resulting in multiple additions of same resource
                    // This relates specifically to the ad-hoc resources module
                    if (!mod.resources.find({ x -> x.sourceUrl == r.sourceUrl })) {
                        mod.resources << r
                    }
                }
            }

            allResourcesByOriginalSourceURI[r.sourceUrl] = r
            return r
        } // end of closure

        return res
    }

    /**
     * Workaround for replaceAll problems with \ in Java
     */
    String makeFileSystemPathFromURI(uri) {
        def chars = uri.chars
        chars.eachWithIndex { c, i ->
            if (c == '/') {
                chars[i] = File.separatorChar
            }
        }
        new String(chars)
    }

    File makeFileForURI(String uri) {
        def splitPoint = uri.lastIndexOf('/')
        def fileSystemDir = splitPoint > 0 ? makeFileSystemPathFromURI(uri[0..splitPoint - 1]) : ''
        def fileSystemFile = makeFileSystemPathFromURI(uri[splitPoint + 1..-1])
        def staticDir = new File(getWorkDir(), fileSystemDir)

        // force the structure
        if (!staticDir.exists()) {
            // Do not assert this, we are re-entrant and may get multiple simultaneous calls.
            // We just want to be sure one of them works
            staticDir.mkdirs()
            if (!staticDir.exists()) {
                log.error "Unable to create static resource cache directory: ${staticDir}"
            }
        }

        log.debug "Creating file object for URI [$uri] from [${staticDir}] and [${fileSystemFile}]"
        File f = new File(staticDir, fileSystemFile)
        // Delete the existing file - it may be from previous release, we cannot tell.
        if (f.exists()) {
            assert f.delete()
        }
        return f
    }

    /**
     * Take g.resource style args and create a link to that original resource in the app, relative to the app context path
     */
    String buildLinkToOriginalResource(Map args) {
        args.contextPath = '' // make it relative, otherwise we get absolute links from Grails 1.4
        grailsLinkGenerator.resource(args)
    }

    /**
     * Returns the actual URL for loading the resource specified by the uri
     * By default this is a file in the app's WAR, but this could support other schemes
     *
     */
    URL getOriginalResourceURLForURI(uri) {
        if (grailsResourceLocator != null) {
            def res = grailsResourceLocator.findResourceForURI(uri)
            if (res != null) {
                return res.URL
            }
        } else {
            servletContext.getResource(uri)
        }
    }

    /**
     * Resolve mime type for a URI by file extension
     */
    String getMimeType(uri) {
        servletContext.getMimeType(uri)
    }

    /**
     * Execute the processing chain for the resource, returning list of URIs to add to uri -> resource mappings
     * for this resource
     */
    ResourceMeta prepareResource(ResourceMeta r, boolean adHocResource) {
        log.debug "Preparing resource ${r.sourceUrl} (${r.dump()})"

        if (r.delegating) {
            log.debug "Skipping prepare resource for [${r.sourceUrl}] as it is delegated"
            return
        }

        if (!adHocResource && findResourceForURI(r.sourceUrl)) {
            def existing = allResourcesByOriginalSourceURI[r.sourceUrl]
            def modName = existing.module.name
            throw new IllegalArgumentException(
                    "Skipping prepare resource for [${r.sourceUrl}] - This resource is declared in module [${r.module.name}] as well as module [${modName}]"
            )
        }

        r.beginPrepare(this)

        if (processingEnabled) {
            if (!r.originalAbsolute) {
                applyMappers(r)
            }
        }

        r.endPrepare(this)

        return r
    }

    void applyMappers(ResourceMeta r) {

        // Now iterate over the mappers...
        log.debug "Applying mappers to ${r.processedFile}"

        // Apply all mappers / or only those until the resource becomes delegated
        // Once delegated, its the delegate that needs to be processed, not the original
        def phase
        for (m in resourceMappers) {
            if (isMapperEnabledInGlobalConfig(m)) {
                if (r.delegating) {
                    break;
                }

                log.debug "Running mapper ${m.name} (${m.artefact})"

                if (m.phase != phase) {
                    phase = m.phase
                    log.debug "Entering mapper phase ${phase}"
                }

                log.debug "Applying mapper ${m.name} to ${r.processedFile} - delegating? ${r.delegating}"
                long startTime = System.currentTimeMillis()

                boolean appliedMapper = m.invokeIfNotExcluded(r)
                log.debug "Applied mapper ${m.name} to ${r.processedFile}"

                long endTime = System.currentTimeMillis()
                statsManager.storeAggregateStat('mappers-time', m.name, endTime - startTime)

                r.wasProcessedByMapper(m, appliedMapper)
            }
        }
    }

    /**
     * Test if mapper is enabled in global configuration.
     *
     * @param mapper to check
     * @return true if enabled, false otherwise
     */
    private boolean isMapperEnabledInGlobalConfig(ResourceMapper m) {
        getConfigParamOrDefault('mappers.' + m.name + '.enabled', true)
    }

    void prepareSingleDeclaredResource(ResourceMeta r, Closure postPrepare = null) {
        resourceInfo.addDeclaredResource {->
            try {
                prepareResource(r, false)
            } catch (FileNotFoundException fnfe) {
                log.warn fnfe.message
            }
        }
        if (postPrepare) {
            postPrepare()
        }
    }

    void prepareResourceBatch(ResourceProcessorBatch batch) {
        if (log.debugEnabled) {
            log.debug "Preparing resource batch:"
            batch.each { r ->
                log.debug "Batch includes resource: ${r.sourceUrl}"
            }
        }

        def affectedSynthetics = []
        batch.each { r ->
            r.reset()
            resourceInfo.evict(r.sourceUrl)

            prepareSingleDeclaredResource(r) {
                def u = r.sourceUrl
                allResourcesByOriginalSourceURI[u] = r
            }

            if (r.delegating) {
                if (!(affectedSynthetics.find { it == r.delegate })) {
                    affectedSynthetics << r.delegate
                }
            }
        }

        // Synthetic resources are only known after processing declared resources
        log.debug "Preparing synthetic resources"
        for (r in affectedSynthetics) {
            log.debug "Preparing synthetic resource: ${r.sourceUrl}"

            r.reset()
            resourceInfo.evict(r.sourceUrl)

            prepareSingleDeclaredResource(r) {
                def u = r.sourceUrl
                allResourcesByOriginalSourceURI[u] = r
            }
        }
    }

    void storeModule(ResourceModule m, ResourceProcessorBatch batch) {
        log.debug "Storing resource module definition ${m.dump()}"

        if (batch) {
            for (r in m.resources) {
                batch.add(r)
            }
        }

        modulesByName[m.name] = m
    }

    def defineModule(String name) {
        storeModule(new ResourceModule(name, this), null)
    }

    def defineModuleFromBuilder(builderInfo, ResourceProcessorBatch resBatch) {
        def m = new ResourceModule(builderInfo.name, builderInfo.resources, builderInfo.defaultBundle, this)
        storeModule(m, resBatch)
        builderInfo.dependencies?.each { d ->
            m.addModuleDependency(d)
        }
    }

    /**
     * Retrieve a module by name.
     *
     * @param name
     * @return ResourceModule or null if none by that name
     */
    def getModule(name) {
        modulesByName[name]
    }

    void forgetModules() {
        log.info "Forgetting all known modules..."
        modulesByName.clear()
        modulesInDependencyOrder.clear()

        // If we forget modules we have to forget resources too
        forgetResources()
    }

    void forgetResources() {
        log.info "Forgetting all known resources..."

        // These are bi-products of resource processing so need to go
        modulesByName.remove(SYNTHETIC_MODULE)
        modulesByName.remove(ADHOC_MODULE)

        // This is cached data
        allResourcesByOriginalSourceURI.clear()
        resourceInfo = new ResourceMetaStore()
    }

    private loadModules(ResourceProcessorBatch batch) {
        log.info "Loading resource declarations..."
        forgetModules()

        def declarations = ModuleDeclarationsFactory.getModuleDeclarations(grailsApplication)

        def modules = []
        def builder = new ModulesBuilder(modules)

        declarations.each { sourceClassName, dsl ->
            log.debug("evaluating resource modules from $sourceClassName")

            dsl.delegate = builder
            dsl.resolveStrategy = Closure.DELEGATE_FIRST
            dsl.owner.class.metaClass.static.main = { builder.main(it) } // GPRESOURCES-50: hack to allow module name 'main'
            dsl()
        }

        // Always do app modules after
        Closure appModules = ModuleDeclarationsFactory.getApplicationConfigDeclarations(grailsApplication)
        if (appModules) {
            log.debug("evaluating resource modules from application Config")

            appModules.delegate = builder
            appModules.resolveStrategy = Closure.DELEGATE_FIRST
            appModules()
        }

        log.debug("resource modules after evaluation: $modules")

        // Now merge in any overrides
        log.debug "Merging in module overrides ${builder._moduleOverrides}"
        builder._moduleOverrides.each { overriddenModule ->
            log.debug "Merging in module overrides for ${overriddenModule}"

            def existingModule = modules.find { it.name == overriddenModule.name }
            if (existingModule) {
                if (overriddenModule.defaultBundle) {
                    log.debug "Overriding module [${existingModule.name}] defaultBundle with [${overriddenModule.defaultBundle}]"
                    existingModule.defaultBundle = overriddenModule.defaultBundle
                }
                if (overriddenModule.dependencies) {
                    log.debug "Overriding module [${existingModule.name}] dependencies with [${overriddenModule.dependencies}]"
                    // Replace, not merge
                    existingModule.dependencies = overriddenModule.dependencies
                }
                overriddenModule.resources.each { res ->
                    def existingResources = existingModule.resources.findAll {
                        it.id ? (it.id == res.id) : (it.url == res.id)
                    }
                    if (existingResources) {
                        log.debug "Overriding ${overriddenModule.name} resources with id ${res.id} with " +
                                "new settings: ${res}"
                        // Merge, not replace - for each matching resource
                        existingResources.each { r ->
                            r.putAll(res)
                        }
                    }
                }
            } else {
                log.warn "Attempt to override resource module ${overriddenModule.name} but " +
                        "there is nothing to override, this module does not exist"
            }
        }

        modules.each { m -> defineModuleFromBuilder(m, batch) }

        updateDependencyOrder()

        resourcesChanged(batch)
    }

    private resourcesChanged(ResourceProcessorBatch batch) {
        prepareResourceBatch(batch)

        resolveSyntheticResourceDependencies()
    }

    private collectResourcesThatNeedProcessing(module, batch) {
        // Reset them all in case this is a reload
        for (r in module?.resources) {
            log.debug "Has resource [${r.sourceUrl}] changed? ${r.dirty}"

            if (r.dirty) {
                batch.add(r)
            }
        }
    }

    private loadResources(ResourceProcessorBatch resBatch) {
        log.info "Loading declared resources..."

        // Check for any declared or ad hoc resources that need processing
        for (m in modulesInDependencyOrder) {
            if (m != SYNTHETIC_MODULE) {
                def module = modulesByName[m]
                collectResourcesThatNeedProcessing(module, resBatch)
            }
        }
    /* @todo add this later when we understand what less-css-resources needs
        // Now do the derived synthetic resources as we know any changed components
        // have now been reset
        
        // LESS mapper would make synthetic resources too, and these might also delegate
        // to a bundled resource, but all need processing in the correct order
        // and LESS would need to compile the stuff to a file in beginPrepare
        // before the bundle aggregates the output. 
        // Question is, what is the correct ordering here?
        collectResourcesThatNeedProcessing(modulesByName[SYNTHETIC_MODULE], resBatch)
    */
        resourcesChanged(resBatch)
    }

    /**
     *
     */
    void resolveSyntheticResourceDependencies() {
        // @todo:
        // 1. Go through all SYNTHETIC resources 
        // 2. Add all resources before it in the current module as deps to the resources
        // 3. Iterate over module deps of resource's owning module, in module dep order
        // 4. Add all their resources as deps, before existing deps
        // 5. Repeat 2-4 for all resources on all declared modules, in module dep order (bottom up)
    }

    static removeQueryParams(uri) {
        def qidx = uri.indexOf('?')
        qidx > 0 ? uri[0..qidx - 1] : uri
    }

    def getDefaultSettingsForURI(uri, typeOverride = null) {

        if (!typeOverride) {
            // Strip off query args
            def extUrl = removeQueryParams(uri)

            String ext = FilenameUtils.getExtension(extUrl)
            log.debug "Extension extracted from ${uri} ([$extUrl]) is ${ext}"
            typeOverride = ext
        }

        DEFAULT_MODULE_SETTINGS[typeOverride]
    }


    def dumpResources(toLog = true) {
        StringBuilder s1 = new StringBuilder()
        modulesByName.keySet().sort().each { moduleName ->
            def mod = modulesByName[moduleName]
            s1 << "Module: ${moduleName}\n"
            s1 << "   Depends on modules: ${mod.dependsOn}\n"
            def res = [] + mod.resources
            res.sort({ a, b -> a.actualUrl <=> b.actualUrl }).each { resource ->
                if (resource instanceof AggregatedResourceMeta) {
                    s1 << "   Synthetic Resource: ${resource.sourceUrl}\n"
                } else {
                    s1 << "   Resource: ${resource.sourceUrl}\n"
                }
                s1 << "             -- id: ${resource.id}\n"
                s1 << "             -- original Url: ${resource.originalUrl}\n"
                s1 << "             -- local file: ${resource.processedFile}\n"
                s1 << "             -- exists: ${resource.exists()}\n"
                s1 << "             -- mime type: ${resource.contentType}\n"
                s1 << "             -- actual Url: ${resource.actualUrl}\n"
                s1 << "             -- source Extension: ${resource.sourceUrlExtension}\n"
                s1 << "             -- query params/fragment: ${resource.sourceUrlParamsAndFragment}\n"
                s1 << "             -- url for linking: ${resource.linkUrl}\n"
                s1 << "             -- content length: ${resource.contentLength} (original ${resource.originalContentLength})\n"
                s1 << "             -- link override: ${resource.linkOverride}\n"
                s1 << "             -- excluded mappers: ${resource.excludedMappers?.join(', ')}\n"
                s1 << "             -- attributes: ${resource.attributes}\n"
                s1 << "             -- tag attributes: ${resource.tagAttributes}\n"
                s1 << "             -- disposition: ${resource.disposition}\n"
                s1 << "             -- delegating?: ${resource.delegate ? 'Yes: ' + resource.delegate.actualUrl : 'No'}\n"
            }
        }
        StringBuilder s2 = new StringBuilder()
        resourceInfo.keySet().sort().each { uri ->
            def res = resourceInfo[uri]
            s2 << "Resource URI: ${uri} => ${res.processedFile}\n"
        }
        updateDependencyOrder()
        String s4 = "Dependency load order: ${modulesInDependencyOrder}\n"

        String s5 = "Mapper application order: ${resourceMappers*.name}\n"

        if (toLog) {
            log.debug '-' * 50
            log.debug "Resource definitions"
            log.debug(s1)
            log.debug '-' * 50
            log.debug "Resource URI cache"
            log.debug '-' * 50
            log.debug(s2)
            log.debug '-' * 50
            log.debug "Module load order"
            log.debug '-' * 50
            log.debug(s4)
            log.debug '-' * 50
            log.debug(s5)
            log.debug '-' * 50
        }
        return s1.toString() + s2.toString() + s4.toString() + s5.toString()
    }

    boolean isDebugMode(ServletRequest request) {
        if (getConfigParamOrDefault('debug', false)) {
            config.debug
        } else if (request != null) {
            isExplicitDebugRequest(request)
        } else {
            false
        }
    }

    private boolean isExplicitDebugRequest(HttpServletRequest request) {
        if (Environment.current == Environment.DEVELOPMENT) {
            Boolean requestContainsDebug = request.getParameter('_debugResources') != null
            Boolean wasReferredFromDebugRequest = request.getHeader('Referer')?.contains('?_debugResources=')

            return requestContainsDebug || wasReferredFromDebugRequest

        } else {
            false
        }
    }

    private void loadMappers() {
        resourceMappers = ResourceMappersFactory.createResourceMappers(grailsApplication, config.mappers)
    }

    /**
     * Reload just the mappers and just the resources - keep the existing module definitiosn
     */
    synchronized reloadMappers() {
        reloading = true
        try {
            log.info("Performing a resource mapper reload")

            statsManager.resetStats()


            loadMappers()

            ResourceProcessorBatch reloadBatch = new ResourceProcessorBatch()

            loadResources(reloadBatch)

            statsManager.dumpStats()
            log.info("Finished resource mapper reload")
        } finally {
            reloading = false
        }
    }


    synchronized reloadModules() {
        reloading = true
        try {
            log.info("Performing a module definition reload")
            statsManager.resetStats()

            ResourceProcessorBatch reloadBatch = new ResourceProcessorBatch()

            loadModules(reloadBatch)

            statsManager.dumpStats()
            log.info("Finished module definition reload")
        } finally {
            reloading = false
        }
    }

    synchronized reloadChangedFiles() {
        reloading = true
        try {
            log.info("Performing a changed file reload")

            statsManager.resetStats()

            ResourceProcessorBatch reloadBatch = new ResourceProcessorBatch()

            loadResources(reloadBatch)

            statsManager.dumpStats()
            log.info("Finished changed file reload")
        } finally {
            reloading = false
        }
    }

    void reloadAll() {
        reloading = true
        try {
            log.info("Performing a full reload")

            statsManager.resetStats()

            loadMappers()

            ResourceProcessorBatch reloadBatch = new ResourceProcessorBatch()

            loadModules(reloadBatch)

            statsManager.dumpStats()
            log.info("Finished full reload")
        } catch (Throwable t) {
            log.error "Unable to load resources", t
        } finally {
            reloading = false
        }
    }

    /**
     * Return a list of all the names of all modules required (included the input modules) to 
     * satisfy the dependencies of the input list of module names.
     */
    def getAllModuleNamesRequired(moduleNameList) {
        def result = []

        for (m in moduleNameList) {
            def module = getModule(m)
            if (module) {
                if (module.dependsOn) {
                    result.addAll(getAllModuleNamesRequired(module.dependsOn))
                }
                result << m
            } else {
                throw new IllegalArgumentException("No module found with name [${m}]")
            }
        }

        return result
    }

    /**
     * Return a list of all the names of all modules required (included the input modules) to 
     * satisfy the dependencies of the input list of module names.
     */
    def getAllModuleNamesRequired(Map<String, Boolean> moduleNamesAndMandatory) {
        def result = []

        for (m in moduleNamesAndMandatory) {
            def module = getModule(m.key)
            if (module) {
                if (module.dependsOn) {
                    def depModules = getAllModuleNamesRequired(module.dependsOn)
                    for (dep in depModules) {
                        if (result.indexOf(dep) == -1) {
                            result << dep
                        }
                    }
                }
                result << m.key
            } else if (m.value && m.key != ResourceProcessor.ADHOC_MODULE) {
                throw new IllegalArgumentException("No module found with name [${m.key}]")
            }
        }

        return result
    }

    /**
     * Return a the module names sorted in first to last dependency order, based on the required name list
     */
    def getModulesInDependencyOrder(moduleNameList) {
        def result = []

        def modules = moduleNameList as HashSet
        for (m in modulesInDependencyOrder) {
            if (modules.contains(m)) {
                result << m
            }
        }

        return result
    }

    /**
     * Add dispositions for a module to a Request.
     *
     * @param request to add dispositions to
     * @param moduleName
     */
    void addModuleDispositionsToRequest(request, String moduleName) {
        log.debug "Adding module dispositions for module [${moduleName}]"

        def moduleNamesRequired = getAllModuleNamesRequired([moduleName])
        moduleNamesRequired.each { name ->
            def module = modulesByName[name]
            if (module) {
                for (d in module.requiredDispositions) {
                    DispositionsUtils.addDispositionToRequest(request, d, moduleName)
                }
            }
        }
    }

}
