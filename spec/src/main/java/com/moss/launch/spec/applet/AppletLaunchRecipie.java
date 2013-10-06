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
package com.moss.launch.spec.applet;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.moss.launch.components.Component;
import com.moss.launch.spec.LaunchSpecValidationException;
import com.moss.launch.spec.impl.ValidationUtil;
import com.moss.launch.spec.launch.ClassName;
import com.moss.launch.spec.launch.VMSetupSpec;

@XmlAccessorType(XmlAccessType.FIELD)
public class AppletLaunchRecipie {
	private String name;

	@XmlElement(name="applet-class")
	private ClassName appletClass;
	
	@XmlElement(name="vm-setup")
	private VMSetupSpec vmSetup;
	
	@XmlElement(name="parameter")
	private List<AppletParameter> parameters = new LinkedList<AppletParameter>();
	
	@XmlElement(name="component")
	private List<Component> components = new LinkedList<Component>();


	public AppletLaunchRecipie() {
	}
	
	public AppletLaunchRecipie(String name, ClassName appletClass,
			List<AppletParameter> parameters, List<Component> components) {
		super();
		this.name = name;
		this.appletClass = appletClass;
		this.parameters = parameters;
		this.components = components;
	}
	
	public AppletLaunchRecipie(String name, ClassName appletClass) {
		super();
		this.name = name;
		this.appletClass = appletClass;
	}
	
	public void validate() throws LaunchSpecValidationException {
		for(Component c : components){
			ValidationUtil.validate(c);
		}
	}
	public void addParameter(String name, String value){
		parameters.add(new AppletParameter(name, value));
	}
	
	public ClassName appletClass() {
		return appletClass;
	}
	
	public List<Component> components() {
		return components;
	}
	
	public String name() {
		return name;
	}
	
	public List<AppletParameter> parameters() {
		return parameters;
	}
}
