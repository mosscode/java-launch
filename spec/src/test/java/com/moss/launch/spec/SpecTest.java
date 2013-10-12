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
package com.moss.launch.spec;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import bmsi.util.JavaDiff;

import com.moss.jaxbhelper.JAXBHelper;

public class SpecTest extends TestCase {
	public void testApplicationRoundtrip() throws Exception {
		JAXBHelper helper = new JAXBHelper(JavaAppSpec.class);
		final String[] cases = new String[]{
				"a.xml",
				"b.xml"
		};
		
		for(String next : cases){
			String xmlIn = beautify(readText(getClass().getResourceAsStream(next)));
			JavaAppSpec spec = helper.readFromXmlString(xmlIn);
			String xmlOut = helper.writeToXmlString(spec);
			if(!xmlIn.equals(xmlOut)){
				String diff = new JavaDiff().unifiedDiff(xmlIn, xmlOut);
				System.out.println(diff);
				assertEquals(xmlIn, xmlOut);
			}
		}
	}
	
	public void testAppletRoundtrip() throws Exception {
		JAXBHelper helper = new JAXBHelper(JavaAppletSpec.class);
		final String[] cases = new String[]{
				"c.xml",
		};
		
		for(String next : cases){
			String xmlIn = beautify(readText(getClass().getResourceAsStream(next)));
			JavaAppletSpec spec = helper.readFromXmlString(xmlIn);
			String xmlOut = helper.writeToXmlString(spec);
			if(!xmlIn.equals(xmlOut)){
				String diff = new JavaDiff().unifiedDiff(xmlIn, xmlOut);
				System.out.println(diff);
				assertEquals(xmlIn, xmlOut);
			}
		}
	}
	
	private String readText(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder out = new StringBuilder();
		char[] buffer = new char[1024*1024];
		for(int numRead = reader.read(buffer); numRead!=-1;numRead = reader.read(buffer)){
			out.append(buffer, 0, numRead);
		}
		in.close();
		return out.toString();
	}
	
	private static String beautify(String xml) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		beautify(new StringBufferInputStream(xml), bytes);
		return new String(bytes.toByteArray());
	}
	
	private static void beautify(InputStream in, OutputStream out) throws Exception{
		Source xmlSource = new StreamSource(in);
        Source xsltSource = new StreamSource(JAXBHelper.class.getResourceAsStream("prettyprint.xsl"));

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(out));
	}
}
