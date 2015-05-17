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
package de.ethinking.gradle.plugin.deploy

import de.ethinking.gradle.gaffer.tasks.ApplicationAssembleTask
import de.ethinking.gradle.gaffer.tasks.ProfileApplicationAssembleTask
import de.ethinking.gradle.gaffer.tasks.WebappAssembleTask

import java.io.File;
import java.util.Collection;




import org.apache.jasper.compiler.Node.ParamsAction;
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.*

class DeployPluginTest{

    @Test
    public void extensionTest() {

        Project extensionProject = ProjectBuilder.builder().build()

        Project subProjectB = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-b").build()
        subProjectB.with{
            apply plugin:'java'
            apply plugin:'war'
            configurations{ runtimeWar }
        }

        Project subProjectA = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-a").build()
        subProjectA.with{
            apply plugin:'java'
            configurations{ runtimeWar }
        }

        extensionProject.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{
                application{
                    name = 'tomcat'
                    copy{
                        from distributionDependency('org.apache:apache-tomcat:8.0.14')
                        into '/opt/tomcat'
                    }
                    copy{
                        from 'location-1'
                        from 'location-2'
                        into 'location-3'
                    }
                }
                application("tomcat7"){
                    copy{
                        from distributionDependency('org.apache:apache-tomcat:7.0.59')
                        into '/opt/tomcat'
                    }
                }
            }

            gaffer{
                webapp{
                    name 'mywebapp'
                    copy{
                        into('/WEB-INF/lib'){
                            from project(':subproject-b').configurations.runtimeWar
                            from project(':subproject-b').jar
                        }
                      copy{
                          into 'test'
                          from project(':subproject-b').file('src/main/java')
                      }
                    }
                    warProject 'subproject-b','runtimeWar'
                    warProject 'subproject-a'
                }
            }
            gaffer{
                profile{
                    name = 'base'
                    application{
                        name = 'tomcat'
                        copy{
                            from  'sublocation-1'
                            from  project(':subproject-a').jar
                            into '/opt/tomcat/conf'
                        }
                    }
                }


                profile{
                    name = 'frontend'
                    application{
                        name = 'tomcat'
                        copy{
                            from webapp('webapp1')
                            into '/opt/tomcat/webapps'
                        }
                        copy{
                            from  'foo'
                            into '/opt/tomcat/conf'
                        }
                    }
                }
                profile{
                    name = 'solr'
                    application{
                        name = 'tomcat'
                        copy{
                            from webapp('webapp1')
                            into '/opt/tomcat/webapps'
                        }
                        copy{
                            from  'test/server.xml'
                            into '/opt/tomcat/conf'
                        }
                    }
                }
            }
        }



        extensionProject.evaluate()

