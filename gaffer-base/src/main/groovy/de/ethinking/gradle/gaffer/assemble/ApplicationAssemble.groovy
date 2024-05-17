package de.ethinking.gradle.gaffer.assemble


import de.ethinking.gradle.gaffer.assemble.BaseAssemble.Type
import org.gradle.api.Project

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
