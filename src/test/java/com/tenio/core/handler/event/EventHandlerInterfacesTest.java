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

package com.tenio.core.handler.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.data.DataCollection;
import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.entity.define.mode.PlayerDisconnectMode;
import com.tenio.core.entity.define.mode.PlayerLeaveRoomMode;
import com.tenio.core.entity.define.mode.RoomRemoveMode;
import com.tenio.core.entity.define.result.ConnectionEstablishedResult;
import com.tenio.core.entity.define.result.PlayerJoinedRoomResult;
import com.tenio.core.exception.RefusedConnectionAddressException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;
import io.netty.channel.Channel;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Unit Test Cases For EventHandler Interfaces")
class EventHandlerInterfacesTest {

  @Test
  @DisplayName("Test EventReceivedMessageFromPlayer")
  void testEventReceivedMessageFromPlayer() {
    EventReceivedMessageFromPlayer<Player, DataCollection> handler = (player, message) -> {
    };
    handler.handle(Mockito.mock(Player.class), Mockito.mock(DataCollection.class));
  }

  @Test
  @DisplayName("Test EventSendMessageToPlayer")
  void testEventSendMessageToPlayer() {
    EventSendMessageToPlayer<Player, DataCollection> handler = (player, message) -> {
    };
    handler.handle(Mockito.mock(Player.class), Mockito.mock(DataCollection.class));
  }

  @Test
  @DisplayName("Test EventServerInitialization")
  void testEventServerInitialization() {
    EventServerInitialization handler = (serverName, config) -> {
    };
    handler.handle("server", Mockito.mock(com.tenio.common.configuration.Configuration.class));
  }

  @Test
  @DisplayName("Test EventDisconnectPlayer")
  void testEventDisconnectPlayer() {
    EventDisconnectPlayer<Player> handler = (player, mode) -> {
    };
    handler.handle(Mockito.mock(Player.class), PlayerDisconnectMode.IDLE);
  }

  @Test
  @DisplayName("Test EventWriteMessageToConnection")
  void testEventWriteMessageToConnection() {
    EventWriteMessageToConnection handler = (session, packet) -> {
    };
    handler.handle(Mockito.mock(Session.class), Mockito.mock(Packet.class));
  }

  @Test
  @DisplayName("Test EventServerException")
  void testEventServerException() {
    EventServerException handler = throwable -> {
    };
    handler.handle(new Exception("test"));
  }

  @Test
  @DisplayName("Test EventBroadcastToChannel")
  void testEventBroadcastToChannel() {
    EventBroadcastToChannel<Player, DataCollection> handler = (channel, player, message) -> {
    };
    handler.handle(Mockito.mock(com.tenio.core.entity.Channel.class), Mockito.mock(Player.class),
        Mockito.mock(DataCollection.class));
  }

  @Test
  @DisplayName("Test EventRoomWillBeRemoved")
  void testEventRoomWillBeRemoved() {
    EventRoomWillBeRemoved<Room> handler = (room, mode) -> {
    };
    handler.handle(Mockito.mock(Room.class), RoomRemoveMode.WHEN_EMPTY);
  }

  @Test
  @DisplayName("Test EventPlayerLogin")
  void testEventPlayerLogin() {
    EventPlayerLogin<Player> handler = player -> {
    };
    handler.handle(Mockito.mock(Player.class));
  }

  @Test
  @DisplayName("Test EventPlayerSubscribedChannel")
  void testEventPlayerSubscribedChannel() {
    EventPlayerSubscribedChannel<Player> handler = (channel, player) -> {
    };
    handler.handle(Mockito.mock(com.tenio.core.entity.Channel.class), Mockito.mock(Player.class));
  }

  @Test
  @DisplayName("Test EventChannelCreated")
  void testEventChannelCreated() {
    EventChannelCreated handler = channel -> {
    };
    handler.handle(Mockito.mock(com.tenio.core.entity.Channel.class));
  }

  @Test
  @DisplayName("Test EventServerTeardown")
  void testEventServerTeardown() {
    EventServerTeardown handler = serverName -> {
    };
    handler.handle("server");
  }

  @Test
  @DisplayName("Test EventFetchedCcuInfo")
  void testEventFetchedCcuInfo() {
    EventFetchedCcuInfo handler = numberPlayers -> {
    };
    handler.handle(42);
  }

  @Test
  @DisplayName("Test EventChannelWillBeRemoved")
  void testEventChannelWillBeRemoved() {
    EventChannelWillBeRemoved handler = channel -> {
    };
    handler.handle(Mockito.mock(com.tenio.core.entity.Channel.class));
  }

  @Test
  @DisplayName("Test EventPlayerJoinedRoomResult")
  void testEventPlayerJoinedRoomResult() {
    EventPlayerJoinedRoomResult<Player, Room> handler = (player, room, result) -> {
    };
    handler.handle(Mockito.mock(Player.class), Mockito.mock(Room.class),
        PlayerJoinedRoomResult.SUCCESS);
  }

  @Test
  @DisplayName("Test EventFetchedBandwidthInfo")
  void testEventFetchedBandwidthInfo() {
    EventFetchedBandwidthInfo handler =
        (readBytes, readPackets, readDroppedPackets, writtenBytes, writtenPackets, writtenDroppedPacketsByPolicy, writtenDroppedPacketsByFull) -> {
        };
    handler.handle(1L, 2L, 3L, 4L, 5L, 6L, 7L);
  }

  @Test
  @DisplayName("Test EventPlayerReconnectRequestHandling")
  void testEventPlayerReconnectRequestHandling() {
    EventPlayerReconnectRequestHandling<Player, DataCollection> handler =
        (session, message) -> Optional.empty();
    handler.handle(Mockito.mock(Session.class), Mockito.mock(DataCollection.class));
    assertTrue(true);
  }

  @Test
  @DisplayName("Test EventPlayerBeforeLeaveRoom")
  void testEventPlayerBeforeLeaveRoom() {
    EventPlayerBeforeLeaveRoom<Player, Room> handler = (player, room, mode) -> {
    };
    handler.handle(Mockito.mock(Player.class), Mockito.mock(Room.class),
        PlayerLeaveRoomMode.SESSION_CLOSED);
  }

  @Test
  @DisplayName("Test EventSocketConnectionRefused")
  void testEventSocketConnectionRefused() {
    EventSocketConnectionRefused handler = (channel, exception) -> {
    };
    handler.handle(Mockito.mock(SocketChannel.class),
        Mockito.mock(RefusedConnectionAddressException.class));
  }

  @Test
  @DisplayName("Test EventConnectionEstablishedResult")
  void testEventConnectionEstablishedResult() {
    EventConnectionEstablishedResult<DataCollection> handler = (session, message, result) -> {
    };
    handler.handle(Mockito.mock(Session.class), Mockito.mock(DataCollection.class),
        ConnectionEstablishedResult.SUCCESS);
  }

  @Test
  @DisplayName("Test EventPlayerUnsubscribedChannel")
  void testEventPlayerUnsubscribedChannel() {
    EventPlayerUnsubscribedChannel<Player> handler = (channel, player) -> {
    };
    handler.handle(Mockito.mock(com.tenio.core.entity.Channel.class), Mockito.mock(Player.class));
  }

  @Test
  @DisplayName("Test EventWebSocketConnectionRefused")
  void testEventWebSocketConnectionRefused() {
    EventWebSocketConnectionRefused handler = (channel, exception) -> {
    };
    handler.handle(Mockito.mock(Channel.class),
        Mockito.mock(RefusedConnectionAddressException.class));
  }
}
