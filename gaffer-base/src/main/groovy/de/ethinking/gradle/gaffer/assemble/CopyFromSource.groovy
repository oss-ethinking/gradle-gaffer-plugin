package de.ethinking.gradle.gaffer.assemble

import de.ethinking.gradle.gaffer.report.CopyReport
import de.ethinking.gradle.gaffer.report.TaskReport
import de.ethinking.gradle.gaffer.LifecycleState
import groovy.lang.Closure;

import java.io.FilterReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.gradle.api.Action;
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopyProcessingSpec
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Copy

class CopyFromSource {

	Closure closure
	Project project

	Set<String> webappDependencies = new HashSet<String>()
	Set<String> projectDependencies = new HashSet<String>()
	Set<String> taskDependencies = new HashSet<String>()

	LifecycleState lifecycleState = LifecycleState.INITIALIZING

	public CopyFromSource(Closure closure,Project project){
		this.project=project
		this.closure=closure
	}


	private def eval(){
		try{
			DryCopySpec dry = new DryCopySpec(this)
			closure.delegate=dry
			closure.call()
		}catch(Exception e){
              project.logger.error("Could not evaluate assemble dependencies",e)
		}
	}


	def doCopy(File targetDir,CopyReport report){
        
        closure.delegate=this
		CopySpec copySpec = project.copySpec(closure)
		if(!copySpec.getDuplicatesStrategy()){
			copySpec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE)
		}
		project.copy{
			into targetDir
			with copySpec
			eachFile{ FileCopyDetails detail ->
				report.report(detail)
			}
		}
	}


	class DryCopySpec implements CopySpec{

		def CopyFromSource parent

		public DryCopySpec(CopyFromSource parent){
			this.parent = parent
		}


		@Override
		public Integer getDirMode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Integer getFileMode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopyProcessingSpec setDirMode(Integer arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopyProcessingSpec setFileMode(Integer arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getExcludes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getIncludes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec eachFile(Action<? super FileCopyDetails> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec eachFile(Closure arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec exclude(String... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec exclude(Iterable<String> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec exclude(Spec<FileTreeElement> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec exclude(Closure arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec expand(Map<String, ?> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec filesMatching(String arg0, Action<? super FileCopyDetails> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec filesNotMatching(String arg0, Action<? super FileCopyDetails> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec filter(Class<? extends FilterReader> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec filter(Closure arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec filter(Map<String, ?> arg0, Class<? extends FilterReader> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec from(Object... args) {
			if(args !=null){
				for(Object obj:args){
					if(obj instanceof Task){
						parent.taskDependencies.add(obj.getPath())
					}
				}
			}
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec from(Object arg0, Closure arg1) {
			// TODO Auto-generated method stub

			return null;
		}

		@Override
		public DuplicatesStrategy getDuplicatesStrategy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean getIncludeEmptyDirs() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public CopySpec include(String... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec include(Iterable<String> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec include(Spec<FileTreeElement> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec include(Closure arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec into(Object target) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec into(Object target, Closure innerClosure) {
			innerClosure.delegate=this
		    innerClosure.call()
			return this;
		}

		@Override
		public boolean isCaseSensitive() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public CopySpec rename(Closure arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec rename(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopyProcessingSpec rename(Pattern arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setCaseSensitive(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDuplicatesStrategy(DuplicatesStrategy arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public CopySpec setExcludes(Iterable<String> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setIncludeEmptyDirs(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public CopySpec setIncludes(Iterable<String> arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CopySpec with(CopySpec... arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		def webapp(String webapp){
			if(lifecycleState==LifecycleState.INITIALIZING){
				parent.webappDependencies.add(webapp)
				return []
			}else{
				File webappPath = new File(parent.project.getBuildDir(),"assemble/webapp/"+webapp)
				return  webappPath
			}
		}

		def project(String projectPath){
			if(lifecycleState==LifecycleState.INITIALIZING){
				parent.projectDependencies.add(projectPath)
			}
			return parent.project.project(projectPath)
		}
	}

	def dependency(String dependency){
		if(lifecycleState==LifecycleState.INITIALIZING){
			return []
		}
	}

	def switchState(LifecycleState state){
		if(state == LifecycleState.CONFIGURED ){
			eval();
		}
		this.lifecycleState = state
	}

	def createTaskDependencies(){
		Set<String> dependencies = new HashSet<String>()
		webappDependencies.each {  String webapp ->
			dependencies.add("assemble-webapp-"+webapp)
		}

		projectDependencies.each{ String project ->
			dependencies.add(project+":build")
		}
		taskDependencies.each { String taskPath ->
			dependencies.add(taskPath)
		}
		return dependencies
	}
}
