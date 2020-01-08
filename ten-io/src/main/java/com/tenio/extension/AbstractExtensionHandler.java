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
package com.tenio.extension;

import com.tenio.api.HeartBeatApi;
import com.tenio.api.MessageApi;
import com.tenio.api.PlayerApi;
import com.tenio.api.RoomApi;
import com.tenio.api.TaskApi;
import com.tenio.configuration.constant.TEvent;
import com.tenio.event.EventManager;
import com.tenio.event.ISubscriber;
import com.tenio.logger.AbstractLogger;
import com.tenio.server.Server;

/**
 * This class provides you all the necessary APIs for your own logic game
 * handling. The entry point class must implement the {@link IExtension}
 * interface. After that, you can create your desired number of handled logic
 * classes. These logic instances in the entry point object will be handled from
 * up to bottom. It works like a chain, you try to add a new value into an
 * object in one first handler and in the last handler, that value can be
 * retrieved for another purpose. Notice that, one event can be handled multiple
 * times in different classes.
 * 
 * @author kong
 * 
 */
public abstract class AbstractExtensionHandler extends AbstractLogger {

	private Server __server = Server.getInstance();
	
	/**
	 * @see {@link MessageApi}
	 */
	protected MessageApi _messageApi = __server.getMessageApi();
	/**
	 * @see {@link PlayerApi}
	 */
	protected PlayerApi _playerApi = __server.getPlayerApi();
	/**
	 * @see {@link RoomApi}
	 */
	protected RoomApi _roomApi = __server.getRoomApi();
	/**
	 * @see {@link TaskApi}
	 */
	protected TaskApi _taskApi = __server.getTaskApi();
	/**
	 * @see {@link HeartBeatApi}
	 */
	protected HeartBeatApi _heartbeatApi = __server.getHeartBeatApi();

	/**
	 * Handle your own logic with the corresponding event type
	 * 
	 * @param type the type of this current event. All the supported type can be
	 *             found in {@link TEvent}
	 * @param sub  your own subscriber-class handler
	 */
	protected void on(final TEvent type, final ISubscriber sub) {
		EventManager.getEvent().on(type, sub);
	}

}
