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

/**
 *  Usage  : groovy grails.groovy path_to_grails_project grails_command
 *  Example: groovy grails.groovy /tmp/some_project compile
 */
// println args
def baseDir = args[0]
def argList = System.properties["os.name"].contains("Windows")? ["cmd", "/c", "grails.bat"]: ["grails"]
argList.addAll((args as List).subList(1, args.length))
argList << "--non-interactive"

println "baseDir ${baseDir}"
println "command ${argList}"
def process = argList.execute(null, new File(baseDir))

[
    [System.out, process.in],
    [System.err, process.err]
].eachWithIndex {streams, idx ->
    def r = {
        def input = new InputStreamReader(streams[1])
        def output = streams[0]
        input.eachLine {line->
            output.println(line)
        }
    } as Runnable
    def t = new Thread(r)
    t.start()
}

process.waitFor()
System.exit process.exitValue()
