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
package com.moss.launch.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.moss.launch.components.Component;
import com.moss.launch.spec.app.launch.Argument;
import com.moss.launch.tools.simplelauncher.ComponentResolver;

public class LaunchBuilder {
	private final char separator;
	
	private String mainClassName;
	private String javaCommand;
	private List<String> classpath = new LinkedList<String>();
	private List<Argument> arguments = new LinkedList<Argument>();
	
	public LaunchBuilder(char separator) {
		super();
		this.separator = separator;
	}

	public Process launch() throws IOException {
		return Runtime.getRuntime().exec(parts(), new String[]{});
	}
	
	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}
	
	public void setClassPath(final List<Component> components, final ComponentResolver resolver){
		for(Component next : components){
			classpath.add(resolver.locate(next).getAbsolutePath());
		}
	}
	
	public void addArgument(Argument arg){
		this.arguments.add(arg);
	}
	public void setJavaCommand(String javaCommand) {
		this.javaCommand = javaCommand;
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
	
	public String[] parts(){
		List<String> parts = new LinkedList<String>();
		
		parts.addAll(Arrays.asList(
				new String[]{
						javaCommand,
						"-cp",
						printClassPath(),
						mainClassName
				}
			));
		
		for(Argument next : arguments){
			parts.add(next.toString());
		}
		
		return parts.toArray(new String[parts.size()]);
	}
	
	private String printClassPath(){
		StringBuilder text = new StringBuilder();
		
		if(classpath.size()>0){
			text.append(classpath.get(0));
			for(int x=1;x<classpath.size();x++){
				String next = classpath.get(x);
				text.append(separator);
				text.append(next);
			}
		}
		return text.toString();
	}
}