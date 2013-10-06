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

import java.util.LinkedList;
import java.util.List;

import com.moss.launch.spec.select.CpuInstructionSet;
import com.moss.launch.spec.select.RangeSpec;
import com.moss.launch.spec.select.Selector;
import com.moss.launch.spec.select.os.OsFeatureVariant;
import com.moss.launch.spec.select.os.OsFlavor;
import com.moss.launch.spec.select.os.OsHardwareVariant;
import com.moss.launch.spec.select.os.OsProduct;
import com.moss.launch.spec.select.os.OsSelection;
import com.moss.launch.spec.select.vm.VmProduct;
import com.moss.launch.spec.select.vm.VmVendor;

import junit.framework.TestCase;

public class ScorecardTest extends TestCase {
	
	static class StringRangeMatchResult implements RangeMatchResult {
		private final String value;

		public StringRangeMatchResult(String value) {
			this.value = value;
		}
		public int compareTo(Object o) {
			return value.compareTo(o.toString());
		}
		public boolean matches() {
			return value!=null;
		}
	}
	
	static class TestEnv implements LaunchEnvironment {
		private List<String> cpuInstructionSets = new LinkedList<String>();
		private List<String> osFlavors = new LinkedList<String>();
		
		private String 
					javaPlatform,
					osFeatureVariant,
					osHwVariant,
					osProduct,
					osProductUpdate,
					vmProduct,
					vmVendor,
					vmVersion;
		
		private boolean stringMatches(String expected, Object v){
			return v!=null && v.toString().equals(expected);
		}
		
		public boolean has(CpuInstructionSet i) {
			for(String next : cpuInstructionSets){
				if(next.equals(i.toString())){
					return true;
				}
			}
			return false;
		}
		
		public boolean has(OsFeatureVariant v) {
			return stringMatches(osFeatureVariant, v);
		}

		public boolean has(OsFlavor flavor) {
			for(String next : osFlavors){
				if(next.equals(flavor.toString())){
					return true;
				}
			}
			return false;
		}

		public boolean has(OsHardwareVariant v) {
			return stringMatches(this.osHwVariant, v);
		}

		public boolean has(OsProduct p) {
			return stringMatches(this.osProduct, p);
		}

		public boolean has(VmProduct p) {
			return stringMatches(this.vmProduct, p);
		}

		public boolean has(VmVendor v) {
			return stringMatches(this.vmVendor, v);
		}
		
		private static RangeMatchResult doRangeMatch(String value, RangeSpec range){
			if(new DecimalPlusRangeCheck().inRange(value, range)){
				return new StringRangeMatchResult(value);
			}else{
				return new StringRangeMatchResult(null);
			}
		}
		public RangeMatchResult hasJavaPlatform(RangeSpec range) {
			return doRangeMatch(javaPlatform, range);
		}
		public RangeMatchResult hasOsProductUpdate(OsProduct product, RangeSpec range) {
			return doRangeMatch(osProductUpdate, range);
		}
		public RangeMatchResult hasVmVersion(RangeSpec range) {
			return doRangeMatch(vmVersion, range);
		}
	}
	public void testRun(){
		TestEnv e = new TestEnv();
		e.cpuInstructionSets.add("i386");
		e.javaPlatform = "1.5";
		e.osFlavors.add("unix");
		e.osFlavors.add("linux");
		e.osHwVariant = "64bit";
		e.osFeatureVariant = "desktop";
		e.osProduct = "ubuntu-9.10";
		e.vmProduct = "sun";
		e.vmVendor = "sun";
		e.vmVersion = "1.6.0_16";
		
		
		{
			Selector a = new Selector();
			
			Scorecard scA = new Scorecard(a, null, e);
			System.out.println("Score : " + scA.baseScore());
			assertEquals(0, scA.baseScore().intValue());
			
			Selector b = new Selector();
			b.setOs(new OsSelection(new OsProduct("ubuntu-9.10"), new OsFeatureVariant("desktop"), null));
			
			Scorecard scB = new Scorecard(b, null, e);
			System.out.println("Score : " + scB.baseScore());
			assertEquals(2, scB.baseScore().intValue());
			
			assertTrue(scA.compareTo(scB)<0);
			
			Selector c = new Selector();
			c.setOs(new OsSelection(new OsProduct("ubuntu-9.10"), new OsFeatureVariant("desktop"), null));
			c.add(new OsFlavor("unix"));
			
			Scorecard scC = new Scorecard(c, null, e);
			System.out.println("Score : " + scC.baseScore());
			assertEquals(3, scC.baseScore().intValue());
			
			assertTrue(scC.compareTo(scB)>0);
		}
		
		
		
	}
}
