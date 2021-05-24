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

import com.tenio.core.bootstrap.annotations.Autowired;
import com.tenio.core.bootstrap.annotations.Component;
import com.tenio.core.bootstrap.handlers.ConnectionEventHandler;
import com.tenio.core.bootstrap.handlers.HttpEventHandler;
import com.tenio.core.bootstrap.handlers.MixinsEventHandler;
import com.tenio.core.bootstrap.handlers.PlayerEventHandler;
import com.tenio.core.bootstrap.handlers.RoomEventHandler;
import com.tenio.core.extension.AbstractExtension;

/**
 * @author kong
 */
@Component
//TODO: Add description
public final class EventHandler extends AbstractExtension {

	@Autowired
	private ConnectionEventHandler __connectionEventHandler;

	@Autowired
	private PlayerEventHandler __playerEventHandler;

	@Autowired
	private RoomEventHandler __roomEventHandler;

	@Autowired
	private HttpEventHandler __httpEventHandler;

	@Autowired
	private MixinsEventHandler __mixinsEventHandler;

	public void initialize() {

		__connectionEventHandler.initialize();
		__playerEventHandler.initialize();
		__roomEventHandler.initialize();
		__httpEventHandler.initialize();
		__mixinsEventHandler.initialize();

	}

}
