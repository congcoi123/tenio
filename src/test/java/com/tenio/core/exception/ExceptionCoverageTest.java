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

package com.tenio.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.common.data.DataCollection;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.entity.define.result.RoomCreatedResult;
import com.tenio.core.entity.define.result.SwitchedPlayerRoleInRoomResult;
import com.tenio.core.network.entity.packet.Packet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For Exceptions")
class ExceptionCoverageTest {

  @Test
  @DisplayName("Test PacketEncryptorException")
  void testPacketEncryptorException() {
    PacketEncryptorException ex = new PacketEncryptorException("encryption error");
    assertEquals("encryption error", ex.getMessage());
  }

  @Test
  @DisplayName("Test ConfigurationException")
  void testConfigurationException() {
    ConfigurationException ex = new ConfigurationException("config error");
    assertEquals("config error", ex.getMessage());
  }

  @Test
  @DisplayName("Test PacketCompressorException")
  void testPacketCompressorException() {
    PacketCompressorException ex = new PacketCompressorException("compress error");
    assertEquals("compress error", ex.getMessage());
  }

  @Test
  @DisplayName("Test ServiceRuntimeException")
  void testServiceRuntimeException() {
    ServiceRuntimeException ex = new ServiceRuntimeException("service error");
    assertEquals("service error", ex.getMessage());
  }

  @Test
  @DisplayName("Test NoImplementedClassFoundException")
  void testNoImplementedClassFoundException() {
    NoImplementedClassFoundException ex = new NoImplementedClassFoundException(String.class);
    assertTrue(ex.getMessage()
        .contains("Unable to find any implementation for the class: java.lang.String"));
  }

  @Test
  @DisplayName("Test NotDefinedSubscribersException")
  void testNotDefinedSubscribersException() {
    NotDefinedSubscribersException ex =
        new NotDefinedSubscribersException(String.class, Integer.class);
    assertTrue(ex.getMessage()
        .contains("Need to implement interfaces: java.lang.String, java.lang.Integer"));
  }

  @Test
  @DisplayName("Test ChannelNotExistException")
  void testChannelNotExistException() {
    ChannelNotExistException ex = new ChannelNotExistException();
    assertNull(ex.getMessage());
  }

  @Test
  @DisplayName("Test CreatedRoomException")
  void testCreatedRoomException() {
    CreatedRoomException ex =
        new CreatedRoomException("room error", RoomCreatedResult.INVALID_NAME_OR_PASSWORD);
    assertEquals("room error", ex.getMessage());
    assertEquals(RoomCreatedResult.INVALID_NAME_OR_PASSWORD, ex.getResult());
  }

  @Test
  @DisplayName("Test RefusedConnectionAddressException")
  void testRefusedConnectionAddressException() {
    RefusedConnectionAddressException ex =
        new RefusedConnectionAddressException("reason", "127.0.0.1");
    assertTrue(ex.getMessage().contains("reason : 127.0.0.1"));
  }

  @Test
  @DisplayName("Test AddedDuplicatedRoomException")
  void testAddedDuplicatedRoomException() {
    Room room = mock(Room.class);
    Mockito.when(room.toString()).thenReturn("room1");
    AddedDuplicatedRoomException ex = new AddedDuplicatedRoomException(room);
    assertTrue(ex.getMessage().contains("Unable to add room: room1, it already exists"));
  }

  @Test
  @DisplayName("Test PacketQueuePolicyViolationException")
  void testPacketQueuePolicyViolationException() {
    Packet packet = mock(Packet.class);
    Mockito.when(packet.toString()).thenReturn("packet1");
    PacketQueuePolicyViolationException ex = new PacketQueuePolicyViolationException(packet, 75.5f);
    assertTrue(ex.getMessage()
        .contains("Dropped packet: [packet1], current packet queue usage: 75.500000%"));
  }

  @Test
  @DisplayName("Test PlayerJoinedRoomException")
  void testPlayerJoinedRoomException() {
    PlayerJoinedRoomException ex =
        new PlayerJoinedRoomException("join error", PlayerJoinedRoomResult.DUPLICATED_PLAYER);
    assertEquals("join error", ex.getMessage());
    assertEquals(PlayerJoinedRoomResult.DUPLICATED_PLAYER, ex.getResult());
  }

  @Test
  @DisplayName("Test AddedDuplicatedPlayerException")
  void testAddedDuplicatedPlayerException() {
    Player player = mock(Player.class);
    Mockito.when(player.getIdentity()).thenReturn("player1");
    AddedDuplicatedPlayerException ex = new AddedDuplicatedPlayerException(player);
    assertTrue(ex.getMessage().contains("Unable to add player: player1, it already exists"));
    assertEquals(player, ex.getPlayer());
  }

  @Test
  @DisplayName("Test DuplicatedBeanCreationException")
  void testDuplicatedBeanCreationException() {
    DuplicatedBeanCreationException ex =
        new DuplicatedBeanCreationException(String.class, "beanName");
    assertTrue(
        ex.getMessage().contains("Duplicated bean creation with type: String, and name: beanName"));
  }

  @Test
  @DisplayName("Test CreatedDuplicatedChannelException")
  void testCreatedDuplicatedChannelException() {
    CreatedDuplicatedChannelException ex = new CreatedDuplicatedChannelException("channel1");
    assertTrue(
        ex.getMessage().contains("Unable to create channel with id: channel1, it already exists"));
  }

  @Test
  @DisplayName("Test RequestQueueFullException")
  void testRequestQueueFullException() {
    RequestQueueFullException ex = new RequestQueueFullException(10);
    assertTrue(ex.getMessage()
        .contains("Reached max queue size, the request was dropped. The current size: 10"));
  }

  @Test
  @DisplayName("Test SwitchedPlayerRoleInRoomException")
  void testSwitchedPlayerRoleInRoomException() {
    SwitchedPlayerRoleInRoomException ex = new SwitchedPlayerRoleInRoomException("switch error",
        SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM);
    assertEquals("switch error", ex.getMessage());
    assertEquals(SwitchedPlayerRoleInRoomResult.PLAYER_WAS_NOT_IN_ROOM, ex.getResult());
  }

  @Test
  @DisplayName("Test AddedDuplicatedClientCommandException")
  void testAddedDuplicatedClientCommandException() {
    AbstractClientCommandHandler<Player, DataCollection> handler =
        new AbstractClientCommandHandler<>() {

          @Override
          public void execute(Player player, DataCollection message) {
          }
        };
    AddedDuplicatedClientCommandException ex =
        new AddedDuplicatedClientCommandException((short) 1, handler);
    assertTrue(ex.getMessage().contains("Unable to add label {1}, it already exists"));
  }

  @Test
  @DisplayName("Test IllegalDefinedAccessControlException")
  void testIllegalDefinedAccessControlException() {
    IllegalDefinedAccessControlException ex = new IllegalDefinedAccessControlException();
    assertNull(ex.getMessage());
  }

  @Test
  @DisplayName("Test PacketQueueFullException")
  void testPacketQueueFullException() {
    PacketQueueFullException ex = new PacketQueueFullException(5);
    assertTrue(ex.getMessage()
        .contains("Reached max queue size, the packet was dropped. The current size: 5"));
  }

  @Test
  @DisplayName("Test InvalidRestMappingClassException")
  void testInvalidRestMappingClassException() {
    InvalidRestMappingClassException ex = new InvalidRestMappingClassException();
    assertTrue(ex.getMessage().contains(
        "Invalid RestMapping class, it should return an instance of jakarta.servlet.http.HttpServlet."));
  }
}
