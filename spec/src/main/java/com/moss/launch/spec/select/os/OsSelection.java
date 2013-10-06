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
package com.moss.launch.spec.select.os;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.moss.launch.spec.select.RangeSpec;

@XmlType(propOrder={
		"product",
		"update",
		"featureVariant",
		"hardwareVariant"
})
@SuppressWarnings("serial")
public class OsSelection implements Serializable{
	@XmlElement
	private OsProduct product;
	@XmlElement
	private RangeSpec update;
	
	@XmlElement(name="feature-variant")
	private OsFeatureVariant featureVariant;
	
	@XmlElement(name="hw-variant")
	private OsHardwareVariant hardwareVariant;
	
	
	OsSelection() {}
	
	public OsSelection(OsProduct product, RangeSpec update,
			OsFeatureVariant featureVariant, OsHardwareVariant hardwareVariant) {
		super();
		this.product = product;
		this.update = update;
		this.featureVariant = featureVariant;
		this.hardwareVariant = hardwareVariant;
	}

	public OsSelection(OsProduct product, OsFeatureVariant featureVariant, OsHardwareVariant hardwareVariant) {
		super();
		this.product = product;
		this.featureVariant = featureVariant;
		this.hardwareVariant = hardwareVariant;
	}


	public OsProduct getProduct() {
		return product;
	}
	
	public OsFeatureVariant getFeatureVariant() {
		return featureVariant;
	}
	public OsHardwareVariant getHardwareVariant() {
		return hardwareVariant;
	}
	public RangeSpec getUpdate() {
		return update;
	}
}
