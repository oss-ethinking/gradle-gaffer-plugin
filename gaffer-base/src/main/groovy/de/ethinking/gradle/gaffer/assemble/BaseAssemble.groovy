package de.ethinking.gradle.gaffer.assemble

import org.gradle.api.Project

import de.ethinking.gradle.gaffer.LifecycleState

class BaseAssemble {

    enum Type {
        APPLICATION,APPLICATION_PROFILE,CONTAINER,PROFILE,WEBAPP
    }
    Type type


    def Project project
    def String name
    def List<CopyFromSource> copyExtensions  = []
    def LifecycleState lifecycleState = LifecycleState.INITIALIZING
    def Map params = new HashMap<String,Object>()
    def String basePath
    def Boolean reportEnabled=false
    def String reportTarget
    def String reportFormat="webapp"


    BaseAssemble(Project project){
        this.project=project
    }
    BaseAssemble(Project project,Type type){
        this.project=project
        this.type=type
    }



    def copy(Closure closure){
        CopyFromSource copy = new CopyFromSource(closure,project)
        //copy.eval()
        copyExtensions << copy

    }

    def name(String name){
        this.name = name
    }

    def switchState(LifecycleState state){
        copyExtensions.each{ CopyFromSource copy ->
            copy.switchState(state)
        }
        this.lifecycleState = state
    }

    def basePath(String path){
        this.basePath = path
    }

    def getReportTarget(){
        if(reportTarget!=null){
            return reportTarget;
        }else{
            String path="report/"
            if(basePath != null){
                path = basePath +"/" +path
            }
        }
    }

    def setReportTarget(String target){
        this.reportTarget=target
        this.reportEnabled=true
    }
}
