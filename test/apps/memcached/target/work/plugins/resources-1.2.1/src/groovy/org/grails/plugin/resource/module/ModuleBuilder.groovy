package org.grails.plugin.resource.module

import org.slf4j.LoggerFactory

/**
 * Implements the DSL for a resource as part of a module.
 * 
 * Is designed to be reused for multiple invocations. The caller is
 * responsible for clearing the given resources and dependencies between invocations.
 *
 * @author Marc Palmer (marc@grailsrocks.com)
 * @author Luke Daley (ld@ldaley.com)
 */
class ModuleBuilder {
    
    private final _data

    private final log = LoggerFactory.getLogger(this.class.name)
    
    ModuleBuilder(def data) {
        _data = data    
    }
        
    void dependsOn(List dependencies) {
        _data.dependencies.addAll(dependencies)
    } 

    void dependsOn(String[] dependencies) {
        _data.dependencies.addAll(dependencies.toList())
    } 
    
    void dependsOn(String dependencies) {
        dependsOn(dependencies.split(',')*.trim())
    } 
    
    void defaultBundle(value) {
        _data.defaultBundle = value
    }   
    
    Object getResource() {
        throw new IllegalArgumentException("You must supply arguments to 'resource' - check that you do not have a line break before your argument list, or add parentheses")
    }

    void resource(args) {
        _data.resources << args
    }
    
    def missingMethod(String name, args) {
        throw new RuntimeException("Method calls such as ${name}($args) not yet supported by the builder!")
    }
}