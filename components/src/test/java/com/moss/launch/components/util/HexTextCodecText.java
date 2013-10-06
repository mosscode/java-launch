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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class HexTextCodecText extends TestCase {
	byte[] data = new byte[]{
			0x0,
			0x1,
			0x2,
			0x3,
			0x4,
			0x5,
			0x6,
			0x7,
			0x8,
			0x9,
			0xa,
			0xb,
			0xc,
			0xd,
			0xe,
			0xf
	};
	
	public void testRun() throws Exception {
		assertEquals("000102030405060708090a0b0c0d0e0f", HexTextCodec.getHexString(data));
		assertEquals(data, HexTextCodec.readHexString("000102030405060708090a0b0c0d0e0f"));
	}
	
	public void testRun2() throws Exception {
		final String expectedString = "f54e5f7af2fe7e722cffe631e2bfc0ad";
		
		URL f = getClass().getResource(HexTextCodec.class.getSimpleName()+".data");
		
		String hex = HexTextCodec.getHexString(md5(f));
		assertEquals(expectedString, hex);
		System.out.println(hex + " " + f.getPath());
		
		byte[] expected = md5(f);
		byte[] actual = HexTextCodec.readHexString(hex);
		
		assertEquals(expected, actual);
		/*
		 * output :
		 *   fffefdfcfbfa
		 */

	}
	
	private static byte[] md5(URL f) throws IOException, NoSuchAlgorithmException {
		InputStream in = f.openStream();
		
		MessageDigest d = MessageDigest.getInstance("MD5");
		
		byte[] b = new byte[100*1024];
		for(int x=in.read(b);x!=-1;x = in.read(b)){
			d.update(b, 0, x);
		}
		return d.digest();
	}
	
	private void assertEquals(byte[] expected, byte[] actual){
		if(expected.length!=actual.length){
			throw new AssertionFailedError("Expected " + expected.length + " bytes but was " + actual.length);
		}
		for(int x=0;x<expected.length;x++){
			assertEquals(expected[x], actual[x]);
		}
	}
}
