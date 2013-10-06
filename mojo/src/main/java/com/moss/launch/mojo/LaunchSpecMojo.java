/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of java-launch.
 *
 * java-launch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * java-launch is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with java-launch; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.launch.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.moss.launch.spec.JavaAppSpec;
import com.moss.launch.spec.JavaAppletSpec;

/**
 * Makes a launch spec from the pom plus some plugin-specific configuration.
 * The plugin-specific configuration info is stored in src/main/launches
 * 

<java-application-spec>

	<main-class>com.moss.test.Main</main-class>
	
	<std-vm-settings>
		<Xmx>256m</Xmx>
		<Xms>100m</Xms>
	</std-vm-settings>

</java-application-spec>

 * @goal generate
 * @phase package
 */
public class LaunchSpecMojo extends AbstractSpecMojo {
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			for(LaunchFileInfo next : findLaunches()){
				switch(next.type){
				case APP:
					handleApp(next.source, next.outputLocation);
					break;
				case APPLET:
					handleApplet(next.source, next.outputLocation);
					break;
				}
			}
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	protected void handleApplet(File next, File outputLocation) throws Exception {
		if(!outputLocation.getParentFile().exists() && !outputLocation.getParentFile().mkdirs()){
			throw new IOException("Could not create directory: " + outputLocation.getParentFile().getAbsolutePath());
		}
		getLog().info("Processing launch file: " + next.getAbsolutePath());
		
		
		// ATTACH THE ARTIFACT
		JavaAppletSpec spec = helper().readFromFile(next);
		{
			String name = spec.name();
			if(name==null || name.trim().length()==0){
				if(next.getName().equals(JavaAppletSpec.FILE_EXTENSION)){
					name = "";
				}else{
					name = next.getName().substring(0, next.getName().lastIndexOf(JavaAppletSpec.DOT_FILE_EXTENSION));
				}
			}
			
			getLog().info("Using name: " + name);
			Artifact artifact = artifactFactory.createArtifactWithClassifier(project.getGroupId(), project.getArtifactId(), project.getVersion(), JavaAppletSpec.FILE_EXTENSION, name);
			artifact.setFile(outputLocation);
			artifact.setResolved( true );
			project.addAttachedArtifact(artifact);
		}
		
		spec.components().addAll(getDependencies(true));

		getLog().info("Writing to " + outputLocation.getAbsolutePath());
		helper().writeToFile(helper().writeToXmlString(spec), outputLocation);
	}
	
	protected void handleApp(File next, File outputLocation) throws Exception {
		if(!outputLocation.getParentFile().exists() && !outputLocation.getParentFile().mkdirs()){
			throw new IOException("Could not create directory: " + outputLocation.getParentFile().getAbsolutePath());
		}
		getLog().info("Processing launch file: " + next.getAbsolutePath());
		
		
		// ATTACH THE ARTIFACT
		JavaAppSpec spec = helper().readFromFile(next);
		{
			String name = spec.name();
			if(name==null || name.trim().length()==0){
				if(next.getName().equals(JavaAppSpec.FILE_EXTENSION)){
					name = "";
				}else{
					name = next.getName().substring(0, next.getName().lastIndexOf(JavaAppSpec.DOT_FILE_EXTENSION));
				}
			}
			getLog().info("Using name: " + name);

			Artifact artifact = artifactFactory.createArtifactWithClassifier(project.getGroupId(), project.getArtifactId(), project.getVersion(), JavaAppSpec.FILE_EXTENSION, name);
			artifact.setFile(outputLocation);
			artifact.setResolved( true );
			project.addAttachedArtifact(artifact);
		}
		
		spec.components().addAll(getDependencies(true));

		getLog().info("Writing to " + outputLocation.getAbsolutePath());
		helper().writeToFile(helper().writeToXmlString(spec), outputLocation);
	};
}
