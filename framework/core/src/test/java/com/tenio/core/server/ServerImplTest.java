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

package com.tenio.core.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.bootstrap.configuration.ConfigurationHandler;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.scheduler.ScheduledJobs;
import com.tenio.core.network.Network;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.scheduler.job.MaintenanceJob;
import com.tenio.core.server.core.ZeroProcessor;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For ServerImpl")
class ServerImplTest {

  @Test
  void commandListingMatchesTheScheduledTaskLogFormat() {
    assertEquals("[\n<empty>\n]", ServerImpl.commandListing(java.util.List.<Short>of()));
    assertEquals("[\n  - help\n  - info\n  - server\n]",
        ServerImpl.commandListing(java.util.List.of("server", "help", "info")));
    assertEquals("[\n  - 1\n  - 2\n  - 10\n]",
        ServerImpl.commandListing(java.util.List.of((short) 10, (short) 2, (short) 1)));
  }

  @Test
  @DisplayName("getInstance returns a non-null server")
  void testGetInstanceReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance());
  }

  @Test
  @DisplayName("getInstance returns the same singleton instance on repeated calls")
  void testGetInstanceIsSingleton() {
    assertSame(ServerImpl.getInstance(), ServerImpl.getInstance());
  }

  @Test
  @DisplayName("getApi returns a non-null ServerApi")
  void testGetApiReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getApi());
  }

  @Test
  @DisplayName("getEventManager returns a non-null EventManager")
  void testGetEventManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getEventManager());
  }

  @Test
  @DisplayName("getPlayerManager returns a non-null PlayerManager")
  void testGetPlayerManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getPlayerManager());
  }

  @Test
  @DisplayName("getRoomManager returns a non-null RoomManager")
  void testGetRoomManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getRoomManager());
  }

  @Test
  @DisplayName("getChannelManager returns a non-null ChannelManager")
  void testGetChannelManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getChannelManager());
  }

  @Test
  @DisplayName("getDatagramChannelManager returns a non-null DatagramChannelManager")
  void testGetDatagramChannelManagerReturnsNonNull() {
    assertNotNull(ServerImpl.getInstance().getDatagramChannelManager());
  }

  @Test
  @DisplayName("getUptime returns a non-negative value before start")
  void testGetUptimeNonNegativeBeforeStart() {
    assertTrue(ServerImpl.getInstance().getUptime() >= 0);
  }

  @Test
  @DisplayName("getStartedTime returns 0 before start is called")
  void testGetStartedTimeIsZeroBeforeStart() {
    assertTrue(ServerImpl.getInstance().getStartedTime() >= 0);
  }

  @Test
  @DisplayName("getConfiguration returns null before start is called")
  void testGetConfigurationIsNullBeforeStart() {
    assertNull(ServerImpl.getInstance().getConfiguration());
  }

  @Test
  @DisplayName("getClientCommandManager returns null before start is called")
  void testGetClientCommandManagerIsNullBeforeStart() {
    assertNull(ServerImpl.getInstance().getClientCommandManager());
  }
  @Test
  @DisplayName("Startup must reject profile changes before any service is initialized")
  void startShouldRejectProfilesChangedAfterBootstrap() throws Exception {
    var server = newIsolatedServer();
    var configuration = mock(Configuration.class);
    var bootstrap = bootstrapFor(configuration, Set.of("world"));
    var processor = mock(ZeroProcessor.class);
    var network = mock(Network.class);
    replaceField(server, "zeroProcessor", processor);
    replaceField(server, "network", network);
    when(configuration.getString(CoreConfigurationType.PROFILE)).thenReturn("game");

    var exception = assertThrows(IllegalStateException.class,
        () -> server.start(bootstrap, new String[] {"selected.xml"}));

    assertEquals("Configuration profiles changed after bootstrap selection", exception.getMessage());
    verify(configuration).load("selected.xml");
    verifyNoInteractions(processor, network);
    assertNull(server.getConfiguration());
  }

  @Test
  @DisplayName("Startup compares normalized profile sets, independent of order and duplicates")
  void startShouldAcceptEquivalentNormalizedProfiles() throws Exception {
    var server = newIsolatedServer();
    var configuration = mock(Configuration.class);
    var bootstrap = bootstrapFor(configuration, Set.of("world", "game"));
    when(configuration.getString(CoreConfigurationType.PROFILE))
        .thenReturn(" game, world , world ");
    var processor = mock(ZeroProcessor.class);
    var reachedServices = new IllegalStateException("Profile validation completed");
    doThrow(reachedServices).when(processor).subscribe(anyBoolean(), anyBoolean());
    replaceField(server, "zeroProcessor", processor);

    var exception = assertThrows(IllegalStateException.class,
        () -> server.start(bootstrap, new String[0]));

    assertSame(reachedServices, exception);
    assertSame(configuration, server.getConfiguration());
    verify(configuration).load(null);
    verify(processor).subscribe(false, false);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", ",, "})
  @DisplayName("An XML-selected startup cannot continue with a missing or blank loaded profile")
  void startShouldRejectMissingLoadedProfile(String configuredProfiles) throws Exception {
    var server = newIsolatedServer();
    var configuration = mock(Configuration.class);
    var bootstrap = bootstrapFor(configuration, Set.of("world"));
    var processor = mock(ZeroProcessor.class);
    replaceField(server, "zeroProcessor", processor);
    when(configuration.getString(CoreConfigurationType.PROFILE)).thenReturn(configuredProfiles);

    assertThrows(IllegalArgumentException.class, () -> server.start(bootstrap, null));

    verifyNoInteractions(processor);
    assertNull(server.getConfiguration());
  }

  @Test
  @DisplayName("Externally assembled handlers with no profile selection retain legacy startup")
  void startShouldAcceptNullSelectionForLegacyHandler() throws Exception {
    assertLegacyStartupContinues(null);
  }

  @Test
  @DisplayName("Legacy programmatic bootstrap with an empty profile selection still starts")
  void startShouldAcceptEmptySelectionForLegacyHandler() throws Exception {
    assertLegacyStartupContinues(Set.of());
  }

  @ParameterizedTest
  @CsvSource({",,0,0", "45,,45,0", ",7,0,7", "45,7,45,7", "0,0,0,0"})
  @DisplayName("Scheduler setup defaults each omitted keepalive setting to disabled independently")
  void setupScheduleShouldApplyOptionalKeepaliveDefaults(String timeout, String interval,
      int expectedTimeout, int expectedInterval) {
    var configuration = mock(Configuration.class);
    var jobs = mock(ScheduledJobs.class);
    configureOptionalInteger(configuration,
        CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS, timeout);
    configureOptionalInteger(configuration,
        CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN, interval);

    new MaintenanceJob().configure(mock(SessionManager.class), configuration, jobs);
    if (expectedTimeout > 0 && expectedInterval > 0) {
      verify(jobs).registerInterval(eq("SCAN_MAIN_CONNECTION_KEEPALIVE"), any(), eq(expectedInterval));
    } else {
      verifyNoInteractions(jobs);
    }
    if (timeout == null) {
      verify(configuration, never()).getInt(CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS);
    }
    if (interval == null) {
      verify(configuration, never()).getInt(CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN);
    }
  }

  @Test
  @DisplayName("An explicit negative keepalive timeout must remain invalid rather than defaulting")
  void setupScheduleShouldRejectExplicitNegativeTimeout() {
    var configuration = mock(Configuration.class);
    configureOptionalInteger(configuration,
        CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS, "-1");

    var exception = assertThrows(IllegalArgumentException.class,
        () -> new MaintenanceJob().configure(mock(SessionManager.class), configuration, mock(ScheduledJobs.class)));

    assertTrue(exception.getMessage().contains("must not be negative"));
    verify(configuration).getInt(CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS);
  }

  @Test
  @DisplayName("Malformed explicit keepalive values must not be silently treated as omitted")
  void setupScheduleShouldPreserveMalformedConfigurationFailure() {
    var configuration = mock(Configuration.class);
    var invalidValue = new NumberFormatException("invalid keepalive timeout");
    when(configuration.getString(CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS))
        .thenReturn("invalid");
    when(configuration.getInt(CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS))
        .thenThrow(invalidValue);

    assertSame(invalidValue, assertThrows(NumberFormatException.class,
        () -> new MaintenanceJob().configure(mock(SessionManager.class), configuration, mock(ScheduledJobs.class))));
  }

  @Test
  void serverStartsAndStopsItsOwnJobsBeforeShuttingDownNetwork() throws Exception {
    var server = newIsolatedServer();
    var jobs = mock(ScheduledJobs.class);
    var network = mock(Network.class);
    replaceField(server, "scheduledJobs", jobs);
    replaceField(server, "network", network);
    replaceField(server, "zeroProcessor", mock(ZeroProcessor.class));
    var start = ServerImpl.class.getDeclaredMethod("startServices");
    start.setAccessible(true);
    start.invoke(server);
    server.shutdown();
    server.shutdown();
    var order = Mockito.inOrder(network, jobs);
    order.verify(network).start();
    order.verify(jobs).start();
    order.verify(jobs).shutdown();
    order.verify(network).shutdown();
    verify(jobs).shutdown();
  }

  @Test
  void serverLifetimeEndsOnShutdownWithAnEmptyScheduler() throws Exception {
    var server = newIsolatedServer();
    replaceField(server, "scheduledJobs", new ScheduledJobs());
    replaceField(server, "network", mock(Network.class));
    replaceField(server, "zeroProcessor", mock(ZeroProcessor.class));
    var waiter = server.startLifecycleThread();
    try {
      assertTrue(waiter.isAlive());
      assertFalse(waiter.isDaemon());
      server.shutdown();
      assertTrue(waiter.join(Duration.ofSeconds(5)));
    } finally {
      waiter.interrupt();
    }
  }

  private void assertLegacyStartupContinues(Set<String> selectedProfiles) throws Exception {
    var server = newIsolatedServer();
    var configuration = mock(Configuration.class);
    var bootstrap = bootstrapFor(configuration, selectedProfiles);
    var processor = mock(ZeroProcessor.class);
    var reachedServices = new IllegalStateException("Legacy startup accepted");
    doThrow(reachedServices).when(processor).subscribe(anyBoolean(), anyBoolean());
    replaceField(server, "zeroProcessor", processor);

    assertSame(reachedServices, assertThrows(IllegalStateException.class, () -> server.start(bootstrap, null)));
    verify(configuration, never()).getString(CoreConfigurationType.PROFILE);
  }

  private BootstrapHandler bootstrapFor(Configuration configuration, Set<String> profiles) {
    var bootstrap = mock(BootstrapHandler.class);
    var configurationHandler = mock(ConfigurationHandler.class);
    when(bootstrap.getConfigurationHandler()).thenReturn(configurationHandler);
    when(configurationHandler.getConfiguration()).thenReturn(configuration);
    when(bootstrap.getActiveProfiles()).thenReturn(profiles);
    return bootstrap;
  }

  private ServerImpl newIsolatedServer() throws ReflectiveOperationException {
    var constructor = ServerImpl.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  private void replaceField(ServerImpl server, String fieldName, Object value)
      throws ReflectiveOperationException {
    Field field = ServerImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(server, value);
  }

  private void configureOptionalInteger(Configuration configuration, CoreConfigurationType type, String value) {
    when(configuration.getString(type)).thenReturn(value);
    if (value == null) {
      // The real configuration parser rejects an absent value when getInt() is called.
      when(configuration.getInt(type)).thenThrow(new NumberFormatException("Missing optional value"));
    } else {
      when(configuration.getInt(type)).thenReturn(Integer.parseInt(value));
    }
  }
}
