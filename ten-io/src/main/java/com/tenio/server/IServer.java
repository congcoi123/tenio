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
package com.tenio.server;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.entities.AbstractPlayer;
import com.tenio.entities.element.TObject;
import com.tenio.extension.IExtension;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set first and all
 * configuration values should be confirmed.
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
	 * When a message sent from a player, you can handle it here
	 * 
	 * @param player          the player who sent a message to the server
	 * @param isSubConnection if it is true, this is a sub-connection
	 * @param message         the message's content
	 */
	void handle(AbstractPlayer player, boolean isSubConnection, TObject message);

	/**
	 * Handle a player's exception
	 * 
	 * @param player the player who cause an exception
	 * @param cause  the exception's content
	 */
	void exception(AbstractPlayer player, Throwable cause);

	/**
	 * Handle a connection's exception
	 * 
	 * @param identify the "connection" id (channel's id, session's id, ...)
	 * @param cause    the exception's content
	 */
	void exception(String identify, Throwable cause);

}
