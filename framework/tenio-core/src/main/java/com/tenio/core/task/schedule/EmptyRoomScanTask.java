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
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.common.task.schedule.ITask;
import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.core.entity.IRoom;

/**
 * To remove the empty room (a room without any players) in period time. You can
 * configure this time in your own configurations, see {@link CoreConfiguration}
 * 
 * @author kong
 * 
 */
public final class EmptyRoomScanTask extends AbstractLogger implements ITask {

	private final RoomApi __roomApi;
	/**
	 * The current list of rooms
	 */
	private final Map<String, IRoom> __rooms;
	/**
	 * Removable list of rooms
	 */
	private final List<IRoom> __removableRooms;
	/**
	 * The period time for scanning empty rooms and delete those
	 */
	private final int __emptyRoomScanPeriod;

	public EmptyRoomScanTask(RoomApi roomApi, int emptyRoomScanPeriod) {
		__roomApi = roomApi;
		__emptyRoomScanPeriod = emptyRoomScanPeriod;
		__rooms = __roomApi.gets();
		__removableRooms = new ArrayList<IRoom>();
	}

	@Override
	public ScheduledFuture<?> run() {
		_info("EMPTY ROOM TASK", "Running ...");
		return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

			__rooms.forEach((key, value) -> {
				if (value.isEmpty()) {
					synchronized (__rooms) {
						__removableRooms.add(value);
					}
				}
			});

			__removableRooms.forEach((room) -> {
				__roomApi.remove(room);
			});

			__removableRooms.clear();

		}, 0, __emptyRoomScanPeriod, TimeUnit.SECONDS);
	}

}
