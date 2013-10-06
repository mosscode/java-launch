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

import java.io.UnsupportedEncodingException;
/**
 * With thanks to http://www.rgagnon.com/javadetails/java-0596.html
 */
public class HexTextCodec {

	static final byte[] HEX_CHAR_TABLE = {
		(byte)'0', (byte)'1', (byte)'2', (byte)'3',
		(byte)'4', (byte)'5', (byte)'6', (byte)'7',
		(byte)'8', (byte)'9', (byte)'a', (byte)'b',
		(byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};    

	public static String getHexString(byte[] raw) {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			
			
//			try {
//				int p1 = v >>> 4;
//				int p2 = v & 0xF;
//				System.out.println(b + " = " + v);
//				System.out.println(p1 + " = " + new String(new byte[]{HEX_CHAR_TABLE[p1]}, "ASCII"));
//				System.out.println(p2 + " = " + new String(new byte[]{HEX_CHAR_TABLE[p2]}, "ASCII"));
//				System.out.println("---------");
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
			
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		try {
			return new String(hex, "ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] readHexString(CharSequence text){
		byte[] data = new byte[text.length()/2];

		int x=0;
		while(x<data.length){
			data[x] = (byte) ((convert(text.charAt(x*2)) * 16) + convert(text.charAt((x*2)+1)));
			x++;
		}
		return data;
	}

	private static byte convert(char c){
		switch(c){
		case '0': return 0x00;
		case '1': return 0x01;
		case '2': return 0x02;
		case '3': return 0x03;
		case '4': return 0x04;
		case '5': return 0x05;
		case '6': return 0x06;
		case '7': return 0x07;
		case '8': return 0x08;
		case '9': return 0x09;
		case 'a': return 0x0a;
		case 'b': return 0x0b;
		case 'c': return 0x0c;
		case 'd': return 0x0d;
		case 'e': return 0x0e;
		case 'f': return 0x0f;
		default: throw new RuntimeException("'" + c + "' is not a hex character");
		}
	}
	public static void main(String args[]) throws Exception{
//		System.out.println("e is " + convert('e'));
//		byte t = 127;
//		System.out.println(t);
//		String hex = getHexString(new byte[]{(byte)228});
//		System.out.println(hex);
//		System.out.println(readHexString(hex)[0]);
	}
		
}
