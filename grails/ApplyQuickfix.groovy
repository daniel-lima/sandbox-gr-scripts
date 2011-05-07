/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Daniel Henrique Alves Lima
 */
includeTargets << grailsScript("_GrailsCompile")

target(default: "Apply a quick and dirty fix to Grails libraries") {

  compile()

  def jars = new File(System.env["GRAILS_HOME"] + "/dist").listFiles({file, name-> name.startsWith('grails') && name.endsWith('.jar')} as FilenameFilter)
  
  
  ['grails-gorm': ['org/codehaus/groovy/grails/plugins/orm/**'],
   'grails-spring': ['org/codehaus/groovy/grails/commons/spring/**']].entrySet().each {
    jarAndDirs ->
      for (jar in jars) {
	if (jar.name.startsWith(jarAndDirs.key)) {
	  def backup = new File(jar.absolutePath + ".backup")
	  if (!backup.exists()) {
	    ant.copy(file: jar, tofile: backup, preservelastmodified: true, failonerror: true)
	  }

	  ant.jar(destfile: jar, update: true) {
	    fileset(dir: grailsSettings.classesDir.path) {
	      jarAndDirs.value.each {
		include(name: it)
	      }
	    }
	  }	  
	}
      }
  }
  
  

  

}