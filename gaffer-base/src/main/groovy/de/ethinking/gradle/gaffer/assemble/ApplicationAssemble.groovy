package de.ethinking.gradle.gaffer.assemble

import java.io.File;

import de.ethinking.gradle.gaffer.assemble.BaseAssemble.Type
import de.ethinking.gradle.gaffer.report.DeploymentReport
import de.ethinking.gradle.gaffer.LifecycleState
import de.ethinking.gradle.repository.DynamicDependencyResolver

import org.gradle.api.Project
import org.gradle.api.file.CopySpec

import java.nio.file.Paths
import java.nio.file.Path
import java.util.Set

class ApplicationAssemble extends BaseAssemble{

	public ApplicationAssemble(Project project){
		super(project,Type.APPLICATION)
	}

	public ApplicationAssemble(Project project,Type type){
		super(project,type)
	}
	
	
	def createTaskDependencies(){
		Set<String> dependencies = new HashSet<String>()
		copyExtensions.each{ CopyFromSource copy ->
			dependencies.addAll(copy.createTaskDependencies())
		}
		return dependencies
	}
	

}
