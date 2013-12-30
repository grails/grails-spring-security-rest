import grails.util.Environment
import org.grails.plugin.resource.util.HalfBakedLegacyLinkGenerator
import org.springframework.core.io.FileSystemResource
import org.springframework.util.AntPathMatcher

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ResourcesGrailsPlugin {

    static DEFAULT_URI_PREFIX = 'static'
    static DEFAULT_ADHOC_PATTERNS = ["/images/*", "*.css", "*.js"].asImmutable()

    def version = "1.2.1"
    def grailsVersion = "1.3 > *"

    def loadAfter = ['logging'] // retained to ensure correct loading under Grails < 2.0

    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/views/index.gsp",
            "grails-app/controllers/**/*.groovy",
            "web-app/css/**/*.*",
            "web-app/js/**/*.*",
            "web-app/images/**/*.*",
            "grails-app/resourceMappers/**/test/*",
            "grails-app/conf/*Resources.groovy"
    ]

    def artefacts = [getResourceMapperArtefactHandler(), getResourcesArtefactHandler()]
    def watchedResources = [
        "file:./grails-app/resourceMappers/**/*.groovy",
        "file:./plugins/*/grails-app/resourceMappers/**/*.groovy",
        "file:./grails-app/conf/*Resources.groovy",
        "file:./plugins/*/grails-app/conf/*Resources.groovy",
        "file:./web-app/**/*.*" // Watch for resource changes, we need excludes here for WEB-INF+META-INF when grails impls this
    ]

    def title = "Resources"
    def description = 'HTML resource management enhancements to replace g.resource etc.'
    def documentation = "http://grails-plugins.github.com/grails-resources"

    def license = "APACHE"
    def organization = [ name: "Grails Community", url: "http://grails.org/" ]
    def developers = [
            [ name: "Marc Palmer", email: "marc@grailsrocks.com" ],
            [ name: "Luke Daley", email: "ld@ldaley.com" ],
            [ name: "Peter N. Steinmetz", email: "ndoc3@steinmetz.org"]
    ]
    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPRESOURCES" ]
    def scm = [ url: "https://github.com/grails-plugins/grails-resources" ]
    
    def getWebXmlFilterOrder() {
        def FilterManager = getClass().getClassLoader().loadClass('grails.plugin.webxml.FilterManager')
        [   DeclaredResourcesPluginFilter: FilterManager.DEFAULT_POSITION - 300,
            AdHocResourcesPluginFilter: FilterManager.DEFAULT_POSITION - 250,
            ResourcesDevModeFilter: FilterManager.RELOAD_POSITION + 100]
    }

    def getResourcesConfig(application) {
        application.config.grails.resources
    }
    
    def getUriPrefix(application) {
        def prf = getResourcesConfig(application).uri.prefix
        prf instanceof String ? prf : DEFAULT_URI_PREFIX
    }
    
    def getAdHocPatterns(application) {
        def patterns = getResourcesConfig(application).adhoc.patterns
        patterns instanceof List ? patterns : DEFAULT_ADHOC_PATTERNS
    }
    
    def doWithSpring = { ->
        if (!springConfig.containsBean('grailsLinkGenerator')) {
            grailsLinkGenerator(HalfBakedLegacyLinkGenerator) {
                pluginManager = ref('pluginManager')
            }
        }
        
        grailsResourceProcessor(org.grails.plugin.resource.ResourceProcessor) {
            grailsLinkGenerator = ref('grailsLinkGenerator')
            if (springConfig.containsBean('grailsResourceLocator')) {
                grailsResourceLocator = ref('grailsResourceLocator')
            }
            grailsApplication = ref('grailsApplication')
        }
        
        // Legacy service name
        springConfig.addAlias "resourceService", "grailsResourceProcessor"
    }
    
    def doWithWebDescriptor = { webXml ->
        def adHocPatterns = getAdHocPatterns(application)

        def declaredResFilter = [   
                name:'DeclaredResourcesPluginFilter', 
                filterClass:"org.grails.plugin.resource.ProcessingFilter",
                urlPatterns:["/${getUriPrefix(application)}/*"]
        ]
        def adHocFilter = [   
            name:'AdHocResourcesPluginFilter', 
            filterClass:"org.grails.plugin.resource.ProcessingFilter",
            params: [adhoc:true],
            urlPatterns: adHocPatterns
        ]

        def filtersToAdd = [declaredResFilter]
        if (adHocPatterns) {
            filtersToAdd << adHocFilter
        }

        if ( Environment.current == Environment.DEVELOPMENT) {
            filtersToAdd << [   
                name:'ResourcesDevModeFilter', 
                filterClass:"org.grails.plugin.resource.DevModeSanityFilter",
                urlPatterns:['/*'],
                dispatchers:['REQUEST']
            ]
        }
        
        log.info("Adding servlet filters")
        def filters = webXml.filter[0]
        filters + {
            filtersToAdd.each { f ->
                log.info "Adding filter: ${f.name} with class ${f.filterClass} and init-params: ${f.params}"
                'filter' {
                    'filter-name'(f.name)
                    'filter-class'(f.filterClass)
                    f.params?.each { k, v ->
                        'init-param' {
                            'param-name'(k)
                            'param-value'(v.toString())
                        }
                    }
                }
            }
        }
        def mappings = webXml.'filter-mapping'[0] 
        mappings + {
            filtersToAdd.each { f ->
                f.urlPatterns?.each { p ->
                    log.info "Adding url pattern ${p} for filter ${f.name}"
                    'filter-mapping' {
                        'filter-name'(f.name)
                        'url-pattern'(p)
                        if (f.dispatchers) {
                            for (d in f.dispatchers) {
                                'dispatcher'(d)
                            }
                        }
                    }
                }
            }
        }
    }

    def doWithDynamicMethods = { applicationContext ->
        applicationContext.grailsResourceProcessor.staticUrlPrefix = "/${getUriPrefix(application)}"
        applicationContext.grailsResourceProcessor.reloadAll()
    }

    static PATH_MATCHER = new AntPathMatcher()
    static RELOADABLE_RESOURCE_EXCLUDES = [
        '**/.svn/**/*.*', 
        '**/.git/**/*.*',
        'WEB-INF/**/*.*',
        'META-INF/**/*.*'
    ]
    
    boolean isResourceWeShouldProcess(File file) {
        // Make windows filenams safe for matching
        def fileName = file.canonicalPath.replaceAll('\\\\', '/').replaceAll('^.*?/web-app/', '')
        boolean shouldProcess = !(RELOADABLE_RESOURCE_EXCLUDES.any { PATH_MATCHER.match(it, fileName ) })
        return shouldProcess
    }
    
    ScheduledThreadPoolExecutor delayedChangeThrottle = new ScheduledThreadPoolExecutor(1)
    ScheduledFuture reloadTask
    static final RELOAD_THROTTLE_DELAY = 500
    
    void triggerReload(Closure reloader) {
        reloadTask?.cancel(false)
        reloadTask = delayedChangeThrottle.schedule( { 
            try {
                reloader()
            } catch (Throwable t) {
                println "Resource reload failed!:"
                t.printStackTrace(System.out)
            }
        }, RELOAD_THROTTLE_DELAY, TimeUnit.MILLISECONDS)
    }
    
    def onChange = { event ->
        if (event.source instanceof FileSystemResource) {
            if (isResourceWeShouldProcess(event.source.file)) {
                log.info("Scheduling reload of resource files due to change of file $event.source.file")
                triggerReload {
                    event.application.mainContext.grailsResourceProcessor.reloadChangedFiles()
                }
            }
        } else if (handleChange(application, event, getResourceMapperArtefactHandler().TYPE, log)) {
            log.info("Scheduling reload of mappers due to change of $event.source.name")
            triggerReload {
                event.application.mainContext.grailsResourceProcessor.reloadMappers()
            }
        } else if (handleChange(application, event, getResourcesArtefactHandler().TYPE, log)) {
            log.info("Scheduling reload of modules due to change of $event.source.name")
            triggerReload {
                event.application.mainContext.grailsResourceProcessor.reloadModules()
            }
        }
    }

    protected handleChange(application, event, type, log) {
        if (application.isArtefactOfType(type, event.source)) {
            log.debug("reloading $event.source.name ($type)")
            def oldClass = application.getArtefact(type, event.source.name)
            application.addArtefact(type, event.source)
            // Reload subclasses
            if (oldClass) {
                application.getArtefacts(type).each {
                    if (it.clazz != event.source && oldClass.clazz.isAssignableFrom(it.clazz)) {
                        def newClass = application.classLoader.reloadClass(it.clazz.name)
                        application.addArtefact(type, newClass)
                    }
                }
            }
                        
            true
        } else {
            false
        }
    }

    def onConfigChange = { event ->
        event.application.mainContext.grailsResourceProcessor.reloadModules()
    }

    /**
     * We have to soft load this class so this file can be compiled on it's own.
     */
    static getResourceMapperArtefactHandler() {
        softLoadClass('org.grails.plugin.resources.artefacts.ResourceMapperArtefactHandler')
    }

    static getResourcesArtefactHandler() {
        softLoadClass('org.grails.plugin.resources.artefacts.ResourcesArtefactHandler')
    }

    static softLoadClass(String className) {
        try {
            getClassLoader().loadClass(className)
        } catch (ClassNotFoundException e) {
            null
        }
    }
}
