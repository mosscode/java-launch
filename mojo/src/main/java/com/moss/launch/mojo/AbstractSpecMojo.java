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
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.joda.time.Instant;

import com.moss.jaxbhelper.JAXBHelper;
import com.moss.launch.components.BuildTimestampComponentHandle;
import com.moss.launch.components.Component;
import com.moss.launch.components.ComponentHandle;
import com.moss.launch.components.ComponentType;
import com.moss.launch.components.MavenCoordinatesHandle;
import com.moss.launch.spec.JavaAppSpec;
import com.moss.launch.spec.JavaAppletSpec;
import com.moss.launch.tools.digests.DigestHandleMaker;
import com.moss.launch.tools.digests.Md5DigestHandleMaker;

public abstract class AbstractSpecMojo extends AbstractMojo {
	/*
	 * Injected maven-specific parameters.
	 */

	/** @component */
	protected ArtifactFactory artifactFactory;
	
	/** @component */
	protected ArtifactResolver resolver;
	
	/** @component */
	protected ArtifactMetadataSource metadata;
	
	/** @component */
	protected MavenProjectBuilder projectBuilder;
	
	/** @component */
	protected MavenProjectHelper projectHelper;
	
	/** @parameter expression="${project}" */
	protected MavenProject project;

	/**@parameter expression="${localRepository}" */
	protected ArtifactRepository local;
	
	/** @parameter expression="${project.remoteArtifactRepositories}" */
	protected List<ArtifactRepository> remote;
	
	private JAXBHelper helper;
	
	protected JAXBHelper helper() throws JAXBException {
		if(helper==null){
			helper = new JAXBHelper(JavaAppletSpec.class, JavaAppSpec.class);
		}
		return helper;
	}
	
	protected List<LaunchFileInfo> findLaunches() {
		final List<LaunchFileInfo> launches = new LinkedList<LaunchFileInfo>();
		
		final File launchSpecsPath = new File(project.getBasedir(), "src/main/launches");
		if(launchSpecsPath.exists() && launchSpecsPath.isDirectory()){
			final File[] files = launchSpecsPath.listFiles();
			
			if(files.length==0){
				getLog().warn("No launch spec patterns found.  For this plugin to work, you'll need to add them to " + launchSpecsPath.getAbsolutePath());
			}else{
				final File targetPath = new File(project.getBuild().getDirectory());
				final File launchesOutputPath = new File(targetPath, "launches");
				
				for(File next : launchSpecsPath.listFiles()){
					if(next.getName().endsWith(".xml")){
						getLog().info("Processing launch file: " + next.getAbsolutePath());
						
						final File outputLocation = new File(launchesOutputPath, next.getName());
						
						LaunchFileInfo.Type type;
						if(
								next.getName().equals(JavaAppSpec.FILE_EXTENSION) 
								|| 
								next.getName().endsWith(JavaAppSpec.DOT_FILE_EXTENSION)
							){
							type = LaunchFileInfo.Type.APP;
						}else 			if(
								next.getName().equals(JavaAppletSpec.FILE_EXTENSION) 
								|| 
								next.getName().endsWith(JavaAppletSpec.DOT_FILE_EXTENSION)
							){
							type = LaunchFileInfo.Type.APPLET;
						}else{
							throw new RuntimeException("Unknown file ending for file: " + next.getName());
						}
						
						launches.add(new LaunchFileInfo(next, outputLocation, type));
					}
				}
			}
		}else{
			getLog().warn("No launch spec patterns found.  For this plugin to work, you'll need to add them to " + launchSpecsPath.getAbsolutePath());
		}
		return launches;
	}
	
	static class LaunchFileInfo {
		enum Type {
			APP, APPLET
		}
		
		final File source;
		final File outputLocation;
		final Type type;
		
