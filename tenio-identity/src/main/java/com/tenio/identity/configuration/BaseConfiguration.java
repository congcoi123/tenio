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
package com.tenio.identity.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.utility.XMLUtility;
import com.tenio.identity.configuration.constant.ConnectionType;
import com.tenio.identity.configuration.constant.RestMethod;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * <ul>
 * <li><i>keepPlayerOnDisconnect:</i> When the server get disconnection of one
 * client, can be hold its player instance until timeout</li>
 * <li><i>maxPlayer:</i> The maximum number of players which game can handle
 * </li>
 * <li><i>idleReader:</i> The max IDLE time in seconds which server can wait
 * from the last getting message from client</li>
 * <li><i>idleWriter:</i> The max IDLE time in seconds which server can wait
 * from the last sending message to client</li>
 * <li><i>emptyRoomScan:</i> Get the period checking in seconds which server can
 * keep the empty room</li>
 * <li><i>timeoutScan:</i> The period checking player time out in seconds</li>
 * <li><i>ccuScan:</i> The period checking CCU in seconds</li>
 * <li><i>serverName:</i> The server name</li>
 * <li><i>serverId:</i> The server id (module name)</li>
 * <li><i>versionName:</i> This current version name of your server in string
 * type</li>
 * <li><i>versionCode:</i> This current version code of your server in integer
 * type (can be compared)</li>
 * </ul>
 * 
 * @author kong
 * 
 */
public abstract class BaseConfiguration extends CommonConfiguration {

	/**
	 * When the server get disconnection of one client, can be hold its player
	 * instance until timeout
	 */
	public static final String KEEP_PLAYER_ON_DISCONNECT = "t.keepPlayerOnDisconnect";
	/**
	 * The maximum number of players which game can handle
	 */
	public static final String MAX_PLAYER = "t.maxPlayer";
	/**
	 * The max IDLE time in seconds which server can wait from the last getting
	 * message from client
	 */
	public static final String IDLE_READER = "t.idleReader";
	/**
	 * The max IDLE time in seconds which server can wait from the last sending
	 * message to client
	 */
	public static final String IDLE_WRITER = "t.idleWriter";
	/**
	 * Get the period checking in seconds which server can keep the empty room
	 */
	public static final String EMPTY_ROOM_SCAN = "t.emptyRoomScan";
	/**
	 * The period checking player time out in seconds
	 */
	public static final String TIMEOUT_SCAN = "t.timeoutScan";
	/**
	 * The period checking CCU in seconds
	 */
	public static final String CCU_SCAN = "t.ccuScan";
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
	 * All ports in sockets zone
	 */
	private final List<Sock> __socketPorts = new ArrayList<Sock>();

	/**
	 * All ports in web sockets zone
	 */
	private final List<Sock> __webSocketPorts = new ArrayList<Sock>();

	/**
	 * All ports in http zone
	 */
	private final List<Http> __httpPorts = new ArrayList<Http>();

	/**
	 * The constructor
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 */
	public BaseConfiguration(final String file) {
		super(file);
	}

	/**
	 * Read file content and convert it to configuration values.
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 * @throws Exception some exceptions, which can be occurred in reading or
	 *                   parsing the file
	 */
	@Override
	protected void _load(final String file) throws Exception {

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
		var attrNetworkSockets = XMLUtility.getNodeList(root, "//Server/Network/Sockets/Port");
		for (int j = 0; j < attrNetworkSockets.getLength(); j++) {
			var pDataNode = attrNetworkSockets.item(j);
			var port = new Sock(pDataNode.getAttributes().getNamedItem("name").getTextContent(),
					__getConnectionType(pDataNode.getAttributes().getNamedItem("type").getTextContent()),
					Integer.parseInt(pDataNode.getTextContent()));
			__socketPorts.add(port);
		}
		var attrNetworkWebSockets = XMLUtility.getNodeList(root, "//Server/Network/WebSockets/Port");
		for (int j = 0; j < attrNetworkWebSockets.getLength(); j++) {
			var pDataNode = attrNetworkWebSockets.item(j);
			var port = new Sock(pDataNode.getAttributes().getNamedItem("name").getTextContent(),
					ConnectionType.WEB_SOCKET, Integer.parseInt(pDataNode.getTextContent()));
			__webSocketPorts.add(port);
		}
		var attrNetworkHttps = XMLUtility.getNodeList(root, "//Server/Network/Http/Port");
		for (int i = 0; i < attrNetworkHttps.getLength(); i++) {
			var pPortNode = attrNetworkHttps.item(i);
			var port = new Http(pPortNode.getAttributes().getNamedItem("name").getTextContent(),
					Integer.parseInt(pPortNode.getAttributes().getNamedItem("value").getTextContent()));

			var attrHttpPaths = XMLUtility.getNodeList(attrNetworkHttps.item(i), "//Path");
			for (int j = 0; j < attrHttpPaths.getLength(); j++) {
				var pPathNode = attrHttpPaths.item(j);
				var path = new Path(pPathNode.getAttributes().getNamedItem("name").getTextContent(),
						__getRestMethod(pPathNode.getAttributes().getNamedItem("method").getTextContent()),
						pPathNode.getTextContent(), pPathNode.getAttributes().getNamedItem("desc").getTextContent(),
						Integer.parseInt(pPathNode.getAttributes().getNamedItem("version").getTextContent()));

				port.addPath(path);
			}

			__httpPorts.add(port);
		}

		// Configuration
		var attrConfigurationProperties = XMLUtility.getNodeList(root, "//Server/Configuration/Properties/Property");
		for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
			var pDataNode = attrConfigurationProperties.item(j);
			switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
			case "keepPlayerOnDisconnect":
				_put(KEEP_PLAYER_ON_DISCONNECT, pDataNode.getTextContent());
				break;

			case "maxPlayer":
				_put(MAX_PLAYER, pDataNode.getTextContent());
				break;

			case "idleReader":
				_put(IDLE_READER, pDataNode.getTextContent());
				break;

			case "idleWriter":
				_put(IDLE_WRITER, pDataNode.getTextContent());
				break;

			case "emptyRoomScan":
				_put(EMPTY_ROOM_SCAN, pDataNode.getTextContent());
				break;

			case "timeoutScan":
				_put(TIMEOUT_SCAN, pDataNode.getTextContent());
				break;

			case "ccuScan":
				_put(CCU_SCAN, pDataNode.getTextContent());
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

	/**
	 * @param type the type name in text
	 * @return the connection type in {@link ConnectionType} type
	 */
	private ConnectionType __getConnectionType(final String type) {
		switch (type.toLowerCase()) {
		case "tcp":
			return ConnectionType.SOCKET;
		case "udp":
			return ConnectionType.DATAGRAM;
		}
		return null;
	}

	/**
	 * @param method the method name in text
	 * @return the method in {@link RestMethod} type
	 */
	private RestMethod __getRestMethod(final String method) {
		switch (method.toLowerCase()) {
		case "get":
			return RestMethod.GET;
		case "post":
			return RestMethod.POST;
		case "put":
			return RestMethod.PUT;
		case "delete":
			return RestMethod.DELETE;
		}
		return null;
	}

	/**
	 * @return the list of socket ports in configuration
	 */
	public List<Sock> getSocketPorts() {
		return __socketPorts;
	}

	/**
	 * @return the list of websocket ports in configuration
	 */
	public List<Sock> getWebSocketPorts() {
		return __webSocketPorts;
	}

	/**
	 * @return the list of http ports in configuration
	 */
	public List<Http> getHttpPorts() {
		return __httpPorts;
	}

}
