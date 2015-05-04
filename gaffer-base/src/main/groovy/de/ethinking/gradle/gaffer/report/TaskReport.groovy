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
package de.ethinking.gradle.gaffer.report

import de.ethinking.gradle.gaffer.assemble.BaseAssemble.Type
import org.gradle.api.file.FileCopyDetails

class TaskReport {

	String name
	long bytes=0
	int files=0
	String assemble
	Type type
	
    List<CopyReport> copyReports = []
	
	
	def addCopyReport(CopyReport copyReport){
		copyReports.add(copyReport)
		bytes +=copyReport.bytes
		files +=copyReport.files.size()
	}
}
