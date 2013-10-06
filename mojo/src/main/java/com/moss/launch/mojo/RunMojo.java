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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.moss.launch.components.MavenCoordinatesHandle;
import com.moss.launch.spec.JavaAppSpec;
import com.moss.launch.tools.simplelauncher.SimpleLauncher;

/**
 * Runs using the specified launch-spec and the target dir + local maven repo.
 *
 * @goal run
 */
public class RunMojo extends AbstractSpecMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			List<LaunchFileInfo> launches = findLaunches();
			if(launches.size()==0){
				throw new MojoExecutionException("No launches defined.");
			}else if(launches.size()>1){
				throw new MojoExecutionException("There are mulitple launches: You must specify which launch to run.");
			}else{
				launch(launches.get(0).source);
			}
		} catch(MojoExecutionException e){
			throw e;
		} catch(MojoFailureException e){
			throw e;
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage(), e);
		}
	}

	protected void launch(File source) throws Exception {
		getLog().info("Launching " + source.getAbsolutePath());
		JavaAppSpec spec = helper().readFromFile(source);
		
		spec.components().addAll(getDependencies(false));
		
		LocalMavenRepoComponentResolver resolver = new LocalMavenRepoComponentResolver(artifactFactory, local, false);

		resolver.addSpecial(
				new MavenCoordinatesHandle(project.getGroupId(), project.getArtifactId(), project.getVersion()), 
				new File(project.getBuild().getOutputDirectory())
		);
		
		Process p = new SimpleLauncher().launch(spec, resolver);
		
		System.out.println("PROCESS START");

		// PIPE THE PROGRAM OUTPUT TO THE CONSOLE
		Dumper outDumper = new Dumper(p.getInputStream(), System.out);
		Dumper errDumper = new Dumper(p.getErrorStream(), System.err);
		
		// WAIT FOR EVERYTHING TO EXECUTE AND FLOW THROUGH
		int exitValue = p.waitFor();
		outDumper.join();
		errDumper.join();
		
		// REPORT RESULT
		System.out.println("PROCESS EXITED WITH " + exitValue);
	}

	private class Dumper extends Thread {
		private final InputStream in;
		private final OutputStream out;

		public Dumper(InputStream in, OutputStream out) {
			super();
			this.in = in;
			this.out = out;
			start();
		}
		@Override
		public void run() {
			try {
				byte[] b = new byte[1024*100];
				for(int x = in.read(b);x!=-1;x = in.read(b)){
					out.write(b, 0, x);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
