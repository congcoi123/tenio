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

package com.tenio.core.bootstrap;

import static com.tenio.core.fixture.bootstrap.BootstrapBeans.CLIENT_COMMAND_CODE;
import static com.tenio.core.fixture.bootstrap.BootstrapBeans.CONTROLLER_PATH;
import static com.tenio.core.fixture.bootstrap.BootstrapBeans.SERVLET_PATH;
import static com.tenio.core.fixture.bootstrap.BootstrapBeans.SYSTEM_COMMAND_LABEL;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.configuration.ConfigurationHandler;
import com.tenio.core.event.handler.EventHandler;
import com.tenio.core.exception.NoImplementedClassFoundException;
import com.tenio.core.fixture.bootstrap.BootstrapApplication;
import com.tenio.core.fixture.bootstrap.BootstrapBeans.BootstrapClientCommand;
import com.tenio.core.fixture.bootstrap.BootstrapBeans.BootstrapRestController;
import com.tenio.core.fixture.bootstrap.BootstrapBeans.BootstrapSystemCommand;
import jakarta.servlet.http.HttpServlet;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Bootstrapper")
class BootstrapperTest {

  private static final String EVENT_HANDLERS = "com.tenio.core.event.handler";
  private final Bootstrapper bootstrapper = new Bootstrapper();

  private static class NotBootstrap {
  }

  @Test
  @DisplayName("Constructed bootstrappers should own independent application contexts")
  void constructedBootstrappersShouldOwnIndependentContexts() throws Exception {
    bootstrapper.runWithProfiles(BootstrapApplication.class, Set.of("world", "monitor"), EVENT_HANDLERS);
    var first = snapshot(bootstrapper);

    var other = new Bootstrapper();
    assertNull(other.getBootstrapHandler());
    assertContextUnchanged(bootstrapper, first);
    other.runWithProfiles(BootstrapApplication.class, Set.of("game", "monitor"), EVENT_HANDLERS);
    var second = snapshot(other);

    assertAll(
        () -> assertNotSame(first.handler(), second.handler()),
        () -> assertNotSame(first.eventHandler(), second.eventHandler()),
        () -> assertNotSame(first.configurationHandler(), second.configurationHandler()),
        () -> assertNotSame(first.clientCommand(), second.clientCommand()),
        () -> assertNotSame(first.systemCommand(), second.systemCommand()),
        () -> assertNotSame(first.servlet(), second.servlet()),
        () -> assertNotSame(first.handler().getClientCommandManager(),
            second.handler().getClientCommandManager()),
        () -> assertNotSame(first.handler().getSystemCommandManager(),
            second.handler().getSystemCommandManager()),
        () -> assertNotSame(first.handler().getServletMap(), second.handler().getServletMap()));
    assertContextUnchanged(bootstrapper, first);
    assertContextUnchanged(other, second);
  }

  @Test
  @DisplayName("run() with a class not annotated with @Bootstrap should not initialize handler")
  void runWithNonBootstrapClassShouldNotInitHandler() {
    assertDoesNotThrow(() -> bootstrapper.run(NotBootstrap.class));
    assertNull(bootstrapper.getBootstrapHandler());
  }

  @Test
  @DisplayName("run() should expose one fully wired context through its handler")
  void runWithBootstrapClassShouldInitHandler() {
    assertDoesNotThrow(() -> bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS));

