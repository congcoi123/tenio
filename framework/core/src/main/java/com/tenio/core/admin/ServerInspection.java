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

import com.tenio.core.entity.Player;
import com.tenio.core.entity.Room;
import com.tenio.core.network.Network;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.server.Server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only views. Only an explicit pressure query scans the session snapshot.
 */
public final class ServerInspection {

    private static final int MAX_ROWS = 50;
    private static final DateTimeFormatter EVENT_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS XXX", Locale.ROOT);
    private final Server server;
    private final Network network;
    private final boolean queueDiagnostics;

    public ServerInspection(Server server, Network network) {
        this(server, network, false);
    }

    public ServerInspection(Server server, Network network, boolean queueDiagnostics) {
        this.server = server;
        this.network = network;
        this.queueDiagnostics = queueDiagnostics;
    }

    public OperationalHistory history() {
        return network.getSessionManager().getOperationalHistory();
    }

    public InspectionResult query(String input) {
        input = AdminCommands.normalize(input);
        boolean fullList = AdminCommands.opensFullList(input);
        if (fullList) {
            input = input.substring(0, input.length() - " all".length());
        }
        int rowLimit = fullList ? Integer.MAX_VALUE : MAX_ROWS;
        String[] words = input.split(" ");
        if (input.equals("help")) {
            return AdminCommands.help();
        }
        var rows = new Rows();
        var sessions = network.getSessionManager();
        if (fullList && input.equals("info player")) {
            var players = server.getPlayerManager().getSnapshotPlayersList();
            players.forEach(player -> rows.add("Player", player.getIdentity()));
            rows.add("Players", players.size());
        } else if (fullList && input.equals("info room")) {
            var rooms = server.getRoomManager().getSnapshotRoomsList();
            rooms.forEach(room -> rows.add("Room " + room.getId(), room.getName()));
            rows.add("Rooms", rooms.size());
        } else if (words[0].equals("session") || words[0].equals("room")) {
            if (words.length != 2) {
                return InspectionResult.message(words[0].equals("room") ? "Usage: room <id> [all]" : "Usage: session <id>");
            }
            long id;
            try {
                id = Long.parseLong(words[1]);
            } catch (NumberFormatException invalid) {
                return InspectionResult.message("Expected a numeric ID");
            }
            if (words[0].equals("room")) {
                var room = server.getRoomManager().getRoomById(id);
                if (room == null) {
                    return InspectionResult.message("Room no longer exists");
                }
                rows.add("Room ID", room.getId());
                rows.add("Name", room.getName());
                rows.add("Owner", room.getOwner().map(Player::getIdentity).orElse(""));
                rows.add("Players", room.getSnapshotPlayerCount());
                rows.add("Capacity", room.getCapacity());
                var members = room.getSnapshotPlayersList();
                members.stream().limit(rowLimit).forEach(player -> rows.add("Member", player.getIdentity()));
                if (members.size() > rowLimit) {
                    rows.add("Members shown", shown(rowLimit, members.size(), "members", input + " all"));
                }
            } else {
                var session = sessions.getSessionById(id);
                if (session == null) {
                    return InspectionResult.message("Session no longer exists");
                }
                rows.add("Session ID", id);
                var name = session.getName();
                var player = name == null ? null : server.getPlayerManager().getPlayerByIdentity(name);
                if (player != null && player.getSession().orElse(null) == session) {
                    rows.add("Player", name);
                    rows.add("Room ID", player.getCurrentRoom().map(Room::getId).orElse(null));
                }
                rows.add("Transport", session.getTransportType());
                rows.add("UDP attached", session.containsUdp());
                if (session.containsUdp()) {
                    rows.add("Last UDP read age (ms)", "unavailable");
                }
                rows.add("Last read age (ms)", age(session.getLastReadTime()));
                if (session.isTcp()) {
                    rows.add("Last reliable TCP read age (ms)", age(session.getLastReliableReadTime()));
                }
                rows.add("Last write age (ms)", age(session.getLastWriteTime()));
                if (queueDiagnostics) {
                    addQueueDetails(rows, session);
                }
            }
        } else if (input.equals("stats") || input.equals("server stats")) {
            rows.add("Uptime", formatUptime(server.getUptime()));
            rows.add("Players", server.getPlayerManager().getSnapshotPlayerCount());
            rows.add("Rooms", server.getRoomManager().getSnapshotRoomCount());
            rows.add("Sessions", sessions.getSnapshotSessionCount());
            var reader = network.getNetworkReaderStatistic();
            rows.add("Bytes read", reader.getReadBytes());
            rows.add("Packets read", reader.getReadPackets());
            rows.add("Inbound drops", reader.getReadDroppedPackets());
            var writer = network.getNetworkWriterStatistic();
            rows.add("Bytes written", writer.getWrittenBytes());
            rows.add("Packets written", writer.getWrittenPackets());
            rows.add("Outbound packets dropped due to congestion", writer.getWrittenDroppedPacketsByFull());
            rows.add("Outbound packets rejected by queue policy", writer.getWrittenDroppedPacketsByPolicy());
        } else if (input.equals("network pressure")) {
            if (!queueDiagnostics) {
                return InspectionResult.message("Network pressure requires the monitor profile.");
            }
            addNetworkPressure(rows, sessions.getSnapshotSessionsList(), rowLimit);
        } else if (input.equals("limits")) {
            var commands = server.getClientCommandManager();
            if (commands == null || !commands.hasLimits()) {
                return InspectionResult.message("Client command limits are disabled.");
            }
            var counts = commands.limitCounts();
            counts.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .limit(rowLimit).forEach(e -> rows.add("Command " + commands.describeCommand(e.getKey(), false),
                            e.getValue().accepted() + " accepted, " + e.getValue().rejected() + " rejected"));
            if (counts.size() > rowLimit) {
                rows.add("Counters shown", shown(rowLimit, counts.size(), "counters", "limits all"));
            }
        } else if (words[0].equals("history")) {
            if (words.length > 2) {
                return InspectionResult.message("Usage: history [id] [all]");
            }
            Long sessionId;
            try {
                sessionId = words.length == 2 ? Long.valueOf(words[1]) : null;
            } catch (NumberFormatException invalid) {
                return InspectionResult.message("Expected a numeric session ID");
            }
            var history = sessions.getOperationalHistory();
            if (history == null) {
                return InspectionResult.message("History is disabled. Add history to the configuration profile (for example, monitor,history) and restart TenIO.");
            }
            if (history.file() != null) {
                return InspectionResult.message("History is written to " + history.file().toAbsolutePath());
            }
            var events = history.snapshot().reversed().stream()
                    .filter(event -> sessionId == null || sessionId == event.sessionId())
                    .toList();
            if (events.isEmpty()) {
                rows.add("History", sessionId == null ? "No events recorded yet."
                        : "No events recorded for session #" + sessionId + ".");
            }
            var time = EVENT_TIME.withZone(ZoneId.systemDefault());
            for (var event : events.subList(0, Math.min(events.size(), rowLimit))) {
                String label = time.format(Instant.ofEpochMilli(event.timestamp())) + " | Session #" + event.sessionId();
                String description = switch (event.kind()) {
                    case DISCONNECT -> "Disconnected";
                    case RECONNECT -> "Reconnect attempt";
                    case PRESSURE_START -> "Outbound queue under pressure";
                    case PRESSURE_END -> "Outbound queue recovered";
                };
                String reason = "EXCEPTION".equals(event.reason()) ? "Connection error"
                        : Objects.toString(event.reason(), "").replace('_', ' ').toLowerCase(Locale.ROOT);
                String detail = description + " (" + event.transport() + ")";
                rows.add(label, reason.isBlank() ? detail : detail + " — " + reason);
            }
            if (events.size() > rowLimit) {
                rows.add("Events shown", shown(rowLimit, events.size(), "events", input + " all"));
            }
            long dropped = history.droppedExports();
            if (dropped > 0) {
                rows.add("Events lost during export", dropped);
            }
        } else {
            return InspectionResult.message("Commands: " + AdminCommands.shortcuts());
        }
        return rows.result();
    }

