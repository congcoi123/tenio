/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.utility.XMLUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.entity.BroadcastConfig;
import com.tenio.core.configuration.entity.HttpConfig;
import com.tenio.core.configuration.entity.PathConfig;
import com.tenio.core.configuration.entity.SocketConfig;
import com.tenio.core.network.define.RestMethod;
import com.tenio.core.network.define.TransportType;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * @see CoreConfigurationType
 * 
 * @author kong
 * 
 */
public abstract class CoreConfiguration extends CommonConfiguration {

	/**
	 * All ports in sockets zone
	 */
	private final List<SocketConfig> __socketPorts;

	/**
	 * All ports in broadcasts zone
	 */
	private final List<BroadcastConfig> __broadcastPorts;

	/**
	 * All ports in web sockets zone
	 */
	private final List<SocketConfig> __webSocketPorts;

	/**
	 * All ports in http zone
	 */
	private final List<HttpConfig> __httpPorts;

	/**
	 * The constructor
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 */
	public CoreConfiguration(String file) {
		__socketPorts = new ArrayList<SocketConfig>();
		__broadcastPorts = new ArrayList<BroadcastConfig>();
		__webSocketPorts = new ArrayList<SocketConfig>();
		__httpPorts = new ArrayList<HttpConfig>();

		try {
			__load(file);
		} catch (Exception e) {
			_error(e, "file: ", file);
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
	private void __load(String file) throws Exception {

		Document xDoc = XMLUtility.parseFile(new File(file));
		Node root = xDoc.getFirstChild();

		// Server's Properties
		var attrRootProperties = XMLUtility.getNodeList(root, "//Server/Properties/Property");
		for (int j = 0; j < attrRootProperties.getLength(); j++) {
			var pDataNode = attrRootProperties.item(j);
			var paramName = pDataNode.getAttributes().getNamedItem("name").getTextContent();
			_push(CoreConfigurationType.getByValue(paramName), pDataNode.getTextContent());
		}

		// Network
		var attrNetworkSockets = XMLUtility.getNodeList(root, "//Server/Network/Sockets/Port");
		for (int j = 0; j < attrNetworkSockets.getLength(); j++) {
			var pDataNode = attrNetworkSockets.item(j);
			var port = new SocketConfig(pDataNode.getAttributes().getNamedItem("name").getTextContent(),
					TransportType.getByValue(pDataNode.getAttributes().getNamedItem("type").getTextContent()),
					Integer.parseInt(pDataNode.getTextContent()));

			__socketPorts.add(port);
		}
		var attrNetworkServerBroadcasts = XMLUtility.getNodeList(root, "//Server/Network/Broadcasts/ServerPort");
		for (int j = 0; j < attrNetworkServerBroadcasts.getLength(); j++) {
			var pDataNode = attrNetworkServerBroadcasts.item(j);
			_push(CoreConfigurationType.SERVER_BROADCAST_PORT, pDataNode.getTextContent());
		}
		var attrNetworkBroadcasts = XMLUtility.getNodeList(root, "//Server/Network/Broadcasts/Port");
		for (int j = 0; j < attrNetworkBroadcasts.getLength(); j++) {
			var pDataNode = attrNetworkBroadcasts.item(j);
			var port = new BroadcastConfig(pDataNode.getAttributes().getNamedItem("id").getTextContent(),
					pDataNode.getAttributes().getNamedItem("name").getTextContent(),
					Integer.parseInt(pDataNode.getTextContent()));

			__broadcastPorts.add(port);
		}
		var attrNetworkWebSockets = XMLUtility.getNodeList(root, "//Server/Network/WebSockets/Port");
		for (int j = 0; j < attrNetworkWebSockets.getLength(); j++) {
			var pDataNode = attrNetworkWebSockets.item(j);
			var port = new SocketConfig(pDataNode.getAttributes().getNamedItem("name").getTextContent(),
					TransportType.WEB_SOCKET, Integer.parseInt(pDataNode.getTextContent()));
			__webSocketPorts.add(port);
		}
		var attrNetworkHttps = XMLUtility.getNodeList(root, "//Server/Network/Http/Port");
		for (int i = 0; i < attrNetworkHttps.getLength(); i++) {
			var pPortNode = attrNetworkHttps.item(i);
			var port = new HttpConfig(pPortNode.getAttributes().getNamedItem("name").getTextContent(),
					Integer.parseInt(pPortNode.getAttributes().getNamedItem("value").getTextContent()));

			var attrHttpPaths = XMLUtility.getNodeList(attrNetworkHttps.item(i), "//Path");
			for (int j = 0; j < attrHttpPaths.getLength(); j++) {
				var pPathNode = attrHttpPaths.item(j);
				var path = new PathConfig(pPathNode.getAttributes().getNamedItem("name").getTextContent(),
						RestMethod.getByValue(pPathNode.getAttributes().getNamedItem("method").getTextContent()),
						pPathNode.getTextContent(), pPathNode.getAttributes().getNamedItem("desc").getTextContent(),
						Integer.parseInt(pPathNode.getAttributes().getNamedItem("version").getTextContent()));

				port.addPath(path);
			}

			__httpPorts.add(port);
		}

		// Ports' Configuration
		_push(CoreConfigurationType.SOCKET_PORTS, __socketPorts);
		_push(CoreConfigurationType.BROADCAST_PORTS, __broadcastPorts);
		_push(CoreConfigurationType.WEBSOCKET_PORTS, __webSocketPorts);
		_push(CoreConfigurationType.HTTP_PORTS, __httpPorts);

		// Parsing configuration data
		var attrConfigurationWorkers = XMLUtility.getNodeList(root, "//Server/Configuration/Workers/Worker");
		for (int j = 0; j < attrConfigurationWorkers.getLength(); j++) {
			var pDataNode = attrConfigurationWorkers.item(j);
			var paramName = pDataNode.getAttributes().getNamedItem("name").getTextContent();
			_push(CoreConfigurationType.getByValue(paramName), pDataNode.getTextContent());
		}
		var attrConfigurationSchedules = XMLUtility.getNodeList(root, "//Server/Configuration/Schedules/Task");
		for (int j = 0; j < attrConfigurationSchedules.getLength(); j++) {
			var pDataNode = attrConfigurationSchedules.item(j);
			var paramName = pDataNode.getAttributes().getNamedItem("name").getTextContent();
			_push(CoreConfigurationType.getByValue(paramName), pDataNode.getTextContent());
		}
		var attrConfigurationProperties = XMLUtility.getNodeList(root, "//Server/Configuration/Properties/Property");
		for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
			var pDataNode = attrConfigurationProperties.item(j);
			var paramName = pDataNode.getAttributes().getNamedItem("name").getTextContent();
			_push(CoreConfigurationType.getByValue(paramName), pDataNode.getTextContent());
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
	}

}
