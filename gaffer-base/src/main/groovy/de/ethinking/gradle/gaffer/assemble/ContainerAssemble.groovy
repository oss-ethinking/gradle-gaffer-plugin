package de.ethinking.gradle.gaffer.assemble


import org.gradle.api.Project
import de.ethinking.gradle.gaffer.assemble.BaseAssemble.Type

class ContainerAssemble extends BaseAssemble{

    def buildfile
    List<String> profiles = new ArrayList<String>()
    Set<String> applications = new HashSet<String>()


    public ContainerAssemble(Project project){
        super(project)
        this.type = Type.CONTAINER
    }


    def profiles(String ...args){
        for(String profile:args){
            if(!profiles.contains(profile)){
                profiles.add(profile)
            }
        }
    }

    def applications(String ...args){
        applications.addAll(args)
    }
}
