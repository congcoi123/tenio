/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.task.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.schedule.ITask;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.event.IEventManager;

/**
 * For a player which is in IDLE mode, that means for a long time without
 * receiving or sending any data from the server or from a client. This task
 * will scan those IDLE players in period time and force them to log out. Those
 * players got a "timeout" error.
 * 
 * @author kong
 * 
 */
public final class TimeOutScanTask extends AbstractLogger implements ITask {

	private final IEventManager __eventManager;
	private final PlayerApi __playerApi;
	/**
	 * The removable list of players
	 */
	private final List<IPlayer> __removeablePlayers;
	/**
	 * After a number of seconds without any message from the client. It can be
	 * configured in your configurations, see {@link CoreConfiguration}
	 */
	private final int __idleReader;
	/**
	 * After a number of seconds without any message is sent to the client. It can
	 * be configured in your configurations, see {@link CoreConfiguration}
	 */
	private final int __idleWriter;
	/**
	 * The period time for scanning IDLE players and log out those
	 */
	private final int __timeoutScanPeriod;

	public TimeOutScanTask(IEventManager eventManager, PlayerApi playerApi, int idleReader, int idleWriter,
			int timeoutScanPeriod) {
		__eventManager = eventManager;
		__playerApi = playerApi;
		__idleReader = idleReader;
		__idleWriter = idleWriter;
		__timeoutScanPeriod = timeoutScanPeriod;
		__removeablePlayers = new ArrayList<IPlayer>();
	}

	@Override
	public ScheduledFuture<?> run() {
		_info("TIME OUT TASK", "Running ...");
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			long currentTime = System.currentTimeMillis();

			for (var player : __playerApi.gets().values()) {
				// normal state, can check time out
				if (!player.isIgnoredTimeout()) {
					long writerTime = player.getWriterTime();
					if (currentTime - writerTime >= (__idleWriter * 1000)) { // check writer time first
						__removeablePlayers.add(player);
						continue;
					} else { // check reader time
						long readerTime = player.getReaderTime();
						if (currentTime - readerTime >= (__idleReader * 1000)) {
							__removeablePlayers.add(player);
						}
					}
				}
			}

			__removeablePlayers.forEach((player) -> {
				__eventManager.getExtension().emit(ExtEvent.PLAYER_GOT_TIMEOUT, player);
				__playerApi.logOut(player);
			});

			__removeablePlayers.clear();

		}, 0, __timeoutScanPeriod, TimeUnit.SECONDS);
	}

}
