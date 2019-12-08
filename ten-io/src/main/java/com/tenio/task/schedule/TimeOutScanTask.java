/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.task.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.api.PlayerApi;
import com.tenio.configuration.BaseConfiguration;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.AbstractPlayer;
import com.tenio.event.EventManager;
import com.tenio.logger.AbstractLogger;

/**
 * For a player which is in IDLE mode, that means for a long time without
 * receiving or sending any data from the server or from a client. This task
 * will scan those IDLE players in period time and force them to log out. Those
 * players got a "timeout" error.
 * 
 * @author kong
 * 
 */
public final class TimeOutScanTask extends AbstractLogger {

	/**
	 * @see EventManager
	 */
	private EventManager __events = EventManager.getInstance();
	/**
	 * @see PlayerApi
	 */
	private PlayerApi __playerApi = PlayerApi.getInstance();
	/**
	 * The removable list of players
	 */
	private List<AbstractPlayer> __removeables = new ArrayList<AbstractPlayer>();
	/**
	 * After a number of seconds without any message from the client. It can be
	 * configured in your configurations @see {@link BaseConfiguration}
	 */
	private int __idleReader;
	/**
	 * After a number of seconds without any message is sent to the client. It can
	 * be configured in your configurations @see {@link BaseConfiguration}
	 */
	private int __idleWriter;
	/**
	 * The period time for scanning IDLE players and log out those
	 */
	private int __timeoutScanPeriod;

	public TimeOutScanTask(int idleReader, int idleWriter, int timeoutScanPeriod) {
		__idleReader = idleReader;
		__idleWriter = idleWriter;
		__timeoutScanPeriod = timeoutScanPeriod;
	}

	public void run() {
		info("TIME OUT TASK", "Running ...");
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			long currentTime = System.currentTimeMillis();

			__playerApi.gets().forEach((key, value) -> {
				// normal state, can check time out
				if (!value.isIgnoreTimeout()) {
					long writerTime = value.getWriterTime();
					if (currentTime - writerTime >= (__idleWriter * 1000)) { // check writer time first
						__removeables.add(value);
						return;
					} else { // check reader time
						long readerTime = value.getReaderTime();
						if (currentTime - readerTime >= (__idleReader * 1000)) {
							__removeables.add(value);
							return;
						}
					}
				}
			});

			__removeables.forEach((player) -> {
				__events.emit(TEvent.PLAYER_TIMEOUT, player);
				__playerApi.logOut(player);
			});

			__removeables.clear();

		}, 0, __timeoutScanPeriod, TimeUnit.SECONDS);
	}

}
