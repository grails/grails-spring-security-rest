package org.grails.plugin.resource.module

import org.slf4j.LoggerFactory

/**
 * Implements the resource modules DSL.
 * 
 * The caller provides a list at construction that will be populated during
 * DSL evaluation of maps defining the resource modules.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ModulesBuilder implements GroovyInterceptable {
    
    private _modules
    private _moduleOverrides
    private _collatedData
    private _moduleBuilder
    private boolean _strict
    
    static METHODNAME_OVERRIDES = 'overrides'
    
    private final log = LoggerFactory.getLogger(this.class.name)
    
    ModulesBuilder(List modules, strict = false) {
        _modules = modules
        _strict = strict
        _collatedData = [resources:[], dependencies:[]]
        _moduleBuilder = new ModuleBuilder(_collatedData)
    }
    
    def invokeMethod(String name, args) {
        if (args.size() == 1 && args[0] instanceof Closure) {

            if (name != METHODNAME_OVERRIDES) {
                
                if (_strict && _modules.find { m -> m.name == name}) {
                    throw new IllegalArgumentException("A module called [$name] has already been defined")
                }
                
                // build it
                def moduleDefinition = args[0]
                moduleDefinition.delegate = _moduleBuilder
                moduleDefinition.resolveStrategy = Closure.DELEGATE_FIRST
                moduleDefinition()

                def module = [name: name, 
                    resources: _collatedData.resources.clone(), 
                    defaultBundle: _collatedData.defaultBundle,
                    dependencies: _collatedData.dependencies.clone()]
            
                if (log.debugEnabled) {
                    log.debug("Defined module '$module'")
                }
            
                // add it
                _modules << module

                // clear for next
                _collatedData.clear()
                _collatedData.resources = []
                _collatedData.dependencies = []
                
            } else {
                
                if (log.debugEnabled) {
                    log.debug("Processing module overrides")
                }
                def nestedBuilder = new ModulesBuilder(_moduleOverrides == null ? [] : _moduleOverrides, false)
                def moduleDefinition = args[0]
                moduleDefinition.delegate = nestedBuilder
                moduleDefinition.resolveStrategy = Closure.DELEGATE_FIRST
                moduleDefinition()
                // Copy these nested decls into separate data for post-processing
                _moduleOverrides = nestedBuilder._modules
            }

        } else {
            throw new IllegalStateException("Only 1 closure argument is accepted (args were: $args)")
        }
    }

}