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
package com.tenio.examples.example4.handlers;

import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.defines.results.AttachedConnectionResult;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventAttachedConnectionResult;
import com.tenio.examples.server.SharedEventKey;
import com.tenio.examples.server.UdpEstablishedState;

@Component
public final class AttachedConnectionHandler extends AbstractExtension implements EventAttachedConnectionResult {

	@Override
	public void handle(Player player, AttachedConnectionResult result) {
		if (result == AttachedConnectionResult.SUCCESS) {
			var data = object().putByte(SharedEventKey.KEY_ALLOW_TO_ATTACH, UdpEstablishedState.ATTACHED);

			response().setContent(data.toBinary()).setRecipient(player).write();
		}
	}

}
