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
package de.ethinking.gradle.plugin



import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.War
import org.gradle.tooling.BuildException
import org.gradle.api.tasks.Delete

import de.ethinking.gradle.gaffer.assemble.ApplicationAssemble
import de.ethinking.gradle.gaffer.assemble.ContainerAssemble
import de.ethinking.gradle.gaffer.assemble.ProfileAssemble
import de.ethinking.gradle.gaffer.assemble.WebappAssemble
import de.ethinking.gradle.gaffer.GafferExtension
import de.ethinking.gradle.gaffer.LifecycleState
import de.ethinking.gradle.gaffer.report.DeploymentReport
import de.ethinking.gradle.gaffer.repository.DynamicDependencyResolver;
import de.ethinking.gradle.gaffer.tasks.ApplicationAssembleTask
import de.ethinking.gradle.gaffer.tasks.ContainerAssembleTask
import de.ethinking.gradle.gaffer.tasks.WebappAssembleTask

class GafferBasePlugin  implements Plugin<Project> {

    void apply(Project project) {

        addClosures(project)
        project.extensions.create("gaffer",GafferExtension,project)

        project.getRootProject().allprojects { Project otherProject ->
            if(!otherProject.getPath().equals(project.getPath())){
                project.evaluationDependsOn(otherProject.getPath())
            }
        }

        project.afterEvaluate {
            if(project.gaffer){
                project.gaffer.switchState(LifecycleState.CONFIGURED)
                addTasks(project)
            }
        }
    }

    def addTasks(Project project){
        DeploymentReport report = new DeploymentReport()
        addApplicationTasks(project,report)
        addWebappTasks(project,report)
        //addProfileTasks(project, report)
        addContainerTasks(project, report)
    }

    def addApplicationTasks(Project project,DeploymentReport report){

        project.gaffer.applicationAssembles.each{ ApplicationAssemble assemble ->
            File assembleTargetDir = new File(project.getBuildDir(),"assemble/application/"+assemble.name)
            project.task("assemble-application-"+assemble.name,type:ApplicationAssembleTask,group:"application assemble"){
                setAssemble(assemble)
                applicationAssemble = assemble
                deploymentReport = report
                targetDirectory=assembleTargetDir
            }
        }
    }

    def addWebappTasks(Project project,DeploymentReport report){

        project.gaffer.webappAssembles.each{WebappAssemble  assemble ->

            File assembleTargetDir = new File(project.getBuildDir(),"assemble/webapp/"+assemble.name)
            //create assemble task

            Set<String> webappDependencies = new HashSet<String>()

            String cleanWebappTask = "cleanWebapp-"+assemble.name
            project.task(cleanWebappTask,type:Delete){ delete assembleTargetDir }
            webappDependencies.add(cleanWebappTask)


            String webappAssembleTaskName = "assemble-webapp-"+assemble.name

            webappDependencies.addAll(assemble.createTaskDependencies(project))
            project.task(webappAssembleTaskName,dependsOn: webappDependencies,type:WebappAssembleTask,group:"webapp assemble"){
                setAssemble(assemble)
                webappAssemble = assemble
                deploymentReport = report
                targetDirectory=assembleTargetDir
            }
            project.task("war-"+assemble.name,type:War,dependsOn:webappAssembleTaskName){
                from assembleTargetDir
                archiveName=assemble.name+".war"
            }
        }
    }



    def addContainerTasks(Project project,DeploymentReport report){

        project.gaffer.containers.each{ ContainerAssemble containerAssemble ->

            Set<String> containerDependencies = new HashSet<String>()
            Set<String> usedApplications = new HashSet<String>()
            File assembleTargetDir = new File(project.getBuildDir(),"assemble/container/"+containerAssemble.name)

            String cleanContainerTask = "cleanContainer-"+containerAssemble.name
            project.task(cleanContainerTask,type:Delete){ delete assembleTargetDir }
            containerDependencies.add(cleanContainerTask)

            List<ApplicationAssemble> applicationAssembles = []
            List<ApplicationAssemble> profileAssembles = []

            containerAssemble.profiles.each { String profile ->
                ProfileAssemble containerProfileAssemble = project.gaffer.findProfileByName(profile)
                if(containerProfileAssemble){
                    containerDependencies.addAll(containerProfileAssemble.createTaskDependencies())
                    //assemble all applications into container assemble directory
                    containerProfileAssemble.applicationAssembles.each{ ApplicationAssemble profileApplicationAssemble ->
                        profileAssembles.add(profileApplicationAssemble)

                        ApplicationAssemble baseApplicationAssemble  = project.gaffer.findApplicationByName(profileApplicationAssemble.name)
                        if(baseApplicationAssemble){
                            if(!usedApplications.contains(profileApplicationAssemble.name)){
                                usedApplications.add(profileApplicationAssemble.name)
                                applicationAssembles.add(baseApplicationAssemble)
                                containerDependencies.addAll(baseApplicationAssemble.createTaskDependencies())
                            }
                            if(!profileApplicationAssemble.basePath){
                                profileApplicationAssemble.basePath=baseApplicationAssemble.basePath
                            }
                        }else{
                            throw new BuildException("Application:"+profileApplicationAssemble.name+" referenced in profile:"+profile+" but not defined.",new Exception())
                        }

                    }
                }else{
                    throw new BuildException("Profile:"+profile+" referenced in container:"+containerAssemble.name+" but not defined.",new Exception())
                }
            }
            containerAssemble.applications.each{ String applicationName ->
                if(!usedApplications.contains(applicationName )){
                    usedApplications.add(applicationName)
                    ApplicationAssemble applicationAssemble = project.gaffer.findApplicationByName(applicationName)
                    if(applicationAssemble){
                        applicationAssembles.add(applicationAssemble)
                        containerDependencies.addAll(applicationAssemble.createTaskDependencies())
                    }else{
                        throw new BuildException("Application:"+applicationName+" referenced in container:"+containerAssemble.name+" but not defined.",new Exception())
                    }
                }
            }

            String taskName = "assemble-container-"+containerAssemble.name

            ContainerAssembleTask containerTask = project.task(taskName,dependsOn:containerDependencies,type:ContainerAssembleTask,group:"container assemble")
            containerTask.setAssemble(containerAssemble)
            containerTask.setContainerAssemble(containerAssemble)
            containerTask.targetDirectory=assembleTargetDir
            containerTask.deploymentReport = report
            containerTask.applicationAssembles.addAll(applicationAssembles)
            containerTask.profileApplicationAssembles.addAll(profileAssembles)

        }
    }


    public static void addClosures(Project project){
        project.ext.webapp = { String webappName ->
            def directory = new File(project.getBuildDir(),"assemble/webapp/"+webappName)
            return directory
        }

        project.ext.distributionDependency= { String distributionDependency ->
            try{
                if(project.gaffer.lifecycleState==LifecycleState.INITIALIZING){
                    return []
                }else{
                    def remove=true
                    DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                    return resolver.resolveToFiles(distributionDependency,remove)
                }
            }catch(Exception e){
                project.logger.error("Faild on resolve distributionDependency:"+distributionDependency,e);
            }
        }


        project.ext.flatDependency= { String flatDependency ->
            try{
                if(project.gaffer.lifecycleState==LifecycleState.INITIALIZING){
                    return []
                }else{
                    def remove=false
                    DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                    return resolver.resolveToFiles(flatDependency,remove)
                }
            }catch(Exception e){
                project.logger.error("Faild on resolve flatDependency:"+flatDependency,e);
            }
        }


        project.ext.dependency= { String dependency ->
            try{
                if(project.gaffer.lifecycleState==LifecycleState.INITIALIZING){
                    return []
                }else{
                    DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                    return resolver.resolveToFile(dependency)
                }
            }catch(Exception e){
                project.logger.error("Faild on resolve dependency:"+dependency,e);
            }
        }

        project.ext.searchProject= { String  name ->
            Project rootProject = project.getParent()
            Project parent = rootProject
            while(parent != null){
                rootProject = parent
                parent = parent.getParent()
            }
            if(name.contains(":")){
                return rootProject.findProject(name)
            }else{
                if(rootProject != null){
                    for(Project subproject:rootProject.allprojects){
                        if(subproject.getName().equals(name)){
                            return subproject
                        }
                    }
                }
            }
            return null
        }
    }
}
