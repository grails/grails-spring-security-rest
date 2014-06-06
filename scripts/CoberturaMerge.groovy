includeTargets << grailsScript("_GrailsInit")

//Mostly borrowed from https://github.com/beckje01/grails-code-coverage/blob/master/scripts/_Events.groovy

target(coberturaMerge: "The description of the script goes here!") {
    ant.taskdef(classpathRef: 'grails.test.classpath', resource: "tasks.properties")
    ant.'cobertura-merge' {
        fileset(dir: basedir) {
            include(name:"**/*.ser")
        }
    }
    createCoverageReports()
    replaceClosureNamesInReports()
}

def createCoverageReports() {
    coverageReportDir = "${basedir}/target/test-reports/cobertura"

    ant.mkdir(dir: "${coverageReportDir}")

    coverageReportFormats = ['html']
    if (argsMap.xml || buildConfig.coverage.xml) {
        coverageReportFormats << 'xml'
    }

    coverageReportFormats.each {reportFormat ->
        ant.'cobertura-report'(destDir: "${coverageReportDir}", datafile: "${dataFile}", format: reportFormat) {
            //load all these dirs independently so the dir structure is flattened,
            //otherwise the source isn't found for the reports
            fileset(dir: "${basedir}/grails-app/controllers", erroronmissingdir: false)
            fileset(dir: "${basedir}/grails-app/domain", erroronmissingdir: false)
            fileset(dir: "${basedir}/grails-app/services", erroronmissingdir: false)
            fileset(dir: "${basedir}/grails-app/taglib", erroronmissingdir: false)
            fileset(dir: "${basedir}/grails-app/utils", erroronmissingdir: false)
            fileset(dir: "${basedir}/src/groovy", erroronmissingdir: false)
            fileset(dir: "${basedir}/src/java", erroronmissingdir: false)
            if (buildConfig.coverage?.sourceInclusions) {
                buildConfig.coverage.sourceInclusions.each {
                    fileset(dir: "${basedir}/${it}")
                }
            }
        }
    }
}

def replaceClosureNamesInReports() {
    if (!argsMap.nopost || !buildConfig.coverage.noPost) {
        def startTime = new Date().time

        def hasGrailsApp = hasProperty('grailsApp')

        replaceClosureNames(hasGrailsApp ? grailsApp?.controllerClasses : null)
        replaceClosureNamesInXmlReports(hasGrailsApp ? grailsApp?.controllerClasses : null)
        def endTime = new Date().time
        println "Done with post processing reports in ${endTime - startTime}ms"
    }
}

def replaceClosureNames(artefacts) {
    artefacts?.each {artefact ->
        artefact.reference.propertyDescriptors.findAll { descriptor ->
            GrailsClassUtils.isGroovyAssignableFrom(Closure, descriptor.propertyType)
        }.each {propertyDescriptor ->
            def closureClassName = artefact.getPropertyOrStaticPropertyOrFieldValue(propertyDescriptor.name, Closure)?.class?.name
            if (closureClassName) {
                // the name in the reports is sans package; subtract the package name
                def nameToReplace = closureClassName - "${artefact.packageName}."

                ant.replace(dir: "${coverageReportDir}",
                        token: ">${nameToReplace}<",
                        value: ">${artefact.shortName}.${propertyDescriptor.name}<") {
                    include(name: "**/*${artefact.fullName}.html")
                    include(name: "frame-summary*.html")
                }
            }
        }
    }
}

def replaceClosureNamesInXmlReports(artefacts) {
    def xml = new File("${coverageReportDir}/coverage.xml")
    if (xml.exists()) {
        def p = new XmlParser()
        p.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        p.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        def parser = p.parse(xml)

        artefacts?.each {artefact ->
            artefact.reference.propertyDescriptors.findAll { descriptor ->
                GrailsClassUtils.isGroovyAssignableFrom(Closure, descriptor.propertyType)
            }.each {propertyDescriptor ->
                def closureClassName = artefact.getPropertyOrStaticPropertyOrFieldValue(propertyDescriptor.name, Closure)?.class?.name
                if (closureClassName) {
                    def node = parser['packages']['package']['classes']['class'].find {it.@name == closureClassName}
                    if (node) {
                        node.@name = "${artefact.fullName}.${propertyDescriptor.name}"
                    }
                }
            }
        }

        xml.withPrintWriter {writer ->
            new XmlNodePrinter(writer).print(parser)
        }
    }
}

setDefaultTarget(coberturaMerge)
