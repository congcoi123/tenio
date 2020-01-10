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
package com.tenio.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tenio.logger.AbstractLogger;
import com.tenio.utils.XMLUtility;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * + <i>nio:</i> Select NIO library: Netty (1) / Apache Mina (2) <br>
 * + <i>webSocketPort:</i> WebSocket port <br>
 * + <i>socketPort:</i> TCP port <br>
 * + <i>datagramPort:</i> UDP port <br>
 * + <i>keepPlayerOnDisconnect:</i> When the server get disconnection of one
 * client, can be hold it's player instance until timeout <br>
 * + <i>maxHeartbeat:</i> The maximum number of heartbeats which game can handle
 * <br>
 * + <i>maxPlayer:</i> The maximum number of players which game can handle <br>
 * + <i>idleReader:</i> The max IDLE time in seconds which server can wait from
 * the last getting message from client <br>
 * + <i>idleWriter:</i> The max IDLE time in seconds which server can wait from
 * the last sending message to client <br>
 * + <i>emptyRoomScan:</i> Get the period checking in seconds which server can
 * keep the empty room <br>
 * + <i>timeoutScan:</i> The period checking player time out in seconds <br>
 * + <i>ccuScan:</i> The period checking CCU in seconds <br>
 * + <i>serverName:</i> The server name <br>
 * + <i>serverId:</i> The server id (module name) + + <i>versionName:</i> This
 * current version name of your server in string type <br>
 * + <i>versionCode:</i> This current version code of your server in integer
 * type (can be compared)
 * 
 * @author kong
 * 
 */
public abstract class BaseConfiguration extends AbstractLogger {

	public static final String NIO = "t.nio";
	public static final String WEBSOCKET_PORT = "t.webSocketPort";
	public static final String SOCKET_PORT = "t.socketPort";
	public static final String DATAGRAM_PORT = "t.datagramPort";
	public static final String KEEP_PLAYER_ON_DISCONNECT = "t.keepPlayerOnDisconnect";
	public static final String MAX_HEARTBEAT = "t.maxHeartbeat";
	public static final String MAX_PLAYER = "t.maxPlayer";
	public static final String IDLE_READER = "t.idleReader";
	public static final String IDLE_WRITER = "t.idleWriter";
	public static final String EMPTY_ROOM_SCAN = "t.emptyRoomScan";
	public static final String TIMEOUT_SCAN = "t.timeoutScan";
	public static final String CCU_SCAN = "t.ccuScan";
	public static final String SERVER_NAME = "t.serverName";
	public static final String SERVER_ID = "t.serverId";
	public static final String VERSION_NAME = "t.versionName";
	public static final String VERSION_CODE = "t.versionCode";

	/**
	 * All configuration values will be held in this map. You access values by your
	 * defined keys.
	 */
	private Map<String, Object> configuration = new HashMap<String, Object>();

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
			error("EXCEPTION CONFIGURATION", file, e);
		}
	}

	/**
	 * Read file content and convert it to configuration values.
	 * 
	 * @param file The name of your configuration file and this file needs to be put
	 *             in same folder with your application
	 * @throws some exceptions, which can be occurred in reading or parsing the file
	 */
	public void load(final String file) throws Exception {

		Document xDoc = XMLUtility.parseFile(new File(file));
		Node root = xDoc.getFirstChild();
		NodeList attrNodes = root.getChildNodes();

		for (int i = 0; i < attrNodes.getLength(); i++) {
			Node attrNode = attrNodes.item(i);

			// Properties
			NodeList attrRootProperties = XMLUtility.getNodeList(attrNode, "//Server/Properties/Property");
			for (int j = 0; j < attrRootProperties.getLength(); j++) {
				Node pDataNode = attrRootProperties.item(j);
				switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
				case "name":
					configuration.put(SERVER_NAME, pDataNode.getTextContent());
					break;

				case "id":
					configuration.put(SERVER_ID, pDataNode.getTextContent());
					break;

				case "versionName":
					configuration.put(VERSION_NAME, pDataNode.getTextContent());
					break;

				case "versionCode":
					configuration.put(VERSION_CODE, Integer.parseInt(pDataNode.getTextContent()));
					break;
				}
			}

			// Network
			NodeList attrRootNetwork = attrNode.getChildNodes();
			for (int j = 0; j < attrRootNetwork.getLength(); j++) {
				Node pDataNode = attrRootNetwork.item(j);
				switch (pDataNode.getNodeName()) {
				case "Nio":
					configuration.put(NIO, Integer.parseInt(pDataNode.getTextContent()));
					break;
				}
			}

			// Port
			NodeList attrRootPort = attrNode.getChildNodes();
			for (int j = 0; j < attrRootPort.getLength(); j++) {
				Node pDataNode = attrRootPort.item(j);
				switch (pDataNode.getNodeName()) {
				case "Socket":
					configuration.put(SOCKET_PORT, Integer.parseInt(pDataNode.getTextContent()));
					break;
				case "Datagram":
					configuration.put(DATAGRAM_PORT, Integer.parseInt(pDataNode.getTextContent()));
					break;
				case "WebSocket":
					configuration.put(WEBSOCKET_PORT, Integer.parseInt(pDataNode.getTextContent()));
					break;
				}
			}

			// Configuration
			NodeList attrConfigurationProperties = XMLUtility.getNodeList(attrNode,
					"//Server/Configuration/Properties/Property");
			for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
				Node pDataNode = attrConfigurationProperties.item(j);
				switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
				case "keepPlayerOnDisconnect":
					configuration.put(KEEP_PLAYER_ON_DISCONNECT, Boolean.parseBoolean(pDataNode.getTextContent()));
					break;

				case "maxHeartbeat":
					configuration.put(MAX_HEARTBEAT, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "maxPlayer":
					configuration.put(MAX_PLAYER, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "idleReader":
					configuration.put(IDLE_READER, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "idleWriter":
					configuration.put(IDLE_WRITER, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "emptyRoomScan":
					configuration.put(EMPTY_ROOM_SCAN, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "timeoutScan":
					configuration.put(TIMEOUT_SCAN, Integer.parseInt(pDataNode.getTextContent()));
					break;

				case "ccuScan":
					configuration.put(CCU_SCAN, Integer.parseInt(pDataNode.getTextContent()));
					break;
				}
			}

			// Extension
			_extend(attrNode);

		}

		// Put the current configurations to the logger
		info("Configuration", toString());

	}

	protected void _put(final String key, final Object value) {
		configuration.put(key, value);
	}

	public Object get(final String key) {
		return configuration.get(key);
	}

	/**
	 * Determine if this configuration is existed or defined. If you want some
	 * configuration value to be treated as an "undefined" status, let its value
	 * "-1".
	 * 
	 * @param key The desired configuration's key
	 * @return Return <code>true</code> if the configuration is defined and
	 *         otherwise return <code>false</code>
	 */
	public boolean isDefined(final String key) {
		return configuration.get(key) == null ? false
				: ((configuration.get(key).equals("-1") || (int) configuration.get(key) == -1) ? false : true);
	}

	@Override
	public String toString() {
		return configuration.toString();
	}

	/**
	 * Your extension part can be handled here. Check the examples for more details
	 * about how to use it.
	 * 
	 * @param attrNode one node in the XML structure
	 * @throws some exceptions when reading node values @see {@link XPathException}
	 */
	protected abstract void _extend(Node attrNode) throws XPathException;

}
