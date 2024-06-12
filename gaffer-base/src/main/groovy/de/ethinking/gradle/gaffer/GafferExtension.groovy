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
package de.ethinking.gradle.gaffer


import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.War
import org.gradle.tooling.BuildException
import de.ethinking.gradle.gaffer.assemble.ApplicationAssemble
import de.ethinking.gradle.gaffer.assemble.ContainerAssemble
import de.ethinking.gradle.gaffer.assemble.ProfileAssemble
import de.ethinking.gradle.gaffer.assemble.WebappAssemble
import de.ethinking.gradle.gaffer.report.DeploymentReport
import de.ethinking.gradle.gaffer.tasks.ApplicationAssembleTask
import de.ethinking.gradle.gaffer.tasks.ContainerAssembleTask
import de.ethinking.gradle.gaffer.tasks.WebappAssembleTask

class GafferExtension {

    static Logger LOG = Logging.getLogger(GafferExtension.class)


    List<ApplicationAssemble> applicationAssembles = new ArrayList()
    List<WebappAssemble> webappAssembles = new ArrayList()
    List<ProfileAssemble> profiles = new ArrayList()
    List<ContainerAssemble> containers = new ArrayList()


    DeploymentReport report = new DeploymentReport()
    Project project


    LifecycleState lifecycleState = LifecycleState.INITIALIZING

    public GafferExtension(Project project) {
        this.project = project
    }


    def webapp(Closure closure) {
        WebappAssemble webappAssemble = new WebappAssemble(project)
        addWebapp(closure, webappAssemble)
    }

    def webapp(String name, Closure closure) {
        WebappAssemble webappAssemble = new WebappAssemble(project)
        webappAssemble.name = name
        addWebapp(closure, webappAssemble)
    }

    private addWebapp(Closure closure, WebappAssemble assemble) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = assemble
        closure.call()
        webappAssembles << assemble


        File assembleTargetDir = new File(project.getBuildDir(), "assemble/webapp/" + assemble.name)
        //create assemble task

        Set<String> webappDependencies = new HashSet<String>()

        String cleanWebappTask = "cleanWebapp-" + assemble.name
        project.tasks.register(cleanWebappTask, Delete) {
            delete assembleTargetDir
        }
        webappDependencies.add(cleanWebappTask)


        String webappAssembleTaskName = "assemble-webapp-" + assemble.name

