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

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import de.ethinking.gradle.gaffer.assemble.WebappAssemble
import de.ethinking.gradle.gaffer.report.DeploymentReport


class WebappAssembleTask extends BaseAssembleTask{

	static Logger LOG = Logging.getLogger(WebappAssembleTask.class)

	WebappAssemble webappAssemble


	@TaskAction
	def assemble(){
        project.delete(targetDirectory)
		targetDirectory.mkdirs()
		assembleWebapp(webappAssemble)
		report()
	}

}
