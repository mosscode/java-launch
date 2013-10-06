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
package com.moss.launch.tools.introspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.moss.launch.components.BuildTimestampComponentHandle;
import com.moss.launch.components.ComponentHandle;
import com.moss.launch.components.CRC32ComponentHandle;
import com.moss.launch.components.Component;
import com.moss.launch.components.ComponentHandleVisitor;
import com.moss.launch.components.ComponentType;
import com.moss.launch.components.MavenCoordinatesHandle;
import com.moss.launch.components.Md5ComponentHandle;
import com.moss.launch.introspect.LocatedComponent;

public class ComponentInfo {
	public enum Part{CRC32, MD5, MAVEN, RESOURCE, TYPE, WHEN_BUILT}
	
	final int num;
	
	public ComponentInfo(int num) {
		this.num = num;
	}

	Map<Part, String> properties = new HashMap<Part, String>();
	
	
	public ComponentInfo(int num, Component c, String resourceKey) {
		this.num = num;
		
		properties.put(Part.TYPE, c.type().toString());
		
		for(ComponentHandle h : c.artifactHandles()){
			h.accept(new ComponentHandleVisitor<Void>(){
				public Void visit(MavenCoordinatesHandle m) {
					properties.put(Part.MAVEN, m.toString());
					return null;
				}
				public Void visit(CRC32ComponentHandle h) {
					properties.put(Part.CRC32, h.value().toString());
					return null;
				}
				public Void visit(Md5ComponentHandle h) {
					properties.put(Part.MD5, h.hashString());
					return null;
				}
				public Void visit(BuildTimestampComponentHandle h) {
					properties.put(Part.WHEN_BUILT, Long.toString(h.getWhenBuilt().getMillis()));
					return null;
				}
			});
		}
		
		properties.put(Part.RESOURCE, resourceKey);
	}
	
	public void write(Properties p){
		for(Map.Entry<Part, String> next : properties.entrySet()){
			p.put(num + "." + next.getKey().name().toLowerCase(), next.getValue());
		}
	}
	
	void set(Part p, String value){
		if(properties.containsKey(p)){
			throw new RuntimeException(p + " is specified more than once for component " + num);
		}
		properties.put(p, value);
	}
	
	public LocatedComponent build(ComponentLocationResolver r){
		String locationResourceKey = null;
		long checksum = 0;
		List<ComponentHandle> handles = new LinkedList<ComponentHandle>();
		ComponentType type = null;
		
		for(Part p : properties.keySet()){
			final String value = properties.get(p);
			switch(p){
				case RESOURCE:{
					locationResourceKey = value;
					break;
				}
				case CRC32:{
					handles.add(new CRC32ComponentHandle(Long.parseLong(value)));
					break;
				}
				case MAVEN:{
					handles.add(MavenCoordinatesHandle.parse(value));
					break;
				}
				case TYPE:{
					type = ComponentType.valueOf(value);
					break;
				}
				case MD5:{
					handles.add(new Md5ComponentHandle(value));
					break;
				}
				default:{
					throw new RuntimeException("Not sure what to do with this: " + p);
				}
			}
		}
		
		if(handles.size()==0 && checksum==0){
			throw new RuntimeException("No handles specified for component #" + num);
		}
		if(locationResourceKey==null){
			throw new RuntimeException("No location resource specified for component #" + num);
		}
		if(type==null){
			throw new RuntimeException("No type specified for component #" + num);
		}
		
		return new LocatedComponent(new Component(type, handles), r.resolve(locationResourceKey));
	}
}