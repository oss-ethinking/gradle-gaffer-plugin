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

import de.ethinking.gradle.gaffer.assemble.ApplicationAssemble;
import de.ethinking.gradle.gaffer.assemble.ProfileAssemble
import org.gradle.api.tasks.TaskAction

class ProfileApplicationAssembleTask extends BaseAssembleTask {
	
	ApplicationAssemble applicationAssemble
	ProfileAssemble profileAssemble
	
	@TaskAction
	def assemble(){
		targetDirectory.mkdirs();
		assembleApplication(applicationAssemble)
		report()
	}

}
