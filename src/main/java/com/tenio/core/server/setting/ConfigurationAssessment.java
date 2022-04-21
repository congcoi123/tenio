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

package com.tenio.core.server.setting;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ConfigurationException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import com.tenio.core.extension.events.EventAttachConnectionRequestValidation;
import com.tenio.core.extension.events.EventAttachedConnectionResult;
import com.tenio.core.extension.events.EventHttpRequestHandle;
import com.tenio.core.extension.events.EventHttpRequestValidation;
import com.tenio.core.extension.events.EventPlayerReconnectRequestHandle;
import com.tenio.core.extension.events.EventPlayerReconnectedResult;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.define.data.HttpConfig;
import com.tenio.core.network.define.data.SocketConfig;
import java.util.List;

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
    checkSubscriberReconnection();
    checkSubscriberConnectionAttach();
    checkDefinedMainSocketConnection();
    checkSubscriberHttpHandler();
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

  private void checkSubscriberConnectionAttach() throws NotDefinedSubscribersException {
    if (containsTcpSocketConfig() && containsUdpSocketConfig()) {
      if (!eventManager.hasSubscriber(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION)
          || !eventManager.hasSubscriber(ServerEvent.ATTACHED_CONNECTION_RESULT)) {
        throw new NotDefinedSubscribersException(EventAttachConnectionRequestValidation.class,
            EventAttachedConnectionResult.class);
      }
    }
  }

  private void checkDefinedMainSocketConnection() throws ConfigurationException {
    if (!containsTcpSocketConfig() && containsUdpSocketConfig()) {
      throw new ConfigurationException("TCP connection was not defined");
    }
  }

  private void checkSubscriberHttpHandler() throws NotDefinedSubscribersException {
    if (containsHttpPathConfigs()
        && (!eventManager.hasSubscriber(ServerEvent.HTTP_REQUEST_VALIDATION)
        || !eventManager.hasSubscriber(ServerEvent.HTTP_REQUEST_HANDLE))) {
      throw new NotDefinedSubscribersException(EventHttpRequestValidation.class,
          EventHttpRequestHandle.class);
    }
  }

  @SuppressWarnings("unchecked")
  private boolean containsTcpSocketConfig() {
    var socketConfigs =
        (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_CONFIGS);
    return socketConfigs.stream()
        .anyMatch(socketConfig -> socketConfig.getType() == TransportType.TCP);
  }

  @SuppressWarnings("unchecked")
  private boolean containsUdpSocketConfig() {
    var socketConfigs =
        (List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_CONFIGS);
    return socketConfigs.stream()
        .anyMatch(socketConfig -> socketConfig.getType() == TransportType.UDP);
  }

  @SuppressWarnings("unchecked")
  private boolean containsHttpPathConfigs() {
    var httpConfigs = (List<HttpConfig>) configuration.get(CoreConfigurationType.HTTP_CONFIGS);
    return !httpConfigs.isEmpty();
  }
}