    private static void addQueueDetails(Rows rows, Session session) {
        var inbound = session.inboundQueueStatistics();
        rows.add("Inbound depth", inbound == null ? "unavailable" : inbound.depth());
        rows.add("Inbound capacity", inbound == null ? "unavailable"
                : inbound.capacity() <= 0 ? "unlimited" : inbound.capacity());
        rows.add("Inbound messages rejected (queue full)", inbound == null ? "unavailable" : inbound.fullRejections());

        var queue = session.fetchOutboundQueue();
        var outbound = queue == null ? null : queue.statistics();
        rows.add("Outbound depth", outbound == null ? "unavailable" : outbound.depth());
        rows.add("Outbound capacity", outbound == null || outbound.capacity() < 0
                ? "unavailable" : outbound.capacity() == Integer.MAX_VALUE ? "unlimited" : outbound.capacity());
        rows.add("Outbound high-water mark", outbound == null || outbound.highWater() < 0
                ? "unavailable" : outbound.highWater());
        rows.add("Outbound packets dropped (queue full)", outbound == null || outbound.fullDrops() < 0
                ? "unavailable" : outbound.fullDrops());
        rows.add("Outbound packets dropped (queue policy)", outbound == null || outbound.policyDrops() < 0
                ? "unavailable" : outbound.policyDrops());
    }