    var handler = bootstrapper.getBootstrapHandler();
    assertFullyWired(handler);
    assertEquals(Set.of(), handler.getActiveProfiles());
  }

  @Test
  @DisplayName("getBootstrapHandler() before run() should return null")
  void getBootstrapHandlerBeforeRunShouldReturnNull() {
    assertNull(bootstrapper.getBootstrapHandler());
  }

  @Test
  @DisplayName("A failed scan should clear registrations and permit a fresh successful run")
  void runShouldPropagateMissingDependencyAndRecover() throws Exception {
    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);

    // Omitting the event package deliberately removes the required EventHandler provider.
    var exception = assertThrows(NoImplementedClassFoundException.class,
        () -> bootstrapper.run(BootstrapApplication.class));

    assertTrue(exception.getMessage().contains(EventHandler.class.getName()));
    assertNull(bootstrapper.getBootstrapHandler());
    assertRegistrationsCleared(previous.handler());

    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    assertFreshGeneration(previous, bootstrapper.getBootstrapHandler());
  }

  @Test
  @DisplayName("Matching profiles should start the entry point and retain an immutable selection")
  void runWithProfilesShouldSelectEntryPointAndDetachProfiles() throws Exception {
    var profiles = new HashSet<>(Set.of(" world "));
    bootstrapper.runWithProfiles(BootstrapApplication.WorldApplication.class,
        profiles, EVENT_HANDLERS);
    profiles.add("game");

    var handler = bootstrapper.getBootstrapHandler();
    assertFullyWired(handler);
    assertEquals(Set.of("world"), handler.getActiveProfiles());
    assertThrows(UnsupportedOperationException.class,
        () -> handler.getActiveProfiles().add("game"));
  }

  @Test
  @DisplayName("An excluded entry point should clear the prior bootstrap generation")
  void runWithExcludedProfileShouldClearPreviousBeans() throws Exception {
    bootstrapper.runWithProfiles(BootstrapApplication.WorldApplication.class,
        Set.of("world"), EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);

    bootstrapper.runWithProfiles(BootstrapApplication.WorldApplication.class,
        Set.of("game"), EVENT_HANDLERS);

    assertNull(bootstrapper.getBootstrapHandler());
    assertRegistrationsCleared(previous.handler());
    assertEquals(Set.of("world"), previous.handler().getActiveProfiles());
  }

  @Test
  @DisplayName("A non-bootstrap entry point should clear the prior handler and registrations")
  void nonBootstrapEntryShouldClearPreviousBeans() throws Exception {
    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);

    bootstrapper.run(NotBootstrap.class);

    assertNull(bootstrapper.getBootstrapHandler());
    assertRegistrationsCleared(previous.handler());
  }

  @Test
  @DisplayName("Legacy run() should reset profiles and construct a fresh generation")
  void runShouldResetProfilesAndCachedHandler() throws Exception {
    bootstrapper.runWithProfiles(BootstrapApplication.class, Set.of("world"), EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);

    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);

    var handler = bootstrapper.getBootstrapHandler();
    assertFreshGeneration(previous, handler);
    assertEquals(Set.of(), handler.getActiveProfiles());
    assertEquals(Set.of("world"), previous.handler().getActiveProfiles());
  }

  @Test
  @DisplayName("Resetting one bootstrapper should leave another context's registrations intact")
  void resettingOneBootstrapperShouldLeaveAnotherContextIntact() throws Exception {
    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);
    var other = startOtherContext();
    var unaffected = snapshot(other);

    bootstrapper.run(NotBootstrap.class);

    assertNull(bootstrapper.getBootstrapHandler());
    assertRegistrationsCleared(previous.handler());
    assertContextUnchanged(other, unaffected);
  }

  @Test
  @DisplayName("A failed scan and recovery should not change another bootstrapper's context")
  void failedScanAndRecoveryShouldLeaveAnotherContextIntact() throws Exception {
    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);
    var other = startOtherContext();
    var unaffected = snapshot(other);

    assertThrows(NoImplementedClassFoundException.class,
        () -> bootstrapper.run(BootstrapApplication.class));

    assertNull(bootstrapper.getBootstrapHandler());
    assertRegistrationsCleared(previous.handler());
    assertContextUnchanged(other, unaffected);

    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);
    assertFreshGeneration(previous, bootstrapper.getBootstrapHandler());
    assertContextUnchanged(other, unaffected);
  }

  @Test
  @DisplayName("Rescanning one bootstrapper should preserve the other context's object identities")
  void rescanningOneBootstrapperShouldLeaveAnotherContextIntact() throws Exception {
    bootstrapper.runWithProfiles(BootstrapApplication.class, Set.of("world"), EVENT_HANDLERS);
    var previous = snapshot(bootstrapper);
    var other = startOtherContext();
    var unaffected = snapshot(other);

    bootstrapper.run(BootstrapApplication.class, EVENT_HANDLERS);

    assertFreshGeneration(previous, bootstrapper.getBootstrapHandler());
    assertEquals(Set.of(), bootstrapper.getBootstrapHandler().getActiveProfiles());
    assertContextUnchanged(other, unaffected);
  }

  private Bootstrapper startOtherContext() throws Exception {
    var other = new Bootstrapper();
    other.runWithProfiles(BootstrapApplication.class, Set.of("game"), EVENT_HANDLERS);
    return other;
  }

  private static void assertFullyWired(BootstrapHandler handler) {
    assertNotNull(handler);
    assertNotNull(handler.getEventHandler());
    assertNotNull(handler.getConfigurationHandler());
    assertNotNull(handler.getClientCommandManager());
    assertNotNull(handler.getServletMap());
    var client = assertInstanceOf(BootstrapClientCommand.class,
        handler.getClientCommandManager().getHandler(CLIENT_COMMAND_CODE));
    var manager = handler.getSystemCommandManager();
    assertNotNull(manager);
    var system = assertInstanceOf(BootstrapSystemCommand.class,
        manager.getHandler(SYSTEM_COMMAND_LABEL));
    assertSame(manager, system.getCommandManager());
    assertSame(handler.getEventHandler(), system.eventHandler);
    assertEquals(Set.of(SYSTEM_COMMAND_LABEL), manager.getHandlers().keySet());
    assertEquals(Set.of(SYSTEM_COMMAND_LABEL), manager.getAnnotations().keySet());
    var controller = handler.getBeanByClazz(BootstrapRestController.class, CONTROLLER_PATH);
    assertNotNull(controller);
    assertNotNull(handler.getServletMap().get(SERVLET_PATH));
    assertAll(
        () -> assertSame(handler, handler.getBeanByClazz(BootstrapHandler.class)),
        () -> assertSame(handler.getEventHandler(), handler.getBeanByClazz(EventHandler.class)),
        () -> assertSame(handler.getConfigurationHandler(),
            handler.getBeanByClazz(ConfigurationHandler.class)),
        () -> assertSame(handler.getClientCommandManager(), client.getCommandManager()),
        () -> assertSame(handler.getEventHandler(), client.eventHandler),
        () -> assertSame(handler.getEventHandler(), controller.eventHandler),
        () -> assertSame(controller.status(), handler.getServletMap().get(SERVLET_PATH)),
        () -> assertEquals(Set.of(CLIENT_COMMAND_CODE),
            handler.getClientCommandManager().getHandlers().keySet()),
        () -> assertEquals(Set.of(SERVLET_PATH), handler.getServletMap().keySet()));
  }

  private static void assertRegistrationsCleared(BootstrapHandler previous) {
    // These public views point to the previously populated context, not an unrelated Injector.
    assertAll(
        () -> assertNull(previous.getBeanByClazz(BootstrapHandler.class)),
        () -> assertNull(previous.getBeanByClazz(EventHandler.class)),
        () -> assertNull(previous.getBeanByClazz(ConfigurationHandler.class)),
        () -> assertNull(previous.getClientCommandManager().getHandler(CLIENT_COMMAND_CODE)),
        () -> assertNull(previous.getBeanByClazz(BootstrapRestController.class, CONTROLLER_PATH)),
        () -> assertTrue(previous.getClientCommandManager().getHandlers().isEmpty()),
        () -> assertTrue(previous.getServletMap().isEmpty()));
  }

  private static ContextSnapshot snapshot(Bootstrapper owner) {
    var handler = owner.getBootstrapHandler();
    assertFullyWired(handler);
    return new ContextSnapshot(handler, handler.getEventHandler(),
        handler.getConfigurationHandler(),
        (BootstrapClientCommand) handler.getClientCommandManager().getHandler(CLIENT_COMMAND_CODE),
        systemCommand(handler),
        handler.getServletMap().get(SERVLET_PATH), handler.getActiveProfiles());
  }

  private static void assertFreshGeneration(ContextSnapshot previous, BootstrapHandler handler) {
    assertFullyWired(handler);
    // Capture objects before rescanning: a prior handler's registry views remain live.
    assertAll(
        () -> assertNotSame(previous.handler(), handler),
        () -> assertNotSame(previous.eventHandler(), handler.getEventHandler()),
        () -> assertNotSame(previous.configurationHandler(), handler.getConfigurationHandler()),
        () -> assertNotSame(previous.clientCommand(),
            handler.getClientCommandManager().getHandler(CLIENT_COMMAND_CODE)),
        () -> assertNotSame(previous.servlet(), handler.getServletMap().get(SERVLET_PATH)));
  }

  private static void assertContextUnchanged(Bootstrapper owner, ContextSnapshot expected) {
    var handler = owner.getBootstrapHandler();
    assertSame(expected.handler(), handler);
    assertFullyWired(handler);
    assertAll(
        () -> assertSame(expected.eventHandler(), handler.getBeanByClazz(EventHandler.class)),
        () -> assertSame(expected.configurationHandler(),
            handler.getBeanByClazz(ConfigurationHandler.class)),
        () -> assertSame(expected.clientCommand(),
            handler.getClientCommandManager().getHandler(CLIENT_COMMAND_CODE)),
        () -> assertSame(expected.systemCommand(), systemCommand(handler)),
        () -> assertSame(expected.servlet(), handler.getServletMap().get(SERVLET_PATH)),
        () -> assertEquals(expected.profiles(), handler.getActiveProfiles()));
  }

  private static BootstrapSystemCommand systemCommand(BootstrapHandler handler) {
    return (BootstrapSystemCommand) handler.getSystemCommandManager().getHandler(SYSTEM_COMMAND_LABEL);
  }

  private record ContextSnapshot(BootstrapHandler handler, EventHandler eventHandler,
                                 ConfigurationHandler configurationHandler,
                                 BootstrapClientCommand clientCommand,
                                 BootstrapSystemCommand systemCommand, HttpServlet servlet,
                                 Set<String> profiles) {
  }
}
