/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grails.project.work.dir = "target"
grails.project.dependency.resolution = {

  def spockVersion = "0.7-groovy-1.8"
  def isSnapshot = spockVersion.endsWith("-SNAPSHOT")
  
  def isSpockBuild = System.getProperty("spock.building") != null

  inherits "global" // inherit Grails' default dependencies
  log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

  repositories {
    grailsHome()
    grailsCentral()
    mavenCentral()
    if (!isSpockBuild) {
      mavenLocal()
    }
    if (isSnapshot) {
      mavenRepo "http://oss.sonatype.org/content/repositories/snapshots/"
    }
  }

  dependencies {
    if (!isSpockBuild) {
      compile("org.spockframework:spock-grails-support:${spockVersion}")
    }
  }

  plugins {
    compile(":release:2.0.3") {
      export = false
    }
  }
}

grails.release.scm.enabled = false