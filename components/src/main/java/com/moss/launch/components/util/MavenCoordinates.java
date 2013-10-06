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
package com.moss.launch.components.util;

import javax.xml.bind.annotation.XmlElement;
/**
 * This represents a set of maven coordinates as defined by <a href="http://maven.apache.org/pom.html#Maven_Coordinates">the maven documentation.</a>.  That definition is quoted below:
 * 
 * <blockquote>
 *  <p>
 *  The POM defined above is the minimum that Maven 2 will allow. groupId:artifactId:version are all required fields
 *  (although, groupId and version need not be explicitly defined if they are inherited from a parent - more on 
 *  inheritance later). The three fields act much like an address and timestamp in one. This marks a specific place 
 *  in a repository, acting like a coordinate system for Maven projects.
 *  </p>
 *  
 *  <ul>
 *  	<li><b>groupId:</b> This is generally unique amongst an organization or a project. For example, all core Maven artifacts do (well, should) live under the groupId org.apache.maven. Group ID's do not necessarily use the dot notation, for example, the junit project. Note that the dot-notated groupId does not have to correspond to the package structure that the project contains. It is, however, a good practice to follow. When stored within a repository, the group acts much like the Java packaging structure does in an operating system. The dots are replaced by OS specific directory separators (such as '/' in Unix) which becomes a relative directory structure from the base repository. In the example given, the org.codehaus.mojo group lives within the directory $M2_REPO/org/codehaus/mojo.</li>
 *  	<li><b>artifactId:</b> The artifactId is generally the name that the project is known by. Although the groupId is important, people within the group will rarely mention the groupId in discussion (they are often all be the same ID, such as the Codehaus Mojo project groupId: org.codehaus.mojo). It, along with the groupId, create a key that separates this project from every other project in the world (at least, it should :) ). Along with the groupId, the artifactId fully defines the artifact's living quarters within the repository. In the case of the above project, my-project lives in $M2_REPO/org/codehaus/mojo/my-project.</li>
 *  	<li> 
 *  		<p><b>version:</b> This is the last piece of the naming puzzle. groupId:artifactId denote a single project but they cannot delineate which incarnation of that project we are talking about. Do we want the junit:junit of today (version 4), or of four years ago (version 2)? In short: code changes, those changes should be versioned, and this element keeps those versions in line. It is also used within an artifact's repository to separate versions from each other. my-project version 1.0 files live in the directory structure $M2_REPO/org/codehaus/mojo/my-project/1.0.</p><p>
 *  		<p>The three elements given above point to a specific version of a project letting Maven knows who we are dealing with, and when in its software lifecycle we want them.</p>
 *		</li>
 *		<li>
 *			<p><b>packaging:</b> Now that we have our address structure of groupId:artifactId:version, there is one more standard label to give us a really complete address. That is the project's artifact type. In our case, the example POM for org.codehaus.mojo:my-project:1.0 defined above will be packaged as a jar. We could make it into a war by declaring a different packaging:</p>
 *			<pre>
 *				&lt;project xmlns="http://maven.apache.org/POM/4.0.0"
 *				  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *				  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
 *				                      http://maven.apache.org/xsd/maven-4.0.0.xsd"&gt;
 *				  ...
 *				  &lt;packaging&gt;war&lt;/packaging&gt;
 *				  ...
 *				&lt;/project&gt;
 *			</pre>
 *			<p>When no packaging is declared, Maven assumes the artifact is the default: jar. The valid types are Plexus role-hints (read more on Plexus for a explanation of roles and role-hints) of the component role org.apache.maven.lifecycle.mapping.LifecycleMapping. The current core packaging values are: pom, jar, maven-plugin, ejb, war, ear, rar, par. These define the default list of goals which execute to each corresponding build lifecycle stage for a particular package structure.</p>
 *			<p>You will sometimes see Maven print out a project coordinate as groupId:artifactId:packaging:version.</p>
 *		</li>
 *  	<li>
 *  		<b>classifier:</b> You may occasionally find a fifth element on the coordinate, and that is the classifier. We will visit the classifier later, but for now it suffices to know that those kinds of projects are displayed as groupId:artifactId:packaging:classifier:version.
 *  	</li>
 *  </ul>
 * </blockquote>
 */
public class MavenCoordinates {

	public static MavenCoordinates parse(String text){
		return new MavenCoordinates(text);
	}
	
	@XmlElement(name="groupId")
	private String groupId;
	
	@XmlElement(name="artifactId")
	private String artifactId;
	
	@XmlElement
	private String version;
	
	@XmlElement
	private String packaging;
	
	@XmlElement
	private String classifier;
	
	MavenCoordinates() {}
	
	public MavenCoordinates(String text){
		String[] parts = text.split(":");
		
		final String group = parts[0];
		final String artifact = parts[1];
		
		final String version;
		final String packaging;
		final String classifier;
		
		if(parts.length==3){
			packaging = null;
			classifier = null;
			version = parts[2];
		}else if(parts.length==4){
			packaging = parts[2];
			classifier = null;
			version = parts[3];
		}else if(parts.length==5){
			packaging = parts[2];
			classifier = parts[3];
			version = parts[4];
		}else{
			throw new RuntimeException("Parsing exception - incorrect number of colon-delimited segments (" + parts.length + ").  Text: \"" + text + "\"");
		}
		
		this.groupId = group;
		this.artifactId = artifact;
		this.version = version;
		this.packaging = packaging;
		this.classifier = classifier;
	}
	
	public MavenCoordinates(String groupId, String artifactId, String version) {
		super();
		
		if(groupId == null ) throw new NullPointerException();
		this.groupId = groupId;
		
		if(artifactId == null ) throw new NullPointerException();
		this.artifactId = artifactId;
		
		if(version == null ) throw new NullPointerException();
		this.version = version;
		
		packaging = "jar";
		
	}
	
	public MavenCoordinates(String groupId, String artifactId, String version, String packaging, String classifier) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.packaging = packaging;
		this.classifier = classifier;
	}

	
	@Override
	public String toString() {
		StringBuilder text = new StringBuilder(groupId);
		text.append(':');
		text.append(artifactId);
		
		addPrefixedIfNotNull(packaging, text);
		addPrefixedIfNotNull(classifier, text);

		text.append(':');
		text.append(version);
		
		return text.toString();
	}
	
	private static void addPrefixedIfNotNull(String segment, StringBuilder text){
		if(segment==null){
			return;
		}else{
			text.append(':');
			text.append(segment);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof MavenCoordinates && ((MavenCoordinates)o).toString().equals(toString());
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String artifactId() {
		return artifactId;
	}
	public String groupId() {
		return groupId;
	}
	public String version() {
		return version;
	}
	
	public String packaging() {
		return packaging;
	}
	
	public String classifier() {
		return classifier;
	}
}
