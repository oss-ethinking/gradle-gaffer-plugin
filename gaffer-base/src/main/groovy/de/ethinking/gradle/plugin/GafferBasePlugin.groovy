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
import de.ethinking.gradle.gaffer.tasks.ProfileApplicationAssembleTask
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
        addProfileTasks(project, report)
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
            String webappAssembleTaskName = "assemble-webapp-"+assemble.name

            project.task(webappAssembleTaskName,dependsOn: assemble.createTaskDependencies(project),type:WebappAssembleTask,group:"webapp assemble"){
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

    def addProfileTasks(Project project,DeploymentReport report){
        project.gaffer.profiles.each{ ProfileAssemble assemble ->
            File assembleTargetDir = new File(project.getBuildDir(),"assemble/profile/"+assemble.name)
            assemble.applicationAssembles.each{ ApplicationAssemble profileApplicationAssemble ->
                File applicationAssembleTargetDir = new File(assembleTargetDir,profileApplicationAssemble.name)

                ApplicationAssemble baseApplicationAssemble  = project.gaffer.findApplicationByName(profileApplicationAssemble.name)
                if(baseApplicationAssemble && !assemble.basePath){
                    assemble.basePath=baseApplicationAssemble.basePath
                }
                project.task("assemble-profile-"+assemble.name+"-application-"+profileApplicationAssemble.name,dependsOn:profileApplicationAssemble.createTaskDependencies(),type:ProfileApplicationAssembleTask,group:"profile assemble"){
                    profileAssemble = assemble
                    setAssemble(assemble)
                    applicationAssemble = profileApplicationAssemble
                    deploymentReport = report
                    targetDirectory=applicationAssembleTargetDir
                }
            }
        }
    }


    def addHostTasks(Project project,DeploymentReport report){


    }

    def addContainerTasks(Project project,DeploymentReport report){


        Set<String> allTasks = new  HashSet<String>()

        project.gaffer.containers.each{ ContainerAssemble containerAssemble ->
            Set<String> containerDependencies = new HashSet<String>()
            File assembleTargetDir = new File(project.getBuildDir(),"assemble/container/"+containerAssemble.name)
            containerAssemble.profiles.each { String profile ->

                ProfileAssemble containerProfileAssemble = project.gaffer.findProfileByName(profile)

                if(containerProfileAssemble){
                    Set<String> profileDependencies = new  HashSet<String>()
                    //assemble all applications into container assemble directory
                    containerProfileAssemble.applicationAssembles.each{ ApplicationAssemble profileApplicationAssemble ->
                        ApplicationAssemble baseApplicationAssemble  = project.gaffer.findApplicationByName(profileApplicationAssemble.name)
                        if(baseApplicationAssemble){
                            String taskName = "assemble-container-"+containerAssemble.name+"-application-"+baseApplicationAssemble.name
                            if(!allTasks.contains(taskName)){
                                project.task(taskName,dependsOn:baseApplicationAssemble.createTaskDependencies(),type:ApplicationAssembleTask,group:"application assemble"){
                                    setAssemble(baseApplicationAssemble)
                                    applicationAssemble = baseApplicationAssemble
                                    deploymentReport = report
                                    targetDirectory=assembleTargetDir
                                }
                                allTasks.add(taskName)
                                profileDependencies.add(taskName)
                            }

                        }else{
                            println "No Application found:"+profileApplicationAssemble.name
                        }
                        profileDependencies.addAll(profileApplicationAssemble.createTaskDependencies());
                        String taskName = "assemble-container-"+containerAssemble.name+"-profile-"+containerProfileAssemble.name+"-application-"+profileApplicationAssemble.name
                        if(!profileApplicationAssemble.basePath){
                            profileApplicationAssemble.basePath=baseApplicationAssemble.basePath
                        }
                        project.task(taskName,dependsOn:profileDependencies,type:ProfileApplicationAssembleTask,group:"profile assemble"){
                            setAssemble(containerProfileAssemble)
                            profileAssemble = containerProfileAssemble
                            applicationAssemble = profileApplicationAssemble
                            deploymentReport = report
                            targetDirectory=assembleTargetDir
                        }
                        allTasks.add(taskName)
                        containerDependencies.add(taskName)
                    }
                }else{
                    throw new BuildException("Profile:"+profile+" referenced in container:"+containerAssemble.name+" but not defined.",new Exception())
                }
            }
            containerAssemble.applications.each{ String applicationName ->

                ApplicationAssemble applicationAssemble = project.gaffer.findApplicationByName(applicationName)
                String taskName ="assemble-container-"+containerAssemble.name+"-application-"+applicationAssemble.name
                if(!allTasks.contains(taskName)){
                    project.task(taskName,dependsOn:applicationAssemble.createTaskDependencies(),type:ApplicationAssembleTask,group:"application assemble"){
                        setAssemble(applicationAssemble)
                        setApplicationAssemble(applicationAssemble)
                        deploymentReport = report
                        targetDirectory=assembleTargetDir
                    }
                    allTasks.add(taskName)
                    containerDependencies.add(taskName)
                }
            }

            String taskName = "assemble-container-"+containerAssemble.name
            project.task(taskName,dependsOn:containerDependencies,type:ContainerAssembleTask,group:"container assemble"){
                setAssemble(containerAssemble)
                setContainerAssemble(containerAssemble)
                targetDirectory=assembleTargetDir
                deploymentReport = report
            }
        }
    }

 
       public static void addClosures(Project project){
        project.ext.webapp = { String webappName ->
            def directory = new File(project.getBuildDir(),"assemble/webapp/"+webappName)
            return directory
        }
        
        project.ext.distributionDependency= { String distribution ->
            try{
                if(project.gaffer.lifecycleState==LifecycleState.INITIALIZING){
                    return []
                }else{
                    def remove=true
                    DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                    return resolver.resolveToFiles(distribution,remove)
                }
            }catch(Exception e){
                project.logger.error("Faild on resolve distributionDependency:"+distribution,e);
            }
        }


        project.ext.flatDependency= { String distribution ->
            try{
                if(project.gaffer.lifecycleState==LifecycleState.INITIALIZING){
                    return []
                }else{
                    def remove=false
                    DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                  
                    return resolver.resolveToFiles(distribution,remove)
                }
            }catch(Exception e){
                project.logger.error("Faild on resolve flatDependency:"+distribution,e);
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
