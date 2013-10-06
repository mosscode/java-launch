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

import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.moss.diff.DiffCommand;
import com.moss.launch.components.ComponentHandle;
import com.moss.launch.components.CRC32ComponentHandle;
import com.moss.launch.components.Component;
import com.moss.launch.components.LaxComponentHandleVisitor;
import com.moss.launch.components.MavenCoordinatesHandle;
import com.moss.launch.introspect.LocatedComponentSet;

public class PropertiesParsingTest extends TestCase {
	public void testRun() throws Exception {
		
		LocatedComponentSet set;
		final Properties p1 = new Properties();
		{
			p1.load(getClass().getResourceAsStream("a.properties"));
			set = PropertiesTool.read(p1, getClass().getClassLoader());
		}
		
		final Properties p2;
		{
			p2 = PropertiesTool.write(set, testLocator);
			p2.store(System.out, "Testing 123");
		}
		
		assertEquals(p1, p2);
	}
	
	private ComponentResourceLocator testLocator = new ComponentResourceLocator() {
		
		public String componentResource(Component c) {
			for(ComponentHandle h : c.artifactHandles()){
				String resource = h.accept(new LaxComponentHandleVisitor<String>() {
					public String visit(MavenCoordinatesHandle m) {
						return "/launch-spec/components/" + m.artifactId() + "-" + m.version() + ".jar";
					}
					@Override
					protected String defaultValue(ComponentHandle h) {
						return null;
					}
				});
				if(resource!=null){
					return resource;
				}
			}
			throw new RuntimeException("Unable to create dummy resource location for " + c);
		}
	};
	
	private void assertEquals(Properties expected, Properties actual) throws Exception {
		String a = toString(expected);
		String b = toString(actual);
		if(!a.equals(b)){
			String diff = new DiffCommand().unifiedDiff(a, b);
			throw new AssertionFailedError("Not equal:\n" + diff);
		}
	}
	
	private String toString(Properties p){
		StringBuffer text = new StringBuffer();
		
		SortedSet<Object> keys = new TreeSet<Object>(p.keySet());
		
		for(Object key : keys){
			text.append(key.toString());
			text.append('=');
			text.append(p.get(key).toString());
			text.append('\n');
		}
		
		return text.toString();
	}
}
