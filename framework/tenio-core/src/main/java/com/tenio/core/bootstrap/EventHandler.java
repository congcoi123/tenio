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
package com.tenio.core.bootstrap;

import com.tenio.core.bootstrap.annotation.ExtAutowired;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.bootstrap.handler.ConnectionEventHandler;
import com.tenio.core.bootstrap.handler.HttpEventHandler;
import com.tenio.core.bootstrap.handler.MixinsEventHandler;
import com.tenio.core.bootstrap.handler.PlayerEventHandler;
import com.tenio.core.bootstrap.handler.RoomEventHandler;
import com.tenio.core.extension.AbstractExtensionHandler;

/**
 * @author kong
 */
@ExtComponent
public final class EventHandler extends AbstractExtensionHandler {

	@ExtAutowired
	private ConnectionEventHandler __connectionEventHandler;

	@ExtAutowired
	private PlayerEventHandler __playerEventHandler;

	@ExtAutowired
	private RoomEventHandler __roomEventHandler;

	@ExtAutowired
	private HttpEventHandler __httpEventHandler;

	@ExtAutowired
	private MixinsEventHandler __mixinsEventHandler;

	public void initialize() {

		__connectionEventHandler.initialize();
		__playerEventHandler.initialize();
		__roomEventHandler.initialize();
		__httpEventHandler.initialize();
		__mixinsEventHandler.initialize();

	}

}