        assertEquals(2, extensionProject.gaffer.findApplicationByName('tomcat').copyExtensions.size())
        assertEquals(1, extensionProject.gaffer.findApplicationByName('tomcat7').copyExtensions.size())
        assertTrue(extensionProject.gaffer.findWebappByName('mywebapp') != null)
        assertTrue(extensionProject.tasks.getByName('assemble-webapp-mywebapp') instanceof WebappAssembleTask)
        assertTrue(extensionProject.tasks.getByName('assemble-application-tomcat') instanceof ApplicationAssembleTask)
        assertTrue(extensionProject.gaffer.findProfileByName('frontend').createTaskDependencies().contains("assemble-webapp-webapp1"))
        assertTrue(extensionProject.gaffer.findProfileByName('frontend').createTaskDependencies().contains("assemble-application-tomcat"))
        assertTrue(extensionProject.gaffer.findProfileByName('base').createTaskDependencies().contains(":subproject-a:jar"))
    }


    @Test
    public void applicationAssembleTest() {

        Project extensionProject = ProjectBuilder.builder().build()
        Project subProjectB = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-b").build()
        subProjectB.with{
            apply plugin:'java'
            apply plugin:'war'
            configurations{ runtimeWar }
        }

        Project subProjectA = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-a").build()
        subProjectA.with{
            apply plugin:'java'
            configurations{ runtimeWar }
        }
        Project subProjectX = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-x").build()
        subProjectX.with{
            apply plugin:'java'
            configurations{ runtimeWar }
        }


        Project subProjectDeploy = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-deploy").build()

        subProjectDeploy.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{
                application{
                    name = 'test'
                    copy{
                        from  project(':subproject-a').jar
                        from  project(':subproject-a').configurations.runtimeWar
                        into '/test/'
                    }
                    copy{
                        from project(':subproject-b').jar
                        into '/test/'
                    }
                    copy{
                        into('bin'){
                            from project(':subproject-b').file('src/main/conf/bin')
                        }
                    }
                }
            }
            gaffer{
                application{
                    name = 'test2'
                    basePath = "/opt/test"
                    copy{
                        into('bin'){
                            from project(':subproject-b').file('src/main/conf/bin')
                        }
                    }
                }
                
                webapp{
                    name 'mywebapp'
                    copy{
                        from project(':subproject-x').configurations.runtimeWar
                        from project(':subproject-a').file('src/main/javascript')
                        into '/WEB-INF/lib'
                    }
                }
            }
        }




        extensionProject.evaluate()
        subProjectX.evaluate()
        subProjectA.evaluate()
        subProjectB.evaluate()
        subProjectDeploy.evaluate()

        assertEquals(3, subProjectDeploy.gaffer.findApplicationByName('test').copyExtensions.size())
        assertTrue(subProjectDeploy.gaffer.findApplicationByName('test').createTaskDependencies().contains(":subproject-a:jar"))
        assertTrue(subProjectDeploy.gaffer.findApplicationByName('test').createTaskDependencies().contains(":subproject-b:jar"))
        
        subProjectDeploy.tasks.getByName('assemble-webapp-mywebapp').execute()
        
    }

    @Test
    public void containerDSLTest() {
        Project extensionProject = ProjectBuilder.builder().build()
        Project subProjectA = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-a").build()
        subProjectA.with{ apply plugin: 'java' }

        extensionProject.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{

                application('tomcat'){

                    copy{
                        into('/lib'){
                            from project(':subproject-a').jar
                        }
                    }
                }


                profile{
                    name = 'frontend'
                    application{ name = 'tomcat' }
                }
                profile{
                    name = 'live'
                    application{ name = 'tomcat' }
                }
                profile{
                    name = 'solr'
                    application{ name = 'tomcat' }
                }

                container{
                    name = 'frontend'
                    buildfile = 'file'
                    profiles 'frontend','live','live'
                }
                container('test'){
                    applications 'tomcat'
                    params.env = ["param1":"value1","param2":"value2"]
                }
            }
        }
        extensionProject.evaluate()

        assertEquals(2,extensionProject.gaffer.containers.size())
        assertEquals('frontend',extensionProject.gaffer.containers[0].name)
        assertEquals('test',extensionProject.gaffer.containers[1].name)
        assertEquals(2,extensionProject.gaffer.containers[0].profiles.size())
        assertEquals(0,extensionProject.gaffer.containers[1].profiles.size())
        assertEquals(1,extensionProject.gaffer.containers[1].applications.size())
        assertTrue(extensionProject.tasks.getByName('assemble-profile-frontend-application-tomcat') instanceof ProfileApplicationAssembleTask)
        assertTrue(extensionProject.tasks.getByName('assemble-container-test-application-tomcat') instanceof ApplicationAssembleTask)
        assertNotNull(extensionProject.tasks.getByName('assemble-container-test-application-tomcat').applicationAssemble)
        assertNotNull(extensionProject.tasks.getByName('assemble-container-test-application-tomcat').assemble)

        assertNotNull(extensionProject.gaffer.containers[1].params.env)
        assertEquals("value1",extensionProject.gaffer.containers[1].params.env.param1)
        assertEquals("value2",extensionProject.gaffer.containers[1].params.env.param2)


        assertTrue(extensionProject.tasks.getByName('assemble-container-test-application-tomcat').taskDependencies.getDependencies().contains(subProjectA.jar))
    }







    @Test
    public void extraPropertyDSLTest() {
        Project extensionProject = ProjectBuilder.builder().build()

        extensionProject.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{

                application{ name = 'tomcat' }

                profile{
                    name = 'frontend'
                    application{ name = 'tomcat' }
                }
                profile{
                    name = 'live'
                    application{ name = 'tomcat' }
                }
                profile{
                    name = 'solr'
                    application{ name = 'tomcat' }
                }

                container{
                    name = 'frontend'
                    buildfile = 'file'
                    profiles 'frontend','live','live'
                    params.escenicFamily='live'
                }
            }
        }
        extensionProject.evaluate()

        assertEquals(1,extensionProject.gaffer.containers.size())
        assertEquals('frontend',extensionProject.gaffer.containers[0].name)
        assertEquals(2,extensionProject.gaffer.containers[0].profiles.size())
        assertTrue(extensionProject.tasks.getByName('assemble-profile-frontend-application-tomcat') instanceof ProfileApplicationAssembleTask)
        assertEquals('live',extensionProject.tasks.getByName('assemble-container-frontend').params.escenicFamily)
    }


    //@Test
    public void webappDSLTest() {
        Project extensionProject = ProjectBuilder.builder().build()

        Project subProjectB = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-b").build()
        subProjectB.with{
            apply plugin:'java'
            apply plugin:'war'
            configurations{ runtimeWar }
        }

        Project subProjectA = ProjectBuilder.builder().withParent(extensionProject).withName("subproject-a").build()
        subProjectA.with{
            apply plugin:'java'
            configurations{ runtimeWar }
        }

        extensionProject.with{
            apply plugin: 'de.ethinking.gaffer'
            gaffer{
                webapp{
                    name 'mywebapp'
                    copy{
                        from project(':subproject-b').configurations.runtimeWar
                        from project(':subproject-b').jar
                        into '/WEB-INF/lib'
                    }
                    project 'subproject-b','runtimeWar'
                    project 'subproject-a'
                    war 'clean'
                }
            }
        }
        extensionProject.evaluate()
        assertTrue(extensionProject.gaffer.findWebappByName('mywebapp') != null)
        assertEquals(4,extensionProject.gaffer.findWebappByName('mywebapp').copyExtensions.size())
        assertTrue(extensionProject.tasks.getByName('assemble-webapp-mywebapp') instanceof WebappAssembleTask)
    }
}
