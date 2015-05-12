package de.ethinking.gradle.gaffer.repository

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileTree

import java.nio.file.Files
import java.nio.file.Path


class DynamicDependencyResolver {

    Project project

    public DynamicDependencyResolver(Project project){
        this.project=project
    }

    public void resolveToDirectory(String dependencyNotation,File target){
        resolveToDirectory(dependencyNotation, target, false)
    }

    public void resolveToDirectory(String dependencyNotation,File target,boolean removeZipDir){

        Dependency  dependency = project.dependencies.create(dependencyNotation)
        Configuration configuration = project.configurations.detachedConfiguration(dependency)
        configuration.setTransitive(false)
        configuration.files.each { file ->
            project.copy{
                from (project.zipTree(file)) {
                    eachFile { details ->
                        if(removeZipDir){
                            details.path = details.path.substring(details.relativePath.segments[0].length())
                        }
                    }
                }
                into target
            }
        }
    }


    def resolveToFiles(String dependencyNotation,boolean removeZipDir){
      
        Dependency  dependency = project.dependencies.create(dependencyNotation)
        Configuration configuration = project.configurations.detachedConfiguration(dependency)
        configuration.setTransitive(false)

        if(removeZipDir){
            FileTree result = null
            configuration.files.each {file ->
                result = project.zipTree(file)
            }
            
            Path path = Files.createTempDirectory("ziptemp")
            File f = path.toFile()
            f.deleteOnExit()
            project.copy{
                from  result
                into f
            }
            File rootZipDirectory = f.listFiles()[0]
            return rootZipDirectory
        }

        return configuration
    }
}

