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

package com.tenio.core.server.setting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exception.ConfigurationException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ConfigurationAssessment")
class ConfigurationAssessmentTest {

  private EventManager eventManager;
  private Configuration configuration;

  @BeforeEach
  void setUp() {
    eventManager = mock(EventManager.class);
    configuration = mock(Configuration.class);
  }

  @Test
  @DisplayName("newInstance creates a non-null assessment")
  void testNewInstanceCreatesNonNull() {
    assertNotNull(ConfigurationAssessment.newInstance(eventManager, configuration));
  }

  @Test
  @DisplayName("assess passes when keep-player-on-disconnection is false and no network configs")
  void testAssessPassesWhenKeepPlayerOffAndNoNetworkConfigs() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(false);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(null);
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(null);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertDoesNotThrow(assessment::assess);
  }

  @Test
  @DisplayName("assess throws NotDefinedSubscribersException when reconnection enabled but subscribers missing")
  void testAssessThrowsWhenReconnectionSubscribersMissing() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(true);
    when(eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLING))
        .thenReturn(false);
    when(eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECTED)).thenReturn(false);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertThrows(NotDefinedSubscribersException.class, assessment::assess);
  }

  @Test
  @DisplayName("assess passes when reconnection enabled and both subscribers defined")
  void testAssessPassesWhenReconnectionSubscribersDefined() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(true);
    when(eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLING))
        .thenReturn(true);
    when(eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECTED)).thenReturn(true);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(null);
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(null);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertDoesNotThrow(assessment::assess);
  }

  @Test
  @DisplayName("assess throws NotDefinedSubscribersException when TCP+UDP configured but datagram subscribers missing")
  void testAssessThrowsWhenTcpUdpWithoutDatagramSubscribers() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(false);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(new Object());
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(new Object());
    when(eventManager.hasSubscriber(
        ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION)).thenReturn(false);
    when(eventManager.hasSubscriber(
        ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT)).thenReturn(false);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertThrows(NotDefinedSubscribersException.class, assessment::assess);
  }

  @Test
  @DisplayName("assess passes when TCP+UDP configured and datagram subscribers defined")
  void testAssessPassesWhenTcpUdpWithDatagramSubscribersDefined() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(false);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(new Object());
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(new Object());
    when(eventManager.hasSubscriber(
        ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION)).thenReturn(true);
    when(eventManager.hasSubscriber(
        ServerEvent.ACCESS_DATAGRAM_CHANNEL_REQUEST_VALIDATION_RESULT)).thenReturn(true);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertDoesNotThrow(assessment::assess);
  }

  @Test
  @DisplayName("assess throws ConfigurationException when UDP configured without TCP")
  void testAssessThrowsWhenUdpWithoutTcp() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(false);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(null);
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(new Object());

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertThrows(ConfigurationException.class, assessment::assess);
  }

  @Test
  @DisplayName("assess passes when only TCP is configured")
  void testAssessPassesWhenOnlyTcpConfigured() {
    when(configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION))
        .thenReturn(false);
    when(configuration.get(CoreConfigurationType.NETWORK_TCP)).thenReturn(new Object());
    when(configuration.get(CoreConfigurationType.NETWORK_UDP)).thenReturn(null);

    var assessment = ConfigurationAssessment.newInstance(eventManager, configuration);
    assertDoesNotThrow(assessment::assess);
  }
}
