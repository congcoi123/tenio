/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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
import com.tenio.core.bootstrap.injector.ProfileSelection;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.configuration.SocketConfiguration;
import com.tenio.core.network.define.TransportType;
import java.util.HashMap;
import org.w3c.dom.Node;

/**
 * Manages core configuration settings for the game server. This class handles loading and parsing
 * of configuration from XML files, providing access to various server settings and parameters.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>XML-based configuration loading
 *   <li>Support for custom configuration values
 *   <li>Integration with common configuration
 *   <li>Socket configuration management
 *   <li>Transport type configuration
 * </ul>
 *
 * @see CommonConfiguration
 * @see SocketConfiguration
 * @see TransportType
 * @see CoreConfigurationType
 */
public abstract class CoreConfiguration extends CommonConfiguration {

  private ConfigurationSource startupSource;

  /** Applies the prepared XML through the application's normal load override. */
  public final void loadPrepared(String file, ConfigurationSource source) throws Exception {
    startupSource = source;
    try {
      load(file);
    } finally {
      startupSource = null;
    }
  }

  /**
   * Reads file content and converts it to configuration values.
   *
   * @param file The {@link String} name of the configuration file and this file needs to be put in
   *     same folder with the application
   * @throws Exception some exceptions, which can be occurred when reading or parsing the file
   */
  @Override
  public void load(String file) throws Exception {
    var source = startupSource;
    if (source == null) {
      source = ConfigurationSource.read(file, getClass().getClassLoader());
    }
    if (isInfoEnabled()) {
      info("CONFIGURATION", source.description());
    }
    Node root = source.document().getDocumentElement();

    // Server Properties
    String configuredProfiles = null;
    var attrServerProperties = XmlUtility.getNodeList(root, "//Server/Properties/Property");
    for (int j = 0; j < attrServerProperties.getLength(); j++) {
      var dataNode = attrServerProperties.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      if (CoreConfigurationType.PROFILE.getValue().equals(paramName)) {
        configuredProfiles = dataNode.getTextContent();
      }
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }

    // Validate the actual XML value; a reused configuration cannot retain a previous selection.
    if (configuredProfiles == null || !configuredProfiles.isBlank()) {
      ProfileSelection.parseRequired(configuredProfiles);
    }

    // Network Properties
    var attrNetworkProperties = XmlUtility.getNodeList(root, "//Server/Network/Properties/Property");
    for (int j = 0; j < attrNetworkProperties.getLength(); j++) {
      var dataNode = attrNetworkProperties.item(j);
      var paramName = dataNode.getAttributes().getNamedItem("name").getTextContent();
      push(CoreConfigurationType.getByValue(paramName), dataNode.getTextContent());
    }
    // Network Sockets
    var attrNetworkSockets = XmlUtility.getNodeList(root, "//Server/Network/Sockets/Port");
    for (int j = 0; j < attrNetworkSockets.getLength(); j++) {
      var dataNode = attrNetworkSockets.item(j);
      var cacheSizeNode = dataNode.getAttributes().getNamedItem("cacheSize");
      int cacheSize = cacheSizeNode != null
              ? Integer.parseInt(cacheSizeNode.getTextContent())
              : 1; // The default value should be 1
      var socketConfiguration = new SocketConfiguration(
              dataNode.getAttributes().getNamedItem("name").getTextContent(),
              TransportType.getByValue(dataNode.getAttributes().getNamedItem("type").getTextContent()),
              Integer.parseInt(dataNode.getTextContent()),
              cacheSize
      );

      if (socketConfiguration.type() == TransportType.TCP) {
        push(CoreConfigurationType.NETWORK_TCP, socketConfiguration);
      } else if (socketConfiguration.type() == TransportType.WEB_SOCKET) {
        push(CoreConfigurationType.NETWORK_WEBSOCKET, socketConfiguration);
      } else if (socketConfiguration.type() == TransportType.UDP) {
        push(CoreConfigurationType.NETWORK_UDP, socketConfiguration);
      } else if (socketConfiguration.type() == TransportType.HTTP) {
        push(CoreConfigurationType.NETWORK_HTTP, socketConfiguration);
      }
    }

    // Configured Workers
    var attrConfigurationWorkers =
        XmlUtility.getNodeList(root, "//Server/Configuration/Workers/Worker");
    for (int j = 0; j < attrConfigurationWorkers.getLength(); j++) {
      var dataNode = attrConfigurationWorkers.item(j);
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