		public LaunchFileInfo(File source, File outputLocation, Type type) {
			super();
			this.source = source;
			this.outputLocation = outputLocation;
			this.type = type;
		}
		
	}
	protected List<Component> getDependencies(boolean generateProjectHashes) throws Exception {
		List<Component> dependencies = new LinkedList<Component>();
		Set<Artifact> artifacts;
		{
			Set<Artifact> a = project.createArtifacts(artifactFactory, null, null);

			ArtifactResolutionResult res = resolver.resolveTransitively(
				a, 
				project.getArtifact(), 
				remote, 
				local, 
				metadata
			);
			
			Set<Artifact> b = res.getArtifacts();
			
			if (b.isEmpty()) {
				artifacts = new HashSet<Artifact>();
			}
			else {
				artifacts = (Set<Artifact>)b;
			}
		}
		
		for(Artifact a : artifacts){
			dependencies.add(translate(a));
		}

		// ADD 'THIS' MODULE
		{
//			MavenCoordinatesHandle handle = new MavenCoordinatesHandle(project.getGroupId(), project.getArtifactId(), project.getVersion());
//			spec.add(new Component(ComponentType.JAR, handle));
			
			
			List<ComponentHandle> handles = new LinkedList<ComponentHandle>();
			
			handles.add(new MavenCoordinatesHandle(project.getGroupId(), project.getArtifactId(), project.getVersion()));
			
			if(generateProjectHashes){
				Build b = project.getBuild();
				
				File location = new File(b.getDirectory(), b.getFinalName() + ".jar");
				if(!location.exists()){
					throw new FileNotFoundException(location.getAbsolutePath());
				}
				
				
				
				
				long start = System.currentTimeMillis();
				handles.addAll(DigestHandleMaker.run(
						location, 
						new Md5DigestHandleMaker()
				));
				long finish = System.currentTimeMillis();
				System.out.println("MD5Sum took " + (finish-start) + " millis");
			}
			
			dependencies.add(new Component(ComponentType.JAR, handles));
			
//			for(Object o : project.getArtifacts()){
//				Artifact next = (Artifact) o;
//				System.out.println(next);
//			}
//			
//			spec.add(translate(project.getArtifact()));
		}
		
		return dependencies;
	}
	
	
	
	private Component translate(Artifact a){
		
		List<ComponentHandle> handles = new LinkedList<ComponentHandle>();
		
		MavenCoordinatesHandle handle = new MavenCoordinatesHandle(a.getGroupId(), a.getArtifactId(), a.getBaseVersion(), a.getType(), a.getClassifier());
		handles.add(handle);
		
//			System.out.println("       artifact: " + a.getArtifactId());
//
//			printVersionInfo("[Initial]", a);
			Artifact artifact = artifactFactory.createArtifactWithClassifier(handle.groupId(), handle.artifactId(), handle.version(), "jar", "");
//			printVersionInfo("[ Again]", artifact);
			
			final Instant lastModified;
			File location = new File(local.getBasedir(), local.pathOf(artifact));
			if(!location.exists()){
				throw new RuntimeException("File not found: " + location.getAbsolutePath());
			}else{
				lastModified = new Instant(location.lastModified());
//				System.out.println("location: " + location);
//				System.out.println(" last modified: " + lastModified);
			}
			handles.add(new BuildTimestampComponentHandle(lastModified));
			
			
		return new Component(ComponentType.JAR, handles);
	}
	
//	private void printVersionInfo(String prefix, Artifact a){
//		try {
//			System.out.println("        version " + prefix + ": " + a.getVersion());
//			System.out.println("   base version " + prefix + ": " + a.getBaseVersion());
//			System.out.println("selectd version " + prefix + ": " + a.getSelectedVersion());
//			System.out.println("   build number " + prefix + ": " + a.getSelectedVersion().getBuildNumber());
//			System.out.println("      qualifier " + prefix + ": " + a.getSelectedVersion().getQualifier());
//			for(Object next : a.getMetadataList()){
//				System.out.println("       metadata " + prefix + ": " + next.getClass().getName());
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//	}
}
