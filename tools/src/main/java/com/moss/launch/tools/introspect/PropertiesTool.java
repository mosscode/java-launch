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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.moss.launch.introspect.LocatedComponent;
import com.moss.launch.introspect.LocatedComponentSet;
import com.moss.launch.tools.introspect.ComponentInfo.Part;

public class PropertiesTool {
	
	public static Properties write(LocatedComponentSet set, ComponentResourceLocator locator){
		Properties p = new SortedProperties();
		
		String name = set.name();
		p.put("name", name);
		for(int x=0;x<set.locatedComponents().size();x++){
			LocatedComponent c = set.locatedComponents().get(x);
			new ComponentInfo(x+1, c.component(), locator.componentResource(c.component())).write(p);
		}
		
		return p;
	}
	
	public static LocatedComponentSet read(final URL location) throws IOException {
		Properties props = new Properties();
		{
			InputStream in = location.openStream();
			props.load(in);
			in.close();
		}
		if(props.size()==0){
			throw new RuntimeException("Missing bundle name!");
		}
		
		return read(props, new UrlLocationResolver());
	}
	
	public static LocatedComponentSet read(final String bundleKey, final ClassLoader c) throws IOException {

		URL propertiesFile = c.getResource(bundleKey);

		Properties props = new Properties();
		{
			InputStream in = propertiesFile.openStream();
			props.load(in);
			in.close();
		}
		if(props.size()==0){
			throw new RuntimeException("Missing bundle name!");
		}
		
		return read(props, c);
	}
	
	public static LocatedComponentSet read(final Properties props, final ClassLoader c) throws IOException {
		return read(props, new ClasspathLocationResolver(c));
	}

	private static class UrlLocationResolver implements ComponentLocationResolver {
		public URL resolve(String location) {
			try {
				return new URL(location);
			} catch (MalformedURLException e) {
				throw new RuntimeException("Bad URL: " + location, e);
			}
		}
	}
	
	private static class ClasspathLocationResolver implements ComponentLocationResolver {
		private final ClassLoader c;
		
		public ClasspathLocationResolver(ClassLoader c) {
			this.c = c;
		}

		public URL resolve(String location) {
			return c.getResource(location);
		}
	};
	
	public static LocatedComponentSet read(final Properties props, ComponentLocationResolver r) {

		List<LocatedComponent> components = new LinkedList<LocatedComponent>();

		Map<Integer, ComponentInfo> componentData = new HashMap<Integer, ComponentInfo>();
		
		String name = null;
		
		for(Map.Entry<Object, Object> next : props.entrySet()){
			final String key = next.getKey().toString();
			final String value = next.getValue().toString();
			
			if(key.equals("name")){
				if(name!=null){
					throw new RuntimeException("Format error: Multiple names!");
				}else{
					name = value;
				}
			}else{
				try {
					final int firstDot = key.indexOf('.');
					
					final Integer num = Integer.valueOf(key.substring(0, firstDot));
					final Part part = Part.valueOf(key.substring(firstDot + 1).toUpperCase());
					
					ComponentInfo i = componentData.get(num);
					if(i==null){
						i = new ComponentInfo(num);
						componentData.put(num, i);
					}
					i.set(part, value);
				} catch (Exception e) {
					throw new RuntimeException("Error parsing " + key + " or " + value, e);
				}
			}
		}
		
		for(int x=1;x<=componentData.size();x++){
			ComponentInfo next = componentData.get(x);
			if(next==null){
				throw new RuntimeException("Component " + x + " is missing?");
			}
			components.add(componentData.get(x).build(r));
		}
		return new LocatedComponentSet(name, components);
	}
	

	@SuppressWarnings("serial")
	private static class SortedProperties extends Properties {
		/**
		 * Overrides, called by the store method.
		 */
		@Override
		@SuppressWarnings("unchecked")
		public synchronized Enumeration keys() {
			Enumeration keysEnum = super.keys();
			Vector keyList = new Vector();
			while(keysEnum.hasMoreElements()){
				keyList.add(keysEnum.nextElement());
			}
			Collections.sort(keyList, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					if(o1.toString().equals("name")) return -1;
					else return o1.toString().compareTo(o2.toString());
				}
			});
			return keyList.elements();
		}
	}

}
