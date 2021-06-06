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
package com.tenio.examples.example7.handlers;

import com.tenio.common.bootstrap.annotations.Component;
import com.tenio.common.utilities.MathUtility;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.defines.results.PlayerLoggedinResult;
import com.tenio.core.extension.AbstractExtension;
import com.tenio.core.extension.events.EventPlayerLoggedinResult;
import com.tenio.examples.example7.constant.Example7Constant;

@Component
public final class PlayerLoggedinHandler extends AbstractExtension implements EventPlayerLoggedinResult {

	@Override
	public void handle(Player player, PlayerLoggedinResult result) {
		if (result == PlayerLoggedinResult.SUCCESS) {
			// player.setIgnoreTimeout(true);
			// create the initial position for player
			int x = MathUtility.randInt(100, 400);
			int y = MathUtility.randInt(100, 400);
			player.setProperty(Example7Constant.PLAYER_POSITION_X, x);
			player.setProperty(Example7Constant.PLAYER_POSITION_Y, y);

			api().joinRoom(player, api().getRoomById(0));
		}
	}

}
