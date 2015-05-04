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

import de.ethinking.gradle.assemble.ApplicationAssemble
import de.ethinking.gradle.assemble.ContainerAssemble
import de.ethinking.gradle.assemble.ProfileAssemble
import de.ethinking.gradle.assemble.WebappAssemble

import org.gradle.api.Project

class GafferExtension {

	List<ApplicationAssemble> applicationAssembles = []
	List<WebappAssemble> webappAssembles = []
	List<ProfileAssemble> profiles = []
	List<ContainerAssemble> containers = []
	
    Project project
	
	LifecycleState lifecycleState = LifecycleState.INITIALIZING 
	
	public GafferExtension(Project project){
		this.project = project
	}
	
	
	def webapp(Closure closure){
		WebappAssemble webappAssemble = new WebappAssemble(project)
		addWebapp(closure, webappAssemble)
	}
	
	def webapp(String name,Closure closure){
		WebappAssemble webappAssemble = new WebappAssemble(project)
		webappAssemble.name = name
		addWebapp(closure, webappAssemble)
	}

	private addWebapp(Closure closure, WebappAssemble webappAssemble) {
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate=webappAssemble
		webappAssembles << webappAssemble
		closure.call()
	}

	def application(Closure closure){
		ApplicationAssemble  applicationAssemble = new ApplicationAssemble(project)
		addApplication(applicationAssemble,closure)
	}
	
	def application(String name,Closure closure){
		ApplicationAssemble  applicationAssemble = new ApplicationAssemble(project)
		applicationAssemble.name = name
	    addApplication(applicationAssemble,closure) 
	}
	
	
	private addApplication(ApplicationAssemble applicationAssemble,Closure closure){
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = applicationAssemble
		applicationAssembles << applicationAssemble
		closure.call()
	}
	
	def profile(Closure closure){
		ProfileAssemble profile = new ProfileAssemble(project)
		addProfile(profile,closure)

	}
	def profile(String name,Closure closure){
		ProfileAssemble profile = new ProfileAssemble(project)
		profile.name=name
		addProfile(profile,closure)

	}
	
	private addProfile(ProfileAssemble profile,Closure closure){
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate=profile
		profiles << profile
		closure.call()
	}
	
	def container(Closure closure){
		ContainerAssemble container = new ContainerAssemble(project)
		addContainer(container,closure) 
	}
	
	
	def container(String name,Closure closure){
		ContainerAssemble container = new ContainerAssemble(project)
		container.name=name
		addContainer(container,closure)
	}
	
	private addContainer(ContainerAssemble container,Closure closure){
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = container
		containers << container
		closure.call()
	}
	
	
	
	def ApplicationAssemble findApplicationByName(String name){
		for(ApplicationAssemble assemble:applicationAssembles){
			if(assemble.name.equals(name)){
				return assemble
			}
		}
	}
	
	
	def WebappAssemble findWebappByName(String name){
		for(WebappAssemble webappAssemble:webappAssembles){
			if(webappAssemble.name.equals(name)){
				return webappAssemble
			}
		}
	}
	
	def ProfileAssemble findProfileByName(String name){
		for(ProfileAssemble profile:profiles){
			if(profile.name.equals(name)){
				return profile
			}
		}
	}
	
	def ContainerAssemble findContainerByName(String name){
		for(ContainerAssemble container:containers){
			if(container.name.equals(name)){
				return container
			}
		}
	}
	
	
	def switchState(LifecycleState state){
		
		applicationAssembles.each { ApplicationAssemble applicationAssemble ->
			applicationAssemble.switchState(state)
		}
		webappAssembles.each{ WebappAssemble webappAssemble -> 
			webappAssemble.switchState(state)
		}
		profiles.each { ProfileAssemble profileAssemble ->
			profileAssemble.switchState(state)
		}
		containers.each { ContainerAssemble containerAssemble ->
			containerAssemble.switchState(state)
		}
		this.lifecycleState=state
		
	}
}
