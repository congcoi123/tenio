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
package com.tenio.engine.configuration;

import java.io.File;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.utility.XMLUtility;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * <ul>
 * <li><i>maxHeartbeat:</i> The maximum number of heartbeats which game can
 * handle</li>
 * </ul>
 * 
 * @author kong
 * 
 */
public abstract class BaseConfiguration extends CommonConfiguration {

	/**
	 * The maximum number of heartbeats which game can handle
	 */
	public static final String MAX_HEARTBEAT = "t.maxHeartbeat";
	/**
	 * The server name
	 */
	public static final String SERVER_NAME = "t.serverName";
	/**
	 * The server id (module name)
	 */
	public static final String SERVER_ID = "t.serverId";
	/**
	 * This current version name of your server in string type
	 */
	public static final String VERSION_NAME = "t.versionName";
	/**
	 * This current version code of your server in integer type (can be compared)
	 */
	public static final String VERSION_CODE = "t.versionCode";

	/**
	 * The constructor
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 */
	public BaseConfiguration(final String file) {
		try {
			load(file);
		} catch (Exception e) {
			error(e, "file: ", file);
		}
	}

	/**
	 * Read file content and convert it to configuration values.
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 * @throws Exception some exceptions, which can be occurred in reading or
	 *                   parsing the file
	 */
	public void load(final String file) throws Exception {

		Document xDoc = XMLUtility.parseFile(new File(file));
		Node root = xDoc.getFirstChild();

		// Properties
		var attrRootProperties = XMLUtility.getNodeList(root, "//Server/Properties/Property");
		for (int j = 0; j < attrRootProperties.getLength(); j++) {
			var pDataNode = attrRootProperties.item(j);
			switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
			case "name":
				_put(SERVER_NAME, pDataNode.getTextContent());
				break;

			case "id":
				_put(SERVER_ID, pDataNode.getTextContent());
				break;

			case "versionName":
				_put(VERSION_NAME, pDataNode.getTextContent());
				break;

			case "versionCode":
				_put(VERSION_CODE, pDataNode.getTextContent());
				break;
			}
		}

		// Network

		// Configuration
		var attrConfigurationProperties = XMLUtility.getNodeList(root, "//Server/Configuration/Properties/Property");
		for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
			var pDataNode = attrConfigurationProperties.item(j);
			switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {

			case "maxHeartbeat":
				_put(MAX_HEARTBEAT, pDataNode.getTextContent());
				break;

			}
		}

		// Extension
		var attrExtensionProperties = XMLUtility.getNodeList(root, "//Server/Extension/Properties/Property");
		var extProperties = new HashMap<String, String>();
		for (int j = 0; j < attrExtensionProperties.getLength(); j++) {
			var pDataNode = attrExtensionProperties.item(j);
			var key = pDataNode.getAttributes().getNamedItem("name").getTextContent();
			var value = pDataNode.getTextContent();
			extProperties.put(key, value);
		}
		_extend(extProperties);

		// Put the current configurations to the logger
		info("Configuration", toString());

	}

}
