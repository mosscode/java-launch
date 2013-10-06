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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.moss.launch.spec.app.AppProfile;
import com.moss.launch.spec.select.CpuInstructionSet;
import com.moss.launch.spec.select.Selector;
import com.moss.launch.spec.select.os.OsFlavor;
import com.moss.launch.spec.select.os.OsSelection;
import com.moss.launch.spec.select.vm.VmSelection;

public class Scorecard implements Comparable<Scorecard>{
	private final Log log = LogFactory.getLog(getClass());
	
	private final AppProfile p;
	private final Selector s;
	
	private int osFlavorHits = 0;
	private int osFlavorMisses = 0;
	
	private Boolean hasOsProduct;
	private Boolean hasOsFeatureVariant;
	private Boolean hasOsHardwareVariant;
	private RangeMatchResult osUpdateMatch;
	
	private Boolean hasVmProduct;
	private Boolean hasVmVendor;
	private RangeMatchResult vmVersionMatch;
	
	private RangeMatchResult javaPlatformMatch;
	
	private int cpuInstructionSetHits = 0;
	private int cpuInstructionSetMisses = 0;
	
	public Scorecard(final Selector s, final AppProfile p, final LaunchEnvironment env) {
		this.s = s;
		this.p = p;
		
		for(OsFlavor f : s.osFlavors()){
			if(env.has(f)){
				osFlavorHits++;
			}else{
				if(log.isDebugEnabled()) log.debug("Doesn't have OS Flavor: " + f);
				osFlavorMisses++;
			}
		}
		for(CpuInstructionSet i : s.cpuInstructionSets()){
			if(env.has(i)){
				cpuInstructionSetHits++;
			}else{
				if(log.isDebugEnabled()) log.debug("Doesn't have CPU instruction set: " + i);
				cpuInstructionSetMisses++;
			}
		}
		OsSelection os = s.os();
		
		if(os!=null){
			if(os.getProduct()!=null){
				hasOsProduct = env.has(os.getProduct());
			}
			
			if(os.getFeatureVariant()!=null){
				hasOsFeatureVariant = env.has(os.getFeatureVariant());
			}
			
			if(os.getHardwareVariant()!=null){
				hasOsHardwareVariant = env.has(os.getHardwareVariant());
			}
			if(os.getUpdate()!=null){
				if(os.getProduct()==null){
					throw new RuntimeException("Os Update defined, but there is no product definition");
				}else{
					osUpdateMatch = env.hasOsProductUpdate(os.getProduct(), os.getUpdate());
				}
			}
		}
		
		if(s.javaPlatform()!=null){
			javaPlatformMatch = env.hasJavaPlatform(s.javaPlatform());
		}
		
		VmSelection vm = s.vm();
		
		if(vm!=null){
			if(vm.getProduct()!=null){
				hasVmProduct = env.has(vm.getProduct());
			}
			
			if(vm.getVendor()!=null){
				hasVmVendor = env.has(vm.getVendor());
			}
			
			if(vm.getVersion()!=null){
				vmVersionMatch = env.hasVmVersion(vm.getVersion());
			}
		}
	}
	
	public AppProfile profile() {
		return p;
	}
	
	public Selector selector() {
		return s;
	}
	
	private boolean missed(RangeMatchResult r, String desc){
		final boolean result = r!=null && !r.matches();
		if(result){
			if(log.isDebugEnabled()) log.debug("Missed " + desc);
		}
		return result;
	}
	
	public Integer baseScore(){
		
		if(
				osFlavorMisses>0 
				|| 
				cpuInstructionSetMisses > 0
				||
				missed(vmVersionMatch, "VM Version")
				||
				missed(javaPlatformMatch, "Java Platform")
				||
				missed(osUpdateMatch, "OS Update")
				){
			return -1;
		}else{
			return new ScoreValue()
			.plus(hasOsProduct, 1, "OS Product")
			.plus(hasOsFeatureVariant, 1, "OS Feature")
			.plus(hasOsHardwareVariant, 1, "OS Hardware")
			.plus(hasVmProduct, 1, "VM Product")
			.plus(hasVmVendor, 1, "VM Vendor")
			.plus(osFlavorHits)
			.plus(cpuInstructionSetHits)
			.value;
		}
	}
	public int compareTo(Scorecard o) {
		final Integer scoreA = baseScore();
		final Integer scoreB = o.baseScore();
		
		if(scoreA==scoreB){
			return 
				osUpdateMatch.compareTo(o.osUpdateMatch)
				+
				vmVersionMatch.compareTo(vmVersionMatch)
				+
				javaPlatformMatch.compareTo(javaPlatformMatch)
				;
		}else {
			return scoreA.compareTo(scoreB);
		}
	}
	
	private static class ScoreValue {
		private final Log log = LogFactory.getLog(getClass());
		private final int value;

		public ScoreValue() {
			this.value = 0;
		}
		
		public ScoreValue(int value) {
			this.value = value;
		}

		public ScoreValue plus(Boolean tf, int p, String desc){
			int v = this.value;
			
			if(v<0){
				if(log.isDebugEnabled()) log.debug("Ignoring " + desc + " because the score is already sub-zero");
			}else{
				if(tf==null){
					//nothing
				}else if(tf){
					v+=p;
					if(log.isDebugEnabled()) log.debug("Adding " + p + " due to " + desc);
				}else{
					// no match
					v = 0;
					if(log.isDebugEnabled()) log.debug("Zeroing" + " due to " + desc);
				}
			}
			return new ScoreValue(v);
		}
		
		public ScoreValue plus(int n){
			return new ScoreValue(value + n);
		}
	}
}