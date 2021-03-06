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
package com.moss.launch.spec;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.moss.launch.components.Component;
import com.moss.launch.spec.applet.AppletLaunchRecipie;
import com.moss.launch.spec.applet.AppletParameter;
import com.moss.launch.spec.applet.AppletProfile;
import com.moss.launch.spec.launch.ClassName;

/**
 * At this point, an JavaAppletSpec is just like an {@link JavaAppSpec}, except:
 * a) it doesn't have bundle specs
 * b) it has parameters instead of arguments
 * 
 */
@XmlRootElement(name="java-applet-spec")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("serial")
public class JavaAppletSpec extends AppletLaunchRecipie implements Serializable {
	public static final String FILE_EXTENSION = "applet-spec.xml";
	public static final String DOT_FILE_EXTENSION = "." + FILE_EXTENSION;
	
	public static final JavaAppletSpec read(File path) throws IOException {
		try {
			return (JavaAppletSpec) JAXBContext.newInstance(JavaAppletSpec.class).createUnmarshaller().unmarshal(path);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
	@XmlElement(name="profile")
	private List<AppletProfile> profiles = new LinkedList<AppletProfile>();
	
	
	
	public JavaAppletSpec() {
		super();
	}

	public JavaAppletSpec(String name, ClassName appletClass,
			List<AppletParameter> parameters, List<Component> components) {
		super(name, appletClass, parameters, components);
	}

	public void validate() throws LaunchSpecValidationException {
		super.validate();
		for(AppletProfile profile : profiles){
			profile.validate();
		}
	}

	public JavaAppletSpec(String name, ClassName appletClass) {
		super(name, appletClass);
	}


	public List<AppletProfile> profiles() {
		return profiles;
	}
}