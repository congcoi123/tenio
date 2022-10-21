/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.utility.XmlUtility;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.define.RestMethod;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.define.data.HttpConfig;
import com.tenio.core.network.define.data.PathConfig;
import com.tenio.core.network.define.data.SocketConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This server needs some basic configuration to start running. The
 * configuration file can be defined as an XML file. See an example in
 * <b>configuration.example.xml</b>. You can also extend this file to create your own
 * configuration values.
 */
public abstract class CoreConfiguration extends CommonConfiguration {

  /**
   * All ports in sockets zone.
   */
  private final List<SocketConfig> socketConfigs;

  /**
   * All ports in HTTPs zone.
   */
  private final List<HttpConfig> httpConfigs;

  /**
   * The constructor.
   */
  public CoreConfiguration() {
    socketConfigs = new ArrayList<>();
    httpConfigs = new ArrayList<>();
  }

  /**
   * Reads file content and converts it to configuration values.
   *
   * @param file The {@link String} name of the configuration file and this file needs to be put
   *             in same folder with the application
   * @throws Exception some exceptions, which can be occurred when reading or
   *                   parsing the file
   */
  @Override
  public void load(String file) throws Exception {

    Document document = XmlUtility.parseFile(new File(file));
    Node root = document.getFirstChild();

    // Server Properties
    var attrServerProperties = XmlUtility.getNodeList(root, "//Server/Properties/Property");
    for (int j = 0; j < attrServerProperties.getLength(); j++) {
      var dataNode = attrServerProperties.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }

    // Network Properties
    var attrNetworkProperties =
        XmlUtility.getNodeList(root, "//Server/Network/Properties/Property");
    for (int j = 0; j < attrNetworkProperties.getLength(); j++) {
      var dataNode = attrNetworkProperties.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }
    // Network Sockets
    var attrNetworkSockets = XmlUtility.getNodeList(root, "//Server/Network/Sockets/Port");
    for (int j = 0; j < attrNetworkSockets.getLength(); j++) {
      var dataNode = attrNetworkSockets.item(j);
      var port = new SocketConfig(dataNode.getAttributes().getNamedItem("name").getTextContent(),
          TransportType.getByValue(dataNode.getAttributes().getNamedItem("type").getTextContent()),
          Integer.parseInt(dataNode.getTextContent()));

      socketConfigs.add(port);
    }
    push(CoreConfigurationType.NETWORK_SOCKET_CONFIGS, socketConfigs);
    // Network HTTPs
    var attrNetworkHttps = XmlUtility.getNodeList(root, "//Server/Network/Http/Port");
    for (int i = 0; i < attrNetworkHttps.getLength(); i++) {
      var portNode = attrNetworkHttps.item(i);
      var port = new HttpConfig(portNode.getAttributes().getNamedItem("name").getTextContent(),
          Integer.parseInt(portNode.getAttributes().getNamedItem("value").getTextContent()));

      var attrHttpPaths = XmlUtility.getNodeList(attrNetworkHttps.item(i), "//Path");
      for (int j = 0; j < attrHttpPaths.getLength(); j++) {
        var pathNode = attrHttpPaths.item(j);
        var path = new PathConfig(pathNode.getAttributes().getNamedItem("name").getTextContent(),
            RestMethod.getByValue(
                pathNode.getAttributes().getNamedItem("method").getTextContent()),
            pathNode.getTextContent(),
            pathNode.getAttributes().getNamedItem("desc").getTextContent(),
            Integer.parseInt(pathNode.getAttributes().getNamedItem("version").getTextContent()));

        port.addPath(path);
      }

      httpConfigs.add(port);
    }
    push(CoreConfigurationType.NETWORK_HTTP_CONFIGS, httpConfigs);

    // Implemented Classes
    var attrImplementedClasses = XmlUtility.getNodeList(root, "//Server/Implements/Class");
    for (int j = 0; j < attrImplementedClasses.getLength(); j++) {
      var dataNode = attrImplementedClasses.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }

    // Configured Workers
    var attrConfigurationWorkers =
        XmlUtility.getNodeList(root, "//Server/Configuration/Workers/Worker");
    for (int j = 0; j < attrConfigurationWorkers.getLength(); j++) {
      var dataNode = attrConfigurationWorkers.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }
    // Configured Schedules
    var attrConfigurationSchedules =
        XmlUtility.getNodeList(root, "//Server/Configuration/Schedules/Task");
    for (int j = 0; j < attrConfigurationSchedules.getLength(); j++) {
      var dataNode = attrConfigurationSchedules.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }
    // Configured Properties
    var attrConfigurationProperties =
        XmlUtility.getNodeList(root, "//Server/Configuration/Properties/Property");
    for (int j = 0; j < attrConfigurationProperties.getLength(); j++) {
      var dataNode = attrConfigurationProperties.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }

    // Extension Properties
    var attrExtensionProperties =
        XmlUtility.getNodeList(root, "//Server/Extension/Properties/Property");
    var extProperties = new HashMap<String, String>();
    for (int j = 0; j < attrExtensionProperties.getLength(); j++) {
      var dataNode = attrExtensionProperties.item(j);
      var key = dataNode.getAttributes().getNamedItem("name").getTextContent();
      var value = dataNode.getTextContent();
      extProperties.put(key, value);
    }

    extend(extProperties);
  }
}
