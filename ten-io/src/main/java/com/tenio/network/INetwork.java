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
package com.tenio.network;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.event.IEventManager;

/**
 * A network is one of the most important parts of this server for the main
 * purpose of communication. This class help create a new network instance by
 * your own configurations, see {@link BaseConfiguration}.
 * 
 * @author kong
 * 
 */
public interface INetwork {

	/**
	 * Start a new network for communication
	 * 
	 * @param eventManager  the system event management
	 * @param configuration your own configuration
	 * 
	 * @return <b>true</b> if the network can start normally, <b>false</b> otherwise
	 */
	boolean start(IEventManager eventManager, BaseConfiguration configuration);

	/**
	 * Shutdown the network
	 */
	void shutdown();

}
