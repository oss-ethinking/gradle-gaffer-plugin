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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction


import de.ethinking.gradle.gaffer.assemble.ApplicationAssemble
import de.ethinking.gradle.gaffer.assemble.ContainerAssemble
import de.ethinking.gradle.gaffer.assemble.ProfileAssemble

class ContainerAssembleTask extends BaseAssembleTask{

    @Internal
    ContainerAssemble containerAssemble
    @Internal
    List<ApplicationAssemble> applicationAssembles = []
    @Internal
    List<ApplicationAssemble> profileApplicationAssembles = []

    @TaskAction
    def assemble(){
        targetDirectory.mkdirs()
        for(ApplicationAssemble applicationAssemble:applicationAssembles){
            assembleApplication(applicationAssemble)
        }
        for(ApplicationAssemble profileApplicationAssemble:profileApplicationAssembles){
            assembleApplication(profileApplicationAssemble)
        }
        report()
    }

    def setContainerAssemble(ContainerAssemble containerAssemble){
        this.containerAssemble = containerAssemble
    }

    
    @Input
    def getParams(){
        return containerAssemble.params
    }

    def setReportTarget(String target){
        containerAssemble.setReportTarget(target)
    }
}
