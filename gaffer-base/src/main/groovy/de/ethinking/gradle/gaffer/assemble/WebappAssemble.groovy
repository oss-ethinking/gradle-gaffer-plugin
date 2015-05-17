package de.ethinking.gradle.gaffer.assemble

import de.ethinking.gradle.gaffer.LifecycleState
import de.ethinking.gradle.gaffer.repository.DynamicDependencyResolver;

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class WebappAssemble extends BaseAssemble{

    static Logger LOG = Logging.getLogger(WebappAssemble.class)



    List<Project> projectDependencies = []
    List<String> webapps = []
    Set<String> projects = new HashSet<String>()
    List<WebappDependency> dependencies = []


    public WebappAssemble(Project project){
        super(project)
        this.type = BaseAssemble.Type.WEBAPP
    }


    public Set<String> createTaskDependencies(Project project){

        Set<String> dependencies = new HashSet<String>()
        copyExtensions.each{ CopyFromSource copy ->
            dependencies.addAll(copy.createTaskDependencies())
        }
        projects.each{ String dependencyProject ->
            Project p = project.rootProject.findProject(dependencyProject)
            if(p){
                Task task = p.tasks.findByName("build")
                if(task){
                    dependencies.add(dependencyProject+":build")
                }else{
                  project.getLogger().info("Project "+dependencyProject+" has no build task.")
                }
            }
        }

        return dependencies
    }


    def name(String name){
        this.name = name
    }

    def warProject(String projectName){
        if(lifecycleState == LifecycleState.INITIALIZING){
            warProject(projectName,'runtimeWebapp')
        }
    }

    def warProject(String projectName,String configuration){

        WebappDependency dependency = new WebappDependency()
        dependency.project=projectName
        dependency.configuration=configuration
        dependencies.add(dependency)
        projects.add(projectName)

        def closure = {
            if(lifecycleState != LifecycleState.INITIALIZING){
                Project dependencyProject=project.searchProject(projectName)
                if(dependencyProject){
                    if(dependencyProject.plugins.hasPlugin("war")){
                        from dependencyProject.zipTree(dependencyProject.war.archivePath)
                        into "/"
                    }else{
                        if(dependencyProject.configurations.getAsMap().containsKey(configuration)){
                            into('/WEB-INF/lib'){
                                from dependencyProject.configurations.getByName(configuration)
                            }
                        }
                        if(dependencyProject.plugins.hasPlugin("java")){
                            into('/WEB-INF/lib'){ from dependencyProject.jar }
                        }
                        File webContent = dependencyProject.file("src/main/webapp")
                        if(LOG.isInfoEnabled()){
                            LOG.info("Copy WebContent from:"+webContent.getAbsolutePath()+" exists:"+webContent.exists())
                        }
                        if(webContent.exists()){
                            into('/'){ from webContent }
                        }
                    }
                }
            }
        }

        CopyFromSource copy = new CopyFromSource(closure,project)
        copyExtensions << copy
    }

    def project(Project project){
        projectDependencies.add(project)
    }

    def war(File warFile){
        WebappDependency dependency = new WebappDependency()
        dependency.warFile=warFile
        dependencies.add(dependency)
        def closure = {
            from zipTree(warFile)
            into "/"
        }
        CopyFromSource copy = new CopyFromSource(closure,project)
        copyExtensions << copy
    }

    def war(String dependencyNotation){

        if(dependencyNotation.indexOf(':')>-1){
            dependencyNotation = dependencyNotation
        }else{
            dependencyNotation = "de.ethinking.webapp:"+dependencyNotation+":"
        }
        if(!dependencyNotation.contains('@')){
            dependencyNotation +=":@war"
        }
        if(LOG.isInfoEnabled()){
            LOG.info("Resolve webapp:"+dependencyNotation)
        }

        WebappDependency dependency = new WebappDependency()
        dependency.warDependeny=dependencyNotation
        dependencies.add(dependency)
        Closure closure = {
            if(lifecycleState != LifecycleState.INITIALIZING){
                DynamicDependencyResolver resolver = new DynamicDependencyResolver(project)
                from resolver.resolveToFiles(dependencyNotation,false)
                into "/"
            }
        }
        CopyFromSource copy = new CopyFromSource(closure,project)
        copyExtensions << copy
    }


    public Project findProjectByName(String name,Project project){
        Project rootProject = findRootProject(project)

        if(name.contains(":")){
            return rootProject.findProject(name)
        }else{
            for(Project subproject:rootProject.allprojects){
                if(subproject.getName().equals(name)){
                    return subproject
                }
            }
        }

        println "No project found for:"+name
        return null
    }


    protected Project findRootProject(Project project){
        Project parent = project.getParent()
        if(parent != null){
            return findRootProject(parent)
        }
        return project
    }



    public class WebappDependency{
        String configuration = 'runtimeWebapp'
        String project
        String warDependeny
        File warFile
    }
}
