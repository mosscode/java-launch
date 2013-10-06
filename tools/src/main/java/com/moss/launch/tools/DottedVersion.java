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

/**
 * Represents a dotted version with one or more numeric segments.
 * 
 * E.g. "1.1", "1.0.1", "23.2.0.0"
 */
public class DottedVersion implements Comparable<DottedVersion>{
	private final int[] parts;
	
	public DottedVersion(String text) {
		String[] strings = text.split("\\.");
		if(strings.length==0){
			throw new RuntimeException("Error parsing version: \"" + text + "\"");
		}
		try {
			this.parts = new int[strings.length];
			for(int x=0;x<strings.length;x++){
				this.parts[x] = Integer.parseInt(strings[x]);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error parsing version \"" + text + "\"", e);
		}
		validate();
	}
	
	public DottedVersion(int[] parts) {
		this.parts = parts;
		validate();
	}

	private void validate() {
		for(int x : parts){
			if(x<0){
				throw new RuntimeException("A version number cannot contain negative numbers: " + x);
			}
		}
	}


	public int compareTo(DottedVersion o) {
		int numPlaces = o.parts.length>parts.length?o.parts.length:parts.length;
		
		DottedVersion a = this.extend(numPlaces);
		DottedVersion b = o.extend(numPlaces);
		
		int comparison = 0;
		
		for(int x=0;x<numPlaces;x++){
			Integer iA = a.parts[x];
			Integer iB = b.parts[x];
			comparison = iA.compareTo(iB);
			if(comparison!=0){
				break;
			}
		}
		
		return comparison;
	}
	
	public DottedVersion extend(int places){
		if(places<=parts.length){
			return this;
		}else{
			int[] parts = new int[places];
			
			for(int x=0;x<places;x++){
				if(x<this.parts.length){
					parts[x] = this.parts[x];
				}else{
					parts[x] = 0;
				}
			}
			return new DottedVersion(parts);
		}
	}
	@Override
	public boolean equals(Object o) {
		return o instanceof DottedVersion && ((DottedVersion)o).compareTo(this)==0;
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public int numParts(){
		return parts.length;
	}
	
	public int part(int pos) {
		return parts[pos];
	}
	
	@Override
	public String toString() {
		StringBuilder text = new StringBuilder(Integer.toString(parts[0]));
		for(int x=1;x<parts.length;x++){
			text.append('.');
			text.append(Integer.toString(parts[x]));
		}
		return text.toString();
	}
}