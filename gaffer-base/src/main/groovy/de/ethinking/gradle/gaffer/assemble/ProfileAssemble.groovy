package de.ethinking.gradle.gaffer.assemble

import java.util.List

import groovy.lang.Closure



import org.gradle.api.Project

import de.ethinking.gradle.gaffer.report.DeploymentReport
import de.ethinking.gradle.gaffer.LifecycleState

class ProfileAssemble extends BaseAssemble {
	
	List<ApplicationAssemble> applicationAssembles = []
	List<WebappAssemble> webappAssembles = []

	public ProfileAssemble(Project project){
		super(project)
		this.type = BaseAssemble.Type.PROFILE
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
		ApplicationAssemble  applicationAssemble = new ApplicationAssemble(project,BaseAssemble.Type.APPLICATION_PROFILE)
		addApplication(applicationAssemble,closure)
	}
	
	def application(String name,Closure closure){
		ApplicationAssemble  applicationAssemble = new ApplicationAssemble(project,BaseAssemble.Type.APPLICATION_PROFILE)
		applicationAssemble.name = name
	    addApplication(applicationAssemble,closure) 
	}
	
	def application(String name){
		ApplicationAssemble  applicationAssemble = new ApplicationAssemble(project,BaseAssemble.Type.APPLICATION_PROFILE)
		applicationAssemble.name = name
		applicationAssembles << applicationAssemble
	}
	
	
	private addApplication(ApplicationAssemble applicationAssemble,Closure closure){
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = applicationAssemble
		applicationAssembles << applicationAssemble
		closure.call()
	}

	def switchState(LifecycleState state){
		this.lifecycleState=state
		applicationAssembles.each { ApplicationAssemble applicationAssemble ->
			applicationAssemble.switchState(state)
		}
		webappAssembles.each { WebappAssemble webappAssemble ->
			webappAssemble.switchState(state)
		}
	}

	public Set<String>  createTaskDependencies(){
		Set<String> dependencies = new HashSet<String>()
		applicationAssembles.each{ ApplicationAssemble applicationAssemble ->
			dependencies.addAll(applicationAssemble.createTaskDependencies())
			//dependencies.add("assemble-application-"+applicationAssemble.name)
		}
		return dependencies
	}

}
