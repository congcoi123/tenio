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
package com.tenio.engine.extension;

import com.tenio.common.extension.IExtension;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.engine.api.HeartBeatApi;
import com.tenio.engine.server.Engine;

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

	private Engine __server = Engine.getInstance();

	/**
	 * @see HeartBeatApi
	 */
	protected HeartBeatApi _heartbeatApi = __server.getHeartBeatApi();

}
