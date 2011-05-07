/*
 * Copyright 2010-2011 the original author or authors.
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
includeTargets << grailsScript("_GrailsArgParsing")

USAGE = """
    webxml-to-securitycore [--xml=path_to_xml] [--config=path_to_config_groovy]

where

"""

target(default: "Convert security constraints in web.xml to Spring Security Core configurations") {
    depends(parseArguments)

    //println "argsMap ${argsMap}"
  
    def xmlFile = new File(argsMap.xml)
    println "xmlFile ${argsMap.xml}"
    def webXml = new groovy.util.XmlParser().parseText(xmlFile.text)

    def configFile = new File(argsMap.config? argsMap.config : "${basedir}/grails-app/conf/Config.groovy")
    println "configFile ${configFile}"
    def writer1 = new PrintWriter(new FileWriter(configFile, true))
    def sWriter2 = new StringWriter()
    def writer2 = new PrintWriter(sWriter2)

    writer1.println "/*"
    writer1.println "grails.plugins.springsecurity.interceptUrlMap = ["
    writer2.println "grails.plugins.springsecurity.secureChannel.definition = ["
    def securityConstraints = webXml.'security-constraint'
    securityConstraints.eachWithIndex {securityConstraint, idx1 ->
        def webResourceCollection = securityConstraint.'web-resource-collection'
        def webResourceName = webResourceCollection.'web-resource-name'.text()
    
        def hasAuthConstraint = false
        securityConstraint.children().each {
            hasAuthConstraint = hasAuthConstraint || 'auth-constraint'.equals(it.name())
        }

        def transportGuarantee = securityConstraint.'user-data-constraint'.'transport-guarantee'
        if (transportGuarantee) {
            transportGuarantee = transportGuarantee.text()
        }
        def hasTransportGuarantee = transportGuarantee
        def useSsl = hasTransportGuarantee && !transportGuarantee.toUpperCase().equals('NONE')

        def urlPatterns = webResourceCollection.'url-pattern'
        def authConstraint = securityConstraint.'auth-constraint'
        def roleNames = null
        def roleNamesText = new StringBuilder()

        if (hasAuthConstraint) {
            roleNames = authConstraint.'role-name'
            if (roleNames && roleNames.size() > 0) {
                roleNamesText.append("[")
                def s = ''
                roleNames.each {roleName ->
                    roleNamesText.append(s)
                    roleNamesText.append("'ROLE_${roleName.text()}'")
                    s = ', '
                }
                roleNamesText.append("]")
            } else {
                roleNamesText.append("['IS_AUTHENTICATED_FULLY']")
            }
        } else {
            roleNamesText.append("['IS_AUTHENTICATED_ANONYMOUSLY']")
        }
    
        writer1.println ""
        writer1.println "   // ${webResourceName}"
        if (hasTransportGuarantee) {
            writer2.println ""
            writer2.println "   // ${webResourceName}"
        }
        urlPatterns.eachWithIndex {urlPattern, idx2 ->
            def urlPatternText = urlPattern.text()
            if (urlPatternText.endsWith('*')) {
                urlPatternText += '*'
            } else if (urlPatternText.startsWith('*.')) {
                urlPatternText = '/**/' + urlPatternText
            }
 
            writer1.print "   '${urlPatternText}': ${roleNamesText}"
            if (hasTransportGuarantee) {
                writer2.print "   '${urlPatternText}': " + (useSsl? "'REQUIRES_SECURE_CHANNEL'" : "'ANY_CHANNEL'")
            }
            if (idx2 < urlPatterns.size() - 1 || idx1 < securityConstraints.size() - 1) {
                writer1.println ", "
                if (hasTransportGuarantee) {
                    writer2.println ", "
                }
            } else {
                writer1.println ""
                if (hasTransportGuarantee) {
                    writer2.println ""
                }
            }
        }
    }
    writer1.println "]"
    writer2.println "]"
    writer2.close()
    writer1.println ""
    writer1.print(sWriter2.toString())
    sWriter2 = null
    writer1.println "*/"
    writer1.println ""
    writer1.println "/* grails.plugins.springsecurity.rejectIfNoRule = true */"
    writer1.close()
  

}