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
package com.tenio.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.tenio.entity.element.TObject;
import com.tenio.logger.AbstractLogger;
import com.tenio.utility.XMLUtility;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * TenIOConfig.example.xml. You can also extend this file to create your own
 * configuration values.
 * 
 * <h1>Configuration for game server, declared in properties file</h1> <br>
 * <ul>
 * <li><i>webSocketPort:</i> WebSocket port</li>
 * <li><i>socketPort:</i> TCP port</li>
 * <li><i>datagramPort:</i> UDP port</li>
 * <li><i>keepPlayerOnDisconnect:</i> When the server get disconnection of one
 * client, can be hold its player instance until timeout</li>
 * <li><i>maxHeartbeat:</i> The maximum number of heartbeats which game can
 * handle</li>
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
public abstract class BaseConfiguration extends AbstractLogger {

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
	private final Map<String, String> __configuration = new HashMap<String, String>();

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
	 * @throws Exception some exceptions, which can be occurred in reading or
	 *                   parsing the file
	 */
	public void load(final String file) throws Exception {

		Document xDoc = XMLUtility.parseFile(new File(file));
		Node root = xDoc.getFirstChild();
		NodeList attrNodes = root.getChildNodes();

		for (int i = 0; i < attrNodes.getLength(); i++) {
			var attrNode = attrNodes.item(i);

			// Properties
			var attrRootProperties = XMLUtility.getNodeList(attrNode, "//Server/Properties/Property");
			for (int j = 0; j < attrRootProperties.getLength(); j++) {
				var pDataNode = attrRootProperties.item(j);
				switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
				case "name":
					__configuration.put(SERVER_NAME, pDataNode.getTextContent());
					break;

				case "id":
					__configuration.put(SERVER_ID, pDataNode.getTextContent());
					break;

				case "versionName":
					__configuration.put(VERSION_NAME, pDataNode.getTextContent());
					break;

				case "versionCode":
					__configuration.put(VERSION_CODE, pDataNode.getTextContent());
					break;
				}
			}

			// Port
			var attrRootPort = attrNode.getChildNodes();
			for (int j = 0; j < attrRootPort.getLength(); j++) {
				var pDataNode = attrRootPort.item(j);
				switch (pDataNode.getNodeName()) {
				case "Socket":
					__configuration.put(SOCKET_PORT, pDataNode.getTextContent());
					break;
				case "Datagram":
					__configuration.put(DATAGRAM_PORT, pDataNode.getTextContent());
					break;
				case "WebSocket":
					__configuration.put(WEBSOCKET_PORT, pDataNode.getTextContent());
					break;
				}
			}

			// Configuration
			var attrConfigurationProperties = XMLUtility.getNodeList(attrNode,
					"//Server/Configuration/Properties/Property");
			for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
				var pDataNode = attrConfigurationProperties.item(j);
				switch (pDataNode.getAttributes().getNamedItem("name").getTextContent()) {
				case "keepPlayerOnDisconnect":
					__configuration.put(KEEP_PLAYER_ON_DISCONNECT, pDataNode.getTextContent());
					break;

				case "maxHeartbeat":
					__configuration.put(MAX_HEARTBEAT, pDataNode.getTextContent());
					break;

				case "maxPlayer":
					__configuration.put(MAX_PLAYER, pDataNode.getTextContent());
					break;

				case "idleReader":
					__configuration.put(IDLE_READER, pDataNode.getTextContent());
					break;

				case "idleWriter":
					__configuration.put(IDLE_WRITER, pDataNode.getTextContent());
					break;

				case "emptyRoomScan":
					__configuration.put(EMPTY_ROOM_SCAN, pDataNode.getTextContent());
					break;

				case "timeoutScan":
					__configuration.put(TIMEOUT_SCAN, pDataNode.getTextContent());
					break;

				case "ccuScan":
					__configuration.put(CCU_SCAN, pDataNode.getTextContent());
					break;
				}
			}

			// Extension
			var attrExtensionProperties = XMLUtility.getNodeList(attrNode, "//Server/Extension/Properties/Property");
			var extProperties = TObject.newInstance();
			for (int j = 0; j < attrExtensionProperties.getLength(); j++) {
				var pDataNode = attrExtensionProperties.item(j);
				var key = pDataNode.getAttributes().getNamedItem("name").getTextContent();
				var value = pDataNode.getTextContent();
				extProperties.put(key, value);
			}
			_extend(extProperties);

		}

		// Put the current configurations to the logger
		info("Configuration", toString());

	}

	/**
	 * Put new configuration
	 * 
	 * @param key   key
	 * @param value value
	 */
	protected void _put(final String key, final String value) {
		__configuration.put(key, value);
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Boolean}
	 */
	public boolean getBoolean(final String key) {
		return Boolean.parseBoolean(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Integer}
	 */
	public int getInt(final String key) {
		return Integer.parseInt(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link Float}
	 */
	public float getFloat(final String key) {
		return Float.parseFloat(__configuration.get(key));
	}

	/**
	 * @param key the configuration's key
	 * @return the value in {@link String}
	 */
	public String getString(final String key) {
		return __configuration.get(key);
	}

	/**
	 * Determine if this configuration is existed or defined. If you want some
	 * configuration value to be treated as an "undefined" status, let its value
	 * "-1".
	 * 
	 * @param key The desired configuration's key
	 * @return <b>true</b> if the configuration is defined and otherwise return
	 *         <b>false</b>
	 */
	public boolean isDefined(final String key) {
		return __configuration.get(key) == null ? false : (getString(key).equals("-1") ? false : true);
	}

	@Override
	public String toString() {
		return __configuration.toString();
	}

	/**
	 * Your extension part can be handled here. Check the examples for more details
	 * about how to use it.
	 * 
	 * @param extProperties the extension data in key-value format (see
	 *                      {@link TObject})
	 */
	protected abstract void _extend(TObject extProperties);

}
