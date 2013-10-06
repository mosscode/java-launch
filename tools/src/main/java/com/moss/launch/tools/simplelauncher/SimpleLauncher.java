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
package com.moss.launch.tools.simplelauncher;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.launch.components.Component;
import com.moss.launch.spec.JavaAppSpec;

public class SimpleLauncher {
	public static void main(String[] args) {
		File launchFileLocation = new File(args[0]);
		
	}
	private final Log log = LogFactory.getLog(getClass());
	
	public Process launch(JavaAppSpec spec, ComponentResolver repo) throws IOException {
		
		LaunchCommand c = new LaunchCommand();
		c.mainClassName = spec.mainClass().toString();
		c.javaCommand = "java";
		
		for(Component next : spec.components()){
			c.classpath.add(repo.locate(next).getAbsolutePath());
		}
		
		System.out.println("Launching " + c);
		return c.launch();
	}
	
	static class LaunchCommand {
		private String mainClassName;
		private String javaCommand;
		private List<String> classpath = new LinkedList<String>();
		
		public Process launch() throws IOException {
			return Runtime.getRuntime().exec(parts(), new String[]{});
		}
		
		
		@Override
		public String toString() {
			StringBuffer text = new StringBuffer();
			for(String next : parts()){
				text.append(next);
				text.append(' ');
			}
			return text.toString();
		}
		
		private String[] parts(){
			return new String[]{
					javaCommand,
					"-cp",
					printClassPath(),
					mainClassName
			};
		}
		
		private String printClassPath(){
			StringBuilder text = new StringBuilder();
			
			if(classpath.size()>0){
				text.append(classpath.get(0));
				for(int x=1;x<classpath.size();x++){
					String next = classpath.get(x);
					text.append(":");
					text.append(next);
				}
			}
			return text.toString();
		}
	}
}
