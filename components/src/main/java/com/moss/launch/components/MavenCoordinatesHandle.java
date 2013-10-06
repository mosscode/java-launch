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
package com.moss.launch.components;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.moss.launch.components.util.MavenCoordinates;

/**
 * <h2>Overview</h2>
 * <p>
 * 	A reference to a component, expressed in terms of {@link MavenCoordinates}.
 * </p>
 * <h2>Version functions & resolving Maven coordinates for launch specs</h2>
 * <h3>Introduction</h3>
 * <p>
 *   A primary goal of a launch-spec is to unambiguously identify the components needed
 *   to assemble and launch an application.  However, when a component is referenced
 *   by a set of maven coordinates that employs special function tokens (see below),
 *   those coordinates take on a certain level of ambiguity.  Recognizing this, 
 *   java-launch was designed with a particular strategy in mind for mitigating this
 *   ambiguity problem.  The following discussion outlines that strategy.
 * </p>
 * <h3>Maven Version Functions</h3>
 * <p> 
 *   A maven coordinate set consists of a variety of types of values: groupId, 
 *   artifactId, version, etc.  However, of all these coordinate types, the
 *   'version' is unique in that it can contain special tokens that invoke 
 *   special behavior when interpreted by maven's dependency resolution code.  
 *   These tokens include:
 * </p>
 * <ul>
 * 	<li><b>SNAPSHOT - </b> Dynamically resolves to the latest development version of the project/module.</li>
 *  <li><b>RELEASE - </b></li>
 *  <li><b>LATEST - </b></li>
 * </ul>
 * 
 * <h3>Build->Deploy Reproducability is Important</h3>
 * <p>
 *   When a launch-spec is dynamically produced as part of a maven build, it 
 *   is important that the resulting launch-spec faithfully communicates the 
 *   specific builds of each of the dependencies that were used at compile/build 
 *   time. Otherwise, at deploy/run-time, the deployment/launch tooling may end-up 
 *   selecting builds of the dependencies that differ from those against which
 *   the app was actually built (e.g. somebody executes 'mvn install' again, a 
 *   new build is produced by a build server, etc).  When this happens, it can lead to
 *   inconsistent results at deploy/run time as compared with compile time, 
 *   and thus lead to pesky bugs that are not easily reproducible 'in the lab'.
 * </p>
 * <h3>How Version Functions Impact Build->Depoy Reproducability</h3>
 * <p>
 *   When a maven coordinate set contains a version function, it is normal 
 *   and expected for maven to interpret that coordinate set as a reference to 
 *   various separate builds at various times and in various contexts.  This is
 *   true even within the span of a single development cycle. This is because 
 *   <i><u> these functions express a means of finding a similar build, rather than
 *   finding a specific, individual, historical build</u></i>.  Therefore, if a 
 *   {@link MavenCoordinatesHandle} that contains a version function is the only handle
 *   a launch-spec has on a given component, the information about how maven resolved that
 *   handle at build-time is not present. The result is a Build-Deploy 
 *   Reproducability problem, since the best that any code that later interprets 
 *   that handle can do is re-resolve the dependency within its current maven context,
 *   which is likely different from that in which the build was made, and which
 *   in turn will likely result in the handle being resolved to a different build from that
 *   to which maven resolved it at build-time.
 * </p>
 * <h3>The mitigation strategy</h3>
 * <p>
 *   There are multiple approaches that could be used to mitigate this problem.  Java-launch 
 *   (and it's related, dependant projects 'appkeep' and 'appsnap') is designed with one
 *   specific approach in mind:
 * </p>
 * <ol>
 * 		<li><p><b>Version function tokens are treated opaquely.  </b> java-launch and friends makes no attempt to interpret these tokens.  Instead, </p></li>
 * 		<li><p><b>Ambiguous maven coordinate sets are treated as not sufficiently ambiguous to refer to a launch
 *   component in and of themselves.  As such, </b></li>
 *      <li><p><b>They can only be used in concert with another component handle 
 *   type (such as md5, or the build-timestamp type).</b>  The combination of a 
 *   SNAPSHOT-versioned maven coordinate and a MD5 hash, provides enough information for a run/deploy
 *   tool to locate and identify a specific build from either a local or remote maven repository.
 *   </li>
 * </ol>
 * <p>
 *   With this strategy in mind, all the tools in the toolchain have been designed with fail-fast 
 *   behavior when they see a component that is described solely in terms of single version-function
 *   containing {@link MavenCoordinatesHandle}; Put another way,  <b>ALL TOOLS WILL CHOKE WHEN ASKED
 *   TO RESOLVE A COMPONENT BASED SOLELY ON A {@link MavenCoordinatesHandle} THAT HAS A VERSION FUNCTION
 *   SUCH AS 'SNAPSHOT' - SUCH HANDLES MUST BE ACCOMPANIED BY ADDITIONAL MEANS OF IDENTIFICATION SUCH AS
 *   A {@link BuildTimestampComponentHandle}, AN {@link Md5ComponentHandle}, ETC. </b>
 *   
 * </p>
 * 
 * @see <a href="http://www.sonatype.com/books/mvnref-book/reference/pom-relationships-sect-pom-syntax.html">The Maven Book: Pom Syntax</a>
 * @see <a href="http://www.sonatype.com/people/2009/12/maven-dependency-resolution-a-repository-perspective/">Sonatype Blog: Maven Dependency Resolution – A Repository Perspective</a>
 * @see <a href="http://maven.apache.org/pom.html#Maven_Coordinates">The Maven Pom Documentation: Maven Coordinates Section</a>
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
public class MavenCoordinatesHandle extends ComponentHandle implements Serializable {
	
	public static MavenCoordinatesHandle parse(String text){
		return new MavenCoordinatesHandle(new MavenCoordinates(text));
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
	
	MavenCoordinatesHandle() {}
	
	public MavenCoordinatesHandle(String groupId, String artifactId, String version) {
		this(new MavenCoordinates(groupId, artifactId, version));
	}
	
	MavenCoordinatesHandle(MavenCoordinates coordinates){
		this.groupId = coordinates.groupId();
		this.artifactId = coordinates.artifactId();
		this.version = coordinates.version();
		this.packaging = coordinates.packaging();
		this.classifier = coordinates.classifier();
	}
	
	public MavenCoordinatesHandle(String groupId, String artifactId, String version, String packaging, String classifier) {
		this(new MavenCoordinates(groupId, artifactId, version, packaging, classifier));
	}

	@Override
	public <T> T accept(ComponentHandleVisitor<T> v) {
		return v.visit(this);
	}
	
	public MavenCoordinates coordinates(){
		return new MavenCoordinates(groupId, artifactId, version, packaging, classifier);
	}
	
	@Override
	public String toString() {
		return coordinates().toString();
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof MavenCoordinatesHandle && ((MavenCoordinatesHandle)o).toString().equals(toString());
	}
	@Override
	public int hashCode() {
		return coordinates().hashCode();
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
