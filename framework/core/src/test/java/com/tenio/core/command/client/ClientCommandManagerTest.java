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

package com.tenio.core.command.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.tenio.common.data.DataCollection;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.entity.Player;
import com.tenio.core.exception.AddedDuplicatedClientCommandException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ClientCommandManager")
class ClientCommandManagerTest {

  private ClientCommandManager manager;

  @BeforeEach
  void setUp() {
    manager = new ClientCommandManager();
  }

  private AbstractClientCommandHandler<Player, DataCollection> stubHandler() {
    return new AbstractClientCommandHandler<>() {
      @Override
      public void execute(Player player, DataCollection message) {
      }
    };
  }

  @Test
  @DisplayName("registerCommand adds a handler that can be retrieved by code")
  void testRegisterCommandAndGetHandler() {
    var handler = stubHandler();
    manager.registerCommand((short) 1, handler);
    assertEquals(handler, manager.getHandler((short) 1));
  }

  @Test
  @DisplayName("registerCommand with duplicate code throws AddedDuplicatedClientCommandException")
  void testRegisterDuplicateCodeThrows() {
    manager.registerCommand((short) 1, stubHandler());
    assertThrows(AddedDuplicatedClientCommandException.class,
        () -> manager.registerCommand((short) 1, stubHandler()));
  }

  @Test
  @DisplayName("getHandler for unregistered code returns null")
  void testGetHandlerUnknownCodeReturnsNull() {
    assertNull(manager.getHandler((short) 99));
  }

  @Test
  @DisplayName("getHandlers returns all registered handlers")
  void testGetHandlersReturnsAllRegistered() {
    var h1 = stubHandler();
    var h2 = stubHandler();
    manager.registerCommand((short) 1, h1);
    manager.registerCommand((short) 2, h2);
    assertEquals(2, manager.getHandlers().size());
    assertTrue(manager.getHandlers().containsKey((short) 1));
    assertTrue(manager.getHandlers().containsKey((short) 2));
  }

  @Test
  @DisplayName("getHandlersAsList returns list of all registered handlers")
  void testGetHandlersAsListReturnsAll() {
    manager.registerCommand((short) 1, stubHandler());
    manager.registerCommand((short) 2, stubHandler());
    assertEquals(2, manager.getHandlersAsList().size());
  }

  @Test
  @DisplayName("invoke with registered code calls execute on the handler")
  void testInvokeCallsExecute() {
    var player = mock(Player.class);
    var message = mock(DataCollection.class);
    @SuppressWarnings("unchecked")
    AbstractClientCommandHandler<Player, DataCollection> handler =
        mock(AbstractClientCommandHandler.class);
    manager.registerCommand((short) 5, handler);
    manager.invoke((short) 5, player, message);
    verify(handler).execute(player, message);
  }

  @Test
  @DisplayName("invoke with unknown code does nothing")
  void testInvokeUnknownCodeDoesNothing() {
    var player = mock(Player.class);
    var message = mock(DataCollection.class);
    // should not throw
    manager.invoke((short) 99, player, message);
  }

  @Test
  @DisplayName("clear removes all registered handlers")
  void testClearEmptiesManager() {
    manager.registerCommand((short) 1, stubHandler());
    manager.registerCommand((short) 2, stubHandler());
    manager.clear();
    assertTrue(manager.getHandlers().isEmpty());
    assertNull(manager.getHandler((short) 1));
  }

  @Test
  @DisplayName("getHandlers returns empty map initially")
  void testInitiallyEmpty() {
    assertNotNull(manager.getHandlers());
    assertTrue(manager.getHandlers().isEmpty());
  }

  @Test
  void omittedRateLeavesCommandUnrestrictedEvenWithAnExplicitBurst() {
    var handler = new BurstOnlyCommand();
    var player = mock(Player.class);
    manager.registerCommand((short) 10, handler);
    manager.invoke((short) 10, player, null);
    manager.invoke((short) 10, player, null);

    assertEquals(2, handler.invocations);
    assertTrue(manager.limitCounts().isEmpty());
    org.mockito.Mockito.verifyNoInteractions(player);
  }

  @Test
  void omittedBurstUsesTheRateForBucketCapacity() {
    manager.registerCommand((short) 11, new RateOnlyCommand());
    assertTrue(manager.getLimits().allow((short) 11, "player"));
    assertEquals(new CommandLimits.Counts(1, 0), manager.limitCounts().get((short) 11));
  }

  @Test
  void describesNamedCommandsAndFallsBackToTheHandlerClassName() {
    manager.registerCommand((short) 10, new BurstOnlyCommand());
    manager.registerCommand((short) 11, new RateOnlyCommand());

    assertEquals("10 | BurstOnlyCommand", manager.describeCommand((short) 10, true));
    assertEquals("11 | join-room | Joins a room using an invite code", manager.describeCommand((short) 11, true));
    assertEquals("11 | join-room", manager.describeCommand((short) 11, false));

    manager.getHandlers().put((short) 11, new BurstOnlyCommand());
    assertEquals("11 | BurstOnlyCommand", manager.describeCommand((short) 11, true));
    manager.clear();
    assertEquals("11", manager.describeCommand((short) 11, true));
  }

  @ClientCommand(value = 10, burst = 2)
  private static final class BurstOnlyCommand extends AbstractClientCommandHandler<Player, DataCollection> {
    private int invocations;

    @Override
    public void execute(Player player, DataCollection data) {
      invocations++;
    }
  }

  @ClientCommand(value = 11, name = "join-room", description = "Joins a room using an invite code",
      requestsPerSecond = 2)
  private static final class RateOnlyCommand extends AbstractClientCommandHandler<Player, DataCollection> {
    @Override
    public void execute(Player player, DataCollection data) {
    }
  }

  @Test
  void onePlayersBurstDoesNotConsumeAnotherPlayersAllowance() {
    var first = mock(Player.class);
    var second = mock(Player.class);
    org.mockito.Mockito.when(first.getIdentity()).thenReturn("one");
    org.mockito.Mockito.when(second.getIdentity()).thenReturn("two");
    var message = mock(DataCollection.class);
    var handler = stubHandler();
    manager.registerCommand((short) 1, handler);
    manager.getLimits().configure((short) 1, 1, 1);
    manager.invoke((short) 1, first, message);
    manager.invoke((short) 1, first, message);
    manager.invoke((short) 1, second, message);
    assertEquals(new CommandLimits.Counts(2, 1), manager.getLimits().counts().get((short) 1));
  }
}