    private static void addNetworkPressure(Rows rows, List<Session> sessions, int rowLimit) {
        int inboundCount = 0;
        int outboundCount = 0;
        for (Session session : sessions) {
            var inbound = session.inboundQueueStatistics();
            if (inbound != null && underPressure(inbound.depth(), inbound.capacity())) {
                inboundCount++;
                if (rows.size() < rowLimit) {
                    rows.add("Session " + session.getId() + " inbound", inbound.depth() + "/" + inbound.capacity());
                }
            }
            var queue = session.fetchOutboundQueue();
            if (queue != null) {
                var outbound = queue.statistics();
                if (outbound.capacity() != Integer.MAX_VALUE && underPressure(outbound.depth(), outbound.capacity())) {
                    outboundCount++;
                    if (rows.size() < rowLimit) {
                        rows.add("Session " + session.getId() + " outbound", outbound.depth() + "/" + outbound.capacity()
                                + " for " + outbound.pressureAgeMillis() + " ms");
                    }
                }
            }
        }
        rows.add("Matching queues", shown(rows.size(), (long) inboundCount + outboundCount,
                "matching queue rows", "network pressure all"));
        rows.add("Sessions under inbound pressure", inboundCount);
        rows.add("Sessions under outbound pressure", outboundCount);
    }

    private static String shown(int displayed, long total, String unit, String fullCommand) {
        String summary = String.format(Locale.US, "Showing %,d of %,d %s", displayed, total, unit);
        return displayed < total ? summary + ". Use " + fullCommand + " for the full table." : summary;
    }

    private static boolean underPressure(int depth, int capacity) {
        return capacity > 0 && depth * 100L >= capacity * 80L;
    }

    static String formatUptime(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 5) {
            return "just now";
        }
        if (seconds < 60) {
            return duration(seconds, "second");
        }
        if (seconds < 3600) {
            return duration(seconds / 60, "minute");
        }
        if (seconds < 86400) {
            return duration(seconds / 3600, "hour");
        }
        return duration(seconds / 86400, "day");
    }

    private static String duration(long amount, String unit) {
        return String.format(Locale.US, "%,d %s%s", amount, unit, amount == 1 ? "" : "s");
    }

    private static String age(long time) {
        return time <= 0 ? "unavailable" : Long.toString(Math.max(0, System.currentTimeMillis() - time));
    }

    private static final class Rows {

        private final List<String> labels = new ArrayList<>();
        private final List<String> values = new ArrayList<>();

        void add(String key, Object value) {
            labels.add(key);
            String text = switch (value) {
                case null -> "unavailable";
                case Integer count -> String.format(Locale.US, "%,d", count);
                case Long count -> String.format(Locale.US, "%,d", count);
                default -> value.toString();
            };
            values.add(text.length() > 240 ? text.substring(0, 240) : text);
        }

        int size() {
            return labels.size();
        }

        InspectionResult result() {
            return new InspectionResult(labels, values);
        }
    }
}
