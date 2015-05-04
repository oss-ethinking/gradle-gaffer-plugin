/*
 *  Copyright 2015 eThinking GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.ethinking.gradle.deploy.tasks


import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test



class BaseAssembleTaskTest {

    @Test
    def void testWebappReport(){

        Project extensionProject = ProjectBuilder.builder().build()

        extensionProject.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{
                application{ name = 'tomcat' }

                profile{
                    name = 'frontend'
                    application{ name = 'tomcat' }
                }
                container{
                    name = 'frontend'
                    profiles 'frontend'
                }
            }
        }
        extensionProject.evaluate()

        def task = extensionProject.tasks.getByName('assemble-container-frontend')
        task.report()
    }
}
