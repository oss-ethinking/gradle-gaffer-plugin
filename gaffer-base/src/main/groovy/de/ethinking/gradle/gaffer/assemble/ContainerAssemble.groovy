package de.ethinking.gradle.gaffer.assemble

import java.util.Map

import org.gradle.api.Project
import de.ethinking.gradle.gaffer.assemble.BaseAssemble.Type

class ContainerAssemble extends BaseAssemble{

	def buildfile
	Set<String> profiles = new HashSet<String>()
	Set<String> applications = new HashSet<String>()

	
	public ContainerAssemble(Project project){
        super(project)
		this.type = Type.CONTAINER
	}
	
	
	def profiles(String ...args){
		profiles.addAll(args)
	}
	
	def applications(String ...args){
		applications.addAll(args)
	}
}