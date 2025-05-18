/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.server.setting;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.DataType;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ConfigurationException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessDatagramChannelRequestValidationResult;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidation;
import com.tenio.core.handler.event.EventAccessKcpChannelRequestValidationResult;
import com.tenio.core.handler.event.EventPlayerReconnectRequestHandle;
import com.tenio.core.handler.event.EventPlayerReconnectedResult;
import java.util.Arrays;
import java.util.Objects;

/**
 * Performs validation and assessment of server configuration settings.
 * This class is responsible for verifying the correctness and completeness
 * of server configuration, including data serialization settings, subscriber
 * definitions, and network protocol configurations.
 *
 * <p>Key features:
 * <ul>
 *   <li>Configuration validation</li>
 *   <li>Subscriber verification</li>
 *   <li>Network protocol assessment</li>
 *   <li>Data serialization checks</li>
 *   <li>Exception handling for misconfigurations</li>
 * </ul>
 *
 * <p>Note: This class performs critical validation of server configuration
 * and should be used during server initialization to ensure proper setup.
 *
 * @see EventManager
 * @see Configuration
 * @see NotDefinedSubscribersException
 * @see ConfigurationException
 * @since 0.3.0
 */
public final class ConfigurationAssessment {

  private final EventManager eventManager;
  private final Configuration configuration;

  private ConfigurationAssessment(EventManager eventManager, Configuration configuration) {
    this.eventManager = eventManager;
    this.configuration = configuration;
  }

  /**
   * Creates a new instance of configuration assessment.
   *
   * @param eventManager   the {@link EventManager} instance for event handling
   * @param configuration  the {@link Configuration} instance to assess
   * @return a new instance of {@link ConfigurationAssessment}
   */
  public static ConfigurationAssessment newInstance(EventManager eventManager,
                                                  Configuration configuration) {
    return new ConfigurationAssessment(eventManager, configuration);
  }

  /**
   * Performs a comprehensive assessment of the server configuration.
   * This method validates various aspects of the configuration including:
   * <ul>
   *   <li>Data serialization settings</li>
   *   <li>Subscriber definitions for reconnection</li>
   *   <li>Network protocol handlers</li>
   *   <li>Socket connection configurations</li>
   * </ul>
   *
   * @throws NotDefinedSubscribersException if required subscribers are not defined
   * @throws ConfigurationException if configuration settings are invalid
   */
  public void assess() throws NotDefinedSubscribersException, ConfigurationException {
    checkDataSerialization();
    checkSubscriberReconnection();
    checkSubscriberRequestAccessingDatagramChannelHandler();
    checkSubscriberRequestAccessingKcpChannelHandler();
    checkDefinedMainSocketConnection();
  }

  /**
   * Validates the data serialization configuration.
   * Ensures that a valid data serialization type is specified.
   */
  private void checkDataSerialization() {
    var dataSerialization = configuration.getString(CoreConfigurationType.DATA_SERIALIZATION);
    if (Objects.isNull(DataType.getByValue(dataSerialization))) {
      throw new ConfigurationException(String.format("Data Serialization Type {%s} is not " +
              "supported, please reference to the supporting list: %s", dataSerialization,
          Arrays.toString(DataType.values())));
    }
  }

  /**
   * Verifies that required subscribers for reconnection handling are defined.
   *
   * @throws NotDefinedSubscribersException if required subscribers are missing
   */
  private void checkSubscriberReconnection() throws NotDefinedSubscribersException {
    if (configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION)) {
      if (!eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE)
          || !eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECTED_RESULT)) {
        throw new NotDefinedSubscribersException(EventPlayerReconnectRequestHandle.class,
            EventPlayerReconnectedResult.class);
      }
    }
  }

  /**
   * Validates the presence of required subscribers for datagram channel handling.
   *
   * @throws NotDefinedSubscribersException if required subscribers are missing
   */
  private void checkSubscriberRequestAccessingDatagramChannelHandler()
      throws NotDefinedSubscribersException {
    if (containsTcpSocketConfiguration() && containsUdpSocketConfiguration()) {
      if (!eventManager.hasSubscriber(ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION)
          || !eventManager.hasSubscriber(
          ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT)) {
        throw new NotDefinedSubscribersException(EventAccessDatagramChannelRequestValidation.class,
            EventAccessDatagramChannelRequestValidationResult.class);
      }
    }
  }

  /**
   * Validates the presence of required subscribers for KCP channel handling.
   *
   * @throws NotDefinedSubscribersException if required subscribers are missing
   */
  private void checkSubscriberRequestAccessingKcpChannelHandler()
      throws NotDefinedSubscribersException {
    if (containsTcpSocketConfiguration() && containsKcpSocketConfiguration()) {
      if (!eventManager.hasSubscriber(ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION)
          || !eventManager.hasSubscriber(
          ServerEvent.ACCESS_KCP_CHANNEL_REQUEST_VALIDATION_RESULT)) {
        throw new NotDefinedSubscribersException(EventAccessKcpChannelRequestValidation.class,
            EventAccessKcpChannelRequestValidationResult.class);
      }
    }
  }

  /**
   * Verifies that at least one main socket connection type is configured.
   *
   * @throws ConfigurationException if no valid socket connection is configured
   */
  private void checkDefinedMainSocketConnection() throws ConfigurationException {
    if (!containsTcpSocketConfiguration() && containsUdpSocketConfiguration()) {
      throw new ConfigurationException("TCP connection was not defined");
    }
  }

  /**
   * Checks if TCP socket configuration is present.
   *
   * @return {@code true} if TCP socket configuration exists
   */
  private boolean containsTcpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_TCP));
  }

  /**
   * Checks if UDP socket configuration is present.
   *
   * @return {@code true} if UDP socket configuration exists
   */
  private boolean containsUdpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_UDP));
  }

  /**
   * Checks if KCP socket configuration is present.
   *
   * @return {@code true} if KCP socket configuration exists
   */
  private boolean containsKcpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_KCP));
  }
}
