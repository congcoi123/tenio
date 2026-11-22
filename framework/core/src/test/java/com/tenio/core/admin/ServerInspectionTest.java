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

package com.tenio.core.admin;

import com.tenio.core.entity.Room;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.network.Network;
import com.tenio.core.network.entity.outbound.packet.OutboundQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.server.Server;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ServerInspectionTest {

    @Test
    void pollingStatsUsesCountersWithoutWalkingEntities() {
        var server = mock(Server.class);
        var network = mock(Network.class);
        var sessions = mock(SessionManager.class);
        var rooms = mock(RoomManager.class);
        var players = mock(PlayerManager.class);
        when(server.getPlayerManager()).thenReturn(players);
        when(server.getRoomManager()).thenReturn(rooms);
        when(network.getSessionManager()).thenReturn(sessions);
        var reader = NetworkReaderStatistic.newInstance();
        reader.updateReadBytes(120302);
        when(network.getNetworkReaderStatistic()).thenReturn(reader);
        when(server.getUptime()).thenReturn(120000L);
        when(rooms.getSnapshotRoomCount()).thenReturn(1203);
        var writer = NetworkWriterStatistic.newInstance();
        writer.updateWrittenDroppedPacketsByFull(12);
        writer.updateWrittenDroppedPacketsByPolicy(3);
        when(network.getNetworkWriterStatistic()).thenReturn(writer);
        when(sessions.getSnapshotSessionCount()).thenReturn(23);

        var result = new ServerInspection(server, network).query("stats");
        assertEquals("23", result.values().get(result.labels().indexOf("Sessions")));
        assertEquals("1,203", result.values().get(result.labels().indexOf("Rooms")));
        assertEquals("120,302", result.values().get(result.labels().indexOf("Bytes read")));
        assertEquals("2 minutes", result.values().get(result.labels().indexOf("Uptime")));
        assertEquals("12", result.values().get(result.labels().indexOf("Outbound packets dropped due to congestion")));
        assertEquals("3", result.values().get(result.labels().indexOf("Outbound packets rejected by queue policy")));
        verify(sessions, never()).getSnapshotSessionsList();
        verify(rooms, never()).getSnapshotRoomsList();
        verify(players, never()).getSnapshotPlayersList();
    }

    @Test
    void missingSessionAndUnknownCommandsAreReadOnly() {
        var server = mock(Server.class);
        var network = mock(Network.class);
        var sessions = mock(SessionManager.class);
        when(network.getSessionManager()).thenReturn(sessions);
        var inspection = new ServerInspection(server, network);

        assertEquals(InspectionResult.message("Session no longer exists"), inspection.query("session 7"));
        assertTrue(inspection.query("server stop").values().getFirst().startsWith("Commands"));
        assertTrue(inspection.query("history 7").values().getFirst().contains("restart TenIO"));
        assertEquals(InspectionResult.message("Network pressure requires the monitor profile."),
                inspection.query("network pressure"));
        verify(sessions, never()).getSnapshotSessionsList();
        verifyNoInteractions(server);
    }

    @Test
    void pinnedCommandsHaveHelpAndRunThroughInspection() {
        var server = mock(Server.class);
        var network = mock(Network.class);
        var sessions = mock(SessionManager.class);
        var rooms = mock(RoomManager.class);
        var session = mock(Session.class);
        var queue = mock(OutboundQueue.class);
        var room = mock(Room.class);
        try (var history = new OperationalHistory(4)) {
            when(network.getSessionManager()).thenReturn(sessions);
            when(server.getRoomManager()).thenReturn(rooms);
            when(rooms.getRoomById(42)).thenReturn(room);
            when(room.getOwner()).thenReturn(Optional.empty());
            when(room.getSnapshotPlayersList()).thenReturn(List.of());
            when(sessions.getSessionById(7)).thenReturn(session);
            when(sessions.getSnapshotSessionsList()).thenReturn(List.of(session));
            when(session.getId()).thenReturn(7L);
            when(session.inboundQueueStatistics()).thenReturn(new Session.InboundQueueStatistics(8, 10, 3));
            when(session.fetchOutboundQueue()).thenReturn(queue);
            when(queue.statistics()).thenReturn(new OutboundQueue.Statistics(8, 10, 8, 0, 0, 12));
            when(sessions.getOperationalHistory()).thenReturn(history);
            history.record(7, "TCP", OperationalHistory.Kind.DISCONNECT, "EXCEPTION");
            history.record(8, "TCP", OperationalHistory.Kind.RECONNECT, "resumed");

            var inspection = new ServerInspection(server, network, true);
            assertEquals("help | stats | room <id> | session <id> | network pressure | limits | history [id] | clear",
                    AdminCommands.shortcuts());
            assertEquals(List.of("stats", "room <id>", "session <id>", "network pressure", "limits",
                    "history [id]", "clear"), inspection.query("help").labels());
            assertTrue(inspection.query("ROOM  42").labels().contains("Room ID"));
            assertEquals(inspection.query("room 42"), inspection.query("room 42 all"));
            assertEquals(inspection.query("history"), inspection.query("history all"));
            assertEquals(inspection.query("history 7"), inspection.query("history 7 all"));
            var details = inspection.query("session 7");
            assertEquals("3", details.values().get(details.labels().indexOf("Inbound messages rejected (queue full)")));
            assertEquals("0", details.values().get(details.labels().indexOf("Outbound packets dropped (queue full)")));
            assertEquals("0", details.values().get(details.labels().indexOf("Outbound packets dropped (queue policy)")));
            var pressure = inspection.query("network pressure");
            assertEquals(List.of("Session 7 inbound", "Session 7 outbound", "Matching queues", "Sessions under inbound pressure",
                    "Sessions under outbound pressure"), pressure.labels());
            assertEquals(List.of("8/10", "8/10 for 12 ms", "Showing 2 of 2 matching queue rows", "1", "1"), pressure.values());
            assertEquals(2, inspection.query("history").labels().size());
            var result = inspection.query("history 7");
            assertTrue(result.labels().getFirst().matches(
                    "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3} (Z|[+-]\\d{2}:\\d{2}) \\| Session #7"));
            assertEquals(List.of("Disconnected (TCP) — Connection error"), result.values());
            assertEquals(List.of("No events recorded for session #99."), inspection.query("history 99").values());
        }
    }

    @Test
    void fullPressureListIncludesEveryQueueAndDefaultReportsItsCap() {
        var server = mock(Server.class);
        var network = mock(Network.class);
        var sessions = mock(SessionManager.class);
        var session = mock(Session.class);
        when(network.getSessionManager()).thenReturn(sessions);
        when(sessions.getSnapshotSessionsList()).thenReturn(java.util.Collections.nCopies(51, session));
        when(session.inboundQueueStatistics()).thenReturn(new Session.InboundQueueStatistics(8, 10, 0));

        var inspection = new ServerInspection(server, network, true);
        var limited = inspection.query("network pressure");
        var full = inspection.query("network pressure all");

        assertEquals(53, limited.labels().size());
        assertTrue(limited.values().get(50).startsWith("Showing 50 of 51 matching queue rows"));
        assertEquals(54, full.labels().size());
        assertEquals("Showing 51 of 51 matching queue rows", full.values().get(51));
    }

    @Test
    void uptimeUsesReadableUnits() {
        assertEquals("just now", ServerInspection.formatUptime(1));
        assertEquals("1 minute", ServerInspection.formatUptime(60000));
        assertEquals("2 hours", ServerInspection.formatUptime(7200000));
        assertEquals("1 day", ServerInspection.formatUptime(86400000));
    }
}