        webappDependencies.addAll(assemble.createTaskDependencies(project))
        project.tasks.register(webappAssembleTaskName, WebappAssembleTask) {
            assemble.switchState(LifecycleState.CONFIGURED)
            dependsOn webappDependencies
            group "webapp assemble"
            setAssemble(assemble)
            webappAssemble = assemble
            deploymentReport = report
            targetDirectory = assembleTargetDir
        }
        project.tasks.register("war-" + assemble.name, War) {
            dependsOn:
            webappAssembleTaskName
            from assembleTargetDir
            archiveFileName = assemble.name + ".war"
        }
    }

    def application(Closure closure) {
        ApplicationAssemble applicationAssemble = new ApplicationAssemble(project)
        addApplication(applicationAssemble, closure)
    }

    def application(String name, Closure closure) {
        ApplicationAssemble applicationAssemble = new ApplicationAssemble(project)
        applicationAssemble.name = name
        addApplication(applicationAssemble, closure)
    }


    private addApplication(ApplicationAssemble assemble, Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = assemble
        closure.call()
        applicationAssembles << assemble

        File assembleTargetDir = new File(project.getBuildDir(), "assemble/application/" + assemble.name)
        project.tasks.register("assemble-application-" + assemble.name, ApplicationAssembleTask) {
            setAssemble(assemble)
            assemble.switchState(LifecycleState.CONFIGURED)
            group "application assemble"
            applicationAssemble = assemble
            deploymentReport = report
            targetDirectory = assembleTargetDir
        }
    }

    def profile(Closure closure) {
        ProfileAssemble profile = new ProfileAssemble(project)
        addProfile(profile, closure)
    }

    def profile(String name, Closure closure) {
        ProfileAssemble profile = new ProfileAssemble(project)
        profile.name = name
        addProfile(profile, closure)
    }

    private addProfile(ProfileAssemble profile, Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = profile
        closure.call()
        profiles << profile
    }

    def container(Closure closure) {
        ContainerAssemble container = new ContainerAssemble(project)
        addContainer(container, closure)
    }


    def container(String name, Closure closure) {
        ContainerAssemble container = new ContainerAssemble(project)
        container.name = name
        addContainer(container, closure)
    }

    private addContainer(ContainerAssemble containerAssemble, Closure closure) {
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = containerAssemble
        closure.call()
        containers << containerAssemble

        Set<String> containerDependencies = new HashSet<String>()
        Set<String> usedApplications = new HashSet<String>()
        File assembleTargetDir = new File(project.getBuildDir(), "assemble/container/" + containerAssemble.name)

        String cleanContainerTask = "cleanContainer-"+containerAssemble.name
        project.tasks.register(cleanContainerTask,Delete){ delete assembleTargetDir }
        containerDependencies.add(cleanContainerTask)

        String taskName = "assemble-container-" + containerAssemble.name
        // Register the task and get the task provider
        TaskProvider<ContainerAssembleTask> containerTaskProvider = project.tasks.register(taskName, ContainerAssembleTask)
        // Configure the task with provider method
        containerTaskProvider.configure { containerTask ->
            List<ApplicationAssemble> applicationAssembles = []
            List<ApplicationAssemble> profileAssembles = []

            containerAssemble.profiles.each { String profile ->
                ProfileAssemble containerProfileAssemble = project.gaffer.findProfileByName(profile)
                if (containerProfileAssemble) {
                    containerDependencies.addAll(containerProfileAssemble.createTaskDependencies())
                    // assemble all applications into container assemble directory
                    containerProfileAssemble.applicationAssembles.each { ApplicationAssemble profileApplicationAssemble ->
                        profileAssembles.add(profileApplicationAssemble)

                        ApplicationAssemble baseApplicationAssemble = project.gaffer.findApplicationByName(profileApplicationAssemble.name)
                        if (baseApplicationAssemble) {
                            if (!usedApplications.contains(profileApplicationAssemble.name)) {
                                usedApplications.add(profileApplicationAssemble.name)
                                applicationAssembles.add(baseApplicationAssemble)
                                containerDependencies.addAll(baseApplicationAssemble.createTaskDependencies())
                            }
                            if (!profileApplicationAssemble.basePath) {
                                profileApplicationAssemble.basePath = baseApplicationAssemble.basePath
                            }
                        } else {
                            throw new BuildException("Application: " + profileApplicationAssemble.name + " referenced in profile: " + profile + " but not defined.", new Exception())
                        }
                    }
                } else {
                    throw new BuildException("Profile: " + profile + " referenced in container: " + containerAssemble.name + " but not defined.", new Exception())
                }
            }

            containerAssemble.applications.each { String applicationName ->
                if (!usedApplications.contains(applicationName)) {
                    usedApplications.add(applicationName)
                    ApplicationAssemble applicationAssemble = project.gaffer.findApplicationByName(applicationName)
                    if (applicationAssemble) {
                        applicationAssembles.add(applicationAssemble)
                        containerDependencies.addAll(applicationAssemble.createTaskDependencies())
                    } else {
                        throw new BuildException("Application: " + applicationName + " referenced in container: " + containerAssemble.name + " but not defined.", new Exception())
                    }
                }
            }

            containerAssemble.switchState(LifecycleState.CONFIGURED)
            containerTask.group = "container assemble"
            containerTask.dependsOn(containerDependencies)
            containerTask.setAssemble(containerAssemble)
            containerTask.setContainerAssemble(containerAssemble)
            containerTask.targetDirectory = assembleTargetDir
            containerTask.deploymentReport = report
            containerTask.applicationAssembles.addAll(applicationAssembles)
            containerTask.profileApplicationAssembles.addAll(profileAssembles)
        }
    }


    def ApplicationAssemble findApplicationByName(String name) {
        for (ApplicationAssemble assemble : applicationAssembles) {
            if (assemble.name.equals(name)) {
                return assemble
            }
        }
    }


    def WebappAssemble findWebappByName(String name) {
        for (WebappAssemble webappAssemble : webappAssembles) {
            if (webappAssemble.name.equals(name)) {
                return webappAssemble
            }
        }
    }

    def ProfileAssemble findProfileByName(String name) {
        for (ProfileAssemble profile : profiles) {
            if (profile.name.equals(name)) {
                return profile
            }
        }
    }

    def ContainerAssemble findContainerByName(String name) {
        for (ContainerAssemble container : containers) {
            if (container.name.equals(name)) {
                return container
            }
        }
    }


    def switchState(LifecycleState state) {

        applicationAssembles.each { ApplicationAssemble applicationAssemble ->
            applicationAssemble.switchState(state)
        }
        webappAssembles.each { WebappAssemble webappAssemble ->
            webappAssemble.switchState(state)
        }
        profiles.each { ProfileAssemble profileAssemble ->
            profileAssemble.switchState(state)
        }
        containers.each { ContainerAssemble containerAssemble ->
            containerAssemble.switchState(state)
        }
        this.lifecycleState = state
    }
}
