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

package com.tenio.core.scheduler.job;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.Scheduled;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.scheduler.ScheduledJobs;
import com.tenio.core.scheduler.task.AutoCleanOrphanSessionTask;
import com.tenio.core.scheduler.task.AutoDisconnectPlayerTask;
import com.tenio.core.scheduler.task.AutoRemoveRoomTask;
import com.tenio.core.scheduler.task.AutoScanAliveMainConnectionTask;
import com.tenio.core.server.ServerImpl;

/**
 * Performs regular room, player and orphan-session cleanup.
 */
@Component
public final class MaintenanceJob {

    private static final int INITIAL_DELAY_SECONDS = 60;
    private static final int REMOVE_EMPTY_ROOMS_INTERVAL_SECONDS = 30;
    private static final int DISCONNECT_IDLE_PLAYERS_INTERVAL_SECONDS = 45;
    private static final int CLEAN_ORPHAN_SESSIONS_INTERVAL_SECONDS = 45;

    private final AutoRemoveRoomTask emptyRooms = new AutoRemoveRoomTask();
    private final AutoDisconnectPlayerTask idlePlayers = new AutoDisconnectPlayerTask();
    private final AutoCleanOrphanSessionTask orphanSessions = new AutoCleanOrphanSessionTask();

    private static final String KEEPALIVE_TASK_NAME = "SCAN_MAIN_CONNECTION_KEEPALIVE";
    private final AutoScanAliveMainConnectionTask keepalive = new AutoScanAliveMainConnectionTask();

    public void configure(SessionManager sessions, Configuration configuration, ScheduledJobs jobs) {
        var server = ServerImpl.getInstance();
        emptyRooms.setRoomManager(server.getRoomManager());
        idlePlayers.setPlayerManager(server.getPlayerManager());
        orphanSessions.setSessionManager(sessions);
        keepalive.setPlayerManager(server.getPlayerManager());
        int timeout = optionalSeconds(configuration, CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS);
        int interval = optionalSeconds(configuration, CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN);
        keepalive.setTimeoutSeconds(timeout);
        if (timeout > 0 && interval > 0) {
            jobs.registerInterval(KEEPALIVE_TASK_NAME, () -> keepalive.scan(System.currentTimeMillis()), interval);
        }
    }

    private static int optionalSeconds(Configuration configuration, CoreConfigurationType type) {
        return configuration.getString(type) == null ? 0 : configuration.getInt(type);
    }

    @Scheduled(threadName = "REMOVE_EMPTY_ROOMS", intervalSeconds = REMOVE_EMPTY_ROOMS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void emptyRooms() {
        emptyRooms.scan();
    }

    @Scheduled(threadName = "DISCONNECT_IDLE_PLAYERS", intervalSeconds = DISCONNECT_IDLE_PLAYERS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void idlePlayers() {
        idlePlayers.scan();
    }

    @Scheduled(threadName = "CLEAN_ORPHAN_SESSIONS", intervalSeconds = CLEAN_ORPHAN_SESSIONS_INTERVAL_SECONDS,
            initialDelaySeconds = INITIAL_DELAY_SECONDS)
    public void orphanSessions() {
        orphanSessions.scan();
    }
}
