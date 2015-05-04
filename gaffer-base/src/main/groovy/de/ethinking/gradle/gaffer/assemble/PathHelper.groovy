package de.ethinking.gradle.gaffer.assemble

import java.nio.file.Path
import java.nio.file.Paths

class PathHelper {
	
	
	public static  String removeRoot(String file){
		Path path = Paths.get(file)
		Path root = path.getRoot()
		if(root){
			return file.substring(root.toString().length())
		}
		return file
	}
}
