/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.server;

import com.tenio.api.HeartBeatApi;
import com.tenio.api.MessageApi;
import com.tenio.api.PlayerApi;
import com.tenio.api.RoomApi;
import com.tenio.api.TaskApi;
import com.tenio.configuration.BaseConfiguration;
import com.tenio.extension.IExtension;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 * 
 * @author kong
 * 
 */
interface IServer {

	/**
	 * Start the server base on your own configurations
	 * 
	 * @param configuration @see {@link BaseConfiguration}
	 */
	void start(BaseConfiguration configuration);

	/**
	 * Shut down the server and close all services
	 */
	void shutdown();

	/**
	 * @return Returns your own implemented extension
	 */
	IExtension getExtension();

	/**
	 * Set your own extension for handling your own logic in-game
	 * 
	 * @param extension your own logic handling @see {@link IExtension}
	 */
	void setExtension(IExtension extension);

	/**
	 * @return Returns @see {@link PlayerApi}
	 */
	PlayerApi getPlayerApi();

	/**
	 * @return Returns @see {@link RoomApi}
	 */
	RoomApi getRoomApi();

	/**
	 * @return Returns @see {@link MessageApi}
	 */
	MessageApi getMessageApi();

	/**
	 * @return Returns @see {@link HeartBeatApi}
	 */
	HeartBeatApi getHeartBeatApi();

	/**
	 * @return Returns @see {@link TaskApi}
	 */
	TaskApi getTaskApi();

}
