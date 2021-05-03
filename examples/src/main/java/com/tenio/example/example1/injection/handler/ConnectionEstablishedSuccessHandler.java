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
package com.tenio.example.example1.injection.handler;

import com.tenio.common.element.CommonObject;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventConnectionEstablishedSuccess;
import com.tenio.core.network.IConnection;
import com.tenio.example.example1.PlayerLogin;

/**
 * @author kong
 */
@ExtComponent
public final class ConnectionEstablishedSuccessHandler extends AbstractExtensionHandler
		implements IEventConnectionEstablishedSuccess {

	@Override
	public void handle(IConnection connection, CommonObject message) {
		// Allow the connection login into server (become a player)
		var playerName = message.getString("u");
		// Should confirm that credentials by data from database or other services, here
		// is only for testing
		_playerApi.login(new PlayerLogin(playerName), connection);
	}

}