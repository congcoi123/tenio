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

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.data.element.CommonObjectArray;
import com.tenio.core.bootstrap.annotation.ExtComponent;
import com.tenio.core.entity.ZeroPlayer;
import com.tenio.core.entity.backup.EntityProcesser;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.IEventPlayerLoginedSuccess;
import com.tenio.example.example1.PlayerLogin;

/**
 * @author kong
 */
@ExtComponent
public final class PlayerLoginSuccessHandler extends AbstractExtensionHandler implements IEventPlayerLoginedSuccess {

	@Override
	public void handle(ZeroPlayer iplayer) {
		var player = (PlayerLogin) iplayer;

		try {
			_info("PLAYER BACKUP", EntityProcesser.exportToJSON(player));
		} catch (Exception e) {
			_error(e, player.getName());
		}

		// Now you can send messages to the client
		// Sending, the data need to be packed
		var data = _messageApi.getMessageObjectArray();
		_messageApi.sendToPlayer(player, PlayerLogin.MAIN_CHANNEL, "c", "message", "d", data.put("H").put("3").put("L")
				.put("O").put(true).put(CommonObjectArray.newInstance().put("Sub").put("Value").put(100)));

		// Attempt to send internal message
		_messageApi.sendToInternalServer(player, 1,
				CommonObject.newInstance().add("internal", "this is a message in external server"));
	}

}
