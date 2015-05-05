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
package de.ethinking.gradle.gaffer.tasks


import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import de.ethinking.gradle.gaffer.assemble.ApplicationAssemble

import de.ethinking.gradle.gaffer.assemble.BaseAssemble
import de.ethinking.gradle.gaffer.assemble.CopyFromSource
import de.ethinking.gradle.gaffer.assemble.WebappAssemble
import de.ethinking.gradle.gaffer.assemble.WebappAssemble.WebappDependency
import de.ethinking.gradle.gaffer.report.CopyReport
import de.ethinking.gradle.gaffer.report.DeploymentReport
import de.ethinking.gradle.gaffer.report.TaskReport
import de.ethinking.gradle.repository.DynamicDependencyResolver

import org.codehaus.groovy.ast.ClassNode
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException
import groovy.json.*


class BaseAssembleTask extends DefaultTask {

	def File targetDirectory
	def DeploymentReport deploymentReport
	def BaseAssemble assemble


	def applyCopy(List<CopyFromSource> copyExtensions){

		File toDirectory = targetDirectory
		if(assemble.basePath){
			toDirectory = new File(toDirectory,assemble.basePath)
		}
		TaskReport taskReport = new TaskReport()
		taskReport.name  = this.getName()
		taskReport.assemble = assemble.name
		taskReport.type=assemble.type
		for(CopyFromSource source:copyExtensions){
			CopyReport copyReport = new CopyReport()
			copyReport.projects= source.getProjectDependencies()
			source.doCopy(toDirectory,copyReport)
			taskReport.addCopyReport(copyReport)
		}
		deploymentReport.addTaskReport(taskReport)
	}

	def assembleApplication(ApplicationAssemble assemble){
		applyCopy(assemble.copyExtensions)
	}

	def setTargetDirectory(File targetDirectory){
		this.targetDirectory=targetDirectory
	}

	def getTargetDirectory(){
		return this.targetDirectory
	}

	def setAssemble(BaseAssemble assemble){
		this.assemble = assemble
	}


	def assembleWebapp(WebappAssemble assemble){
		applyCopy(assemble.copyExtensions)
	}


	def report(){
		if(assemble.reportEnabled){
			deploymentReport.created=System.currentTimeMillis()
			deploymentReport.params = assemble.params

			String jsonReport = JsonOutput.toJson(deploymentReport)
			File reportBase = new File(targetDirectory,assemble.getReportTarget())

			if(assemble.reportFormat.equals("webapp")){
				createReportWebapp(jsonReport, reportBase)
			}
		}
	}


	def createReportWebapp(String jsonReport,File reportBase){



		File reportFile = new File(reportBase,"report.js")
		reportBase.mkdirs()
		if(reportFile.exists()){
			reportFile.delete()
		}
		reportFile.createNewFile()
		reportFile << "var report="+JsonOutput.prettyPrint(jsonReport)+";\n"

		File reportData = new File(reportBase,"report.json")
		if(reportData.exists()){
			reportData.delete()
		}
		reportData.createNewFile()
		reportData << jsonReport

		File webinf = new File(reportBase,"WEB-INF")
		webinf.mkdirs()
		explodeReportWebapp("/report/report.zip", reportBase)
		
//		store("index.html",reportBase)
//		store("app.js",reportBase)
//		store("css/main.css",reportBase)
//		store("lib/css/foundation.min.css",reportBase)
//		store("lib/css/normalize.css",reportBase)
//		store("lib/js/foundation.min.js",reportBase)
//		store("lib/js/vendor/jquery.js",reportBase)
//		store("lib/js/foundation/foundation.js",reportBase)
//		store("lib/js/foundation/foundation.topbar.js",reportBase)
	}


	def store(String resource,File base){
		File file = new File(base,resource)
		if(file.exists()){
			file.delete()
		}else{
			file.getParentFile().mkdirs()
		}

		storeResource("/report/"+resource,file)
	}

	def storeResource(String source,File target){
		def inputStream = this.class.getResourceAsStream(source)
		String content = inputStream.text
		target << content
	}


	def explodeReportWebapp(String resource,File base){

		ZipInputStream zipStream= null
		OutputStream out = null

		try {
			zipStream=new ZipInputStream(this.class.getResourceAsStream(resource));
			// Get the first entry
			ZipEntry entry = null;
			while ((entry = zipStream.getNextEntry()) != null) {
				String outFilename = entry.getName();

				if (entry.isDirectory()) {
					new File(base, outFilename).mkdirs();
				} else {
					out = new FileOutputStream(new File(base,outFilename));
					// Transfer bytes from the ZIP file to the output file
					byte[] buf = new byte[1024];
					int len;
					while ((len = zipStream.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
				}
			}
		} catch(Exception e){
            e.printStackTrace()
		} finally {
			// Close the stream
			if (zipStream != null) {
				zipStream.close();
			}
			if (out != null) {
				out.close();
			}

		}

	}
}