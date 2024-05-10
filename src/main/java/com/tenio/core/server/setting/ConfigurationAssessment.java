/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
 * Asserting the configuration files.
 */
public final class ConfigurationAssessment {

  private final EventManager eventManager;
  private final Configuration configuration;

  private ConfigurationAssessment(EventManager eventManager, Configuration configuration) {
    this.eventManager = eventManager;
    this.configuration = configuration;
  }

  /**
   * Retrieves a new instance of configuration assessment.
   *
   * @param eventManager an instance of {@link EventManager}
   * @param configuration and instance of {@link Configuration}
   * @return a new instance of {@link ConfigurationAssessment}
   */
  public static ConfigurationAssessment newInstance(EventManager eventManager,
                                                    Configuration configuration) {
    return new ConfigurationAssessment(eventManager, configuration);
  }

  /**
   * Assessment.
   *
   * @throws NotDefinedSubscribersException an exception
   * @throws ConfigurationException         an exception
   */
  public void assess() throws NotDefinedSubscribersException, ConfigurationException {
    checkDataSerialization();
    checkSubscriberReconnection();
    checkSubscriberRequestAccessingDatagramChannelHandler();
    checkSubscriberRequestAccessingKcpChannelHandler();
    checkDefinedMainSocketConnection();
  }

  private void checkDataSerialization() {
    var dataSerialization = configuration.getString(CoreConfigurationType.DATA_SERIALIZATION);
    if (Objects.isNull(DataType.getByValue(dataSerialization))) {
      throw new ConfigurationException(String.format("Data Serialization Type {%s} is not " +
              "supported, please reference to the supporting list: %s", dataSerialization,
          Arrays.toString(DataType.values())));
    }
  }

  private void checkSubscriberReconnection() throws NotDefinedSubscribersException {
    if (configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION)) {
      if (!eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE)
          || !eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECTED_RESULT)) {
        throw new NotDefinedSubscribersException(EventPlayerReconnectRequestHandle.class,
            EventPlayerReconnectedResult.class);
      }
    }
  }

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

  private void checkDefinedMainSocketConnection() throws ConfigurationException {
    if (!containsTcpSocketConfiguration() && containsUdpSocketConfiguration()) {
      throw new ConfigurationException("TCP connection was not defined");
    }
  }

  private boolean containsTcpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_TCP));
  }

  private boolean containsUdpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_UDP));
  }

  private boolean containsKcpSocketConfiguration() {
    return Objects.nonNull(configuration.get(CoreConfigurationType.NETWORK_KCP));
  }
}
