/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.examples.server;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.utils.XMLUtility;

/**
 * Create your own configurations
 * 
 * @see BaseConfiguration
 * 
 * @author kong
 *
 */
public class Configuration extends BaseConfiguration {

	public static final String CUSTOM_VALUE_1 = "c_customvalue_1";
	public static final String CUSTOM_VALUE_2 = "c_customvalue_2";
	public static final String CUSTOM_VALUE_3 = "c_customvalue_3";
	public static final String CUSTOM_VALUE_4 = "c_customvalue_4";

	public Configuration(final String file) {
		super(file);
	}

	@Override
	protected void _extend(Node attrNode) throws XPathExpressionException {
		NodeList attrConfigurationProperties = XMLUtility.getNodeList(attrNode, "//Server/Extension/Properties/Property");
		for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
			Node pDataNode = attrConfigurationProperties.item(j);
			switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
			case "customValue1":
				_put(CUSTOM_VALUE_1, pDataNode.getTextContent());
				break;

			case "customValue2":
				_put(CUSTOM_VALUE_2, Integer.parseInt(pDataNode.getTextContent()));
				break;

			case "customValue3":
				_put(CUSTOM_VALUE_3, Float.parseFloat(pDataNode.getTextContent()));
				break;

			case "customValue4":
				_put(CUSTOM_VALUE_4, Boolean.parseBoolean(pDataNode.getTextContent()));
				break;
			}
		}
	}

}
