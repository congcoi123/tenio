/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.utility;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parse an XML file and convert its content to nodes.
 * 
 * @author kong
 * 
 */
public final class XMLUtility {

	private static XPath __oXpath;

	/**
	 * To parse an XML file
	 * 
	 * @param file the XML file, see {@link File}
	 * @return Returns a document object to easy use, see {@link Document}
	 * @throws Exception the exception
	 */
	public static Document parseFile(final File file) throws Exception {
		var dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		var db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);

		var factory = XPathFactory.newInstance();
		__oXpath = factory.newXPath();
		return doc;
	}

	/**
	 * To parse a stream
	 * 
	 * @param in the stream, see {@link InputStream}
	 * @return Returns a document object to easy use, see {@link Document}
	 * @throws Exception the exception
	 */
	public static Document parseStream(final InputStream in) throws Exception {
		var dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		var db = dbf.newDocumentBuilder();
		Document doc = db.parse(in);

		var factory = XPathFactory.newInstance();
		__oXpath = factory.newXPath();
		return doc;
	}

	/**
	 * Get the node content
	 * 
	 * @param node the current node
	 * @return its content in string value
	 */
	public static String getNodeValue(final Node node) {
		String dataValue = node.getTextContent();
		return dataValue;
	}

	/**
	 * Get a list of nodes
	 * 
	 * @param node  the current node
	 * @param xpath the current xpath in string value
	 * @return a list of nodes
	 * @throws XPathExpressionException the exception
	 */
	public static NodeList getNodeList(final Node node, final String xpath) throws XPathExpressionException {
		var nodeList = (NodeList) __oXpath.evaluate(xpath, node, XPathConstants.NODESET);

		return nodeList;
	}

	/**
	 * Retrieve a node
	 * 
	 * @param node  the current node
	 * @param xpath the current xpath in string value
	 * @return the children node
	 * @throws XPathExpressionException the exception
	 */
	public static Node getNode(final Node node, final String xpath) throws XPathExpressionException {
		var nodeRet = (Node) __oXpath.evaluate(xpath, node, XPathConstants.NODE);

		return nodeRet;
	}

	/**
	 * Retrieve node's attribute value
	 * 
	 * @param node the current node
	 * @param name the current attribute name
	 * @return the value of node's attribute
	 */
	public static String getAttrVal(final Node node, final String name) {
		var eNode = (Element) node;

		return eNode.getAttribute(name);
	}

}
