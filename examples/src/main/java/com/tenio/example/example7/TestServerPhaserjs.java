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
package com.tenio.example.example7;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObjectArray;
import com.tenio.common.utility.MathUtility;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.TestConfiguration;

/**
 * This class shows how a server handle 1000 players and communications
 * 
 * @author kong
 *
 */
public final class TestServerPhaserjs extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] params) {
		var game = new TestServerPhaserjs();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public TestConfiguration getConfiguration() {
		return new TestConfiguration("TenIOConfig.xml");
	}

	@Override
	public void onStarted(IExtension extension, Configuration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		RoomPhaserjs phaserjsRoom = new RoomPhaserjs("phaserjs", "Phaserjs", 3);

		@Override
		public void initialize(Configuration configuration) {
			_on(ExtensionEvent.CONNECTION_ESTABLISHED_SUCCESS, params -> {
				var connection = _getConnection(params[0]);
				var message = _getCommonObject(params[1]);

				// Allow the connection login into server (become a player)
				var playerName = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerPhaserjs(playerName), connection);

				return null;
			});

			_on(ExtensionEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// The player has login successful
				var player = (PlayerPhaserjs) _getPlayer(params[0]);
				player.setIgnoreTimeout(true);
				// create the initial position
				int x = MathUtility.randInt(100, 400);
				int y = MathUtility.randInt(100, 400);
				player.setPosition(x, y);

				_playerApi.makePlayerJoinRoom(phaserjsRoom, player);

				return null;
			});

			_on(ExtensionEvent.PLAYER_JOIN_ROOM_HANDLE, params -> {
				var room = (RoomPhaserjs) _getRoom(params[1]);

				var pack = _messageApi.getMessageObjectArray();
				var players = room.getPlayers();
				for (var p : players.values()) {
					var pjs = (PlayerPhaserjs) p;
					var data = CommonObjectArray.newInstance();
					data.add(pjs.getName());
					data.add(pjs.getX());
					data.add(pjs.getY());
					pack.add(data);
				}
				_messageApi.sendToRoom(room, PlayerPhaserjs.MAIN_CHANNEL, "c", "i", "d", pack);

				return null;
			});

			_on(ExtensionEvent.RECEIVED_MESSAGE_FROM_PLAYER, params -> {
				var player = (PlayerPhaserjs) _getPlayer(params[0]);
				var message = _getCommonObject(params[2]);
				var move = message.getMessageObjectArray("d");

				player.setPosition(move.getInt(0), move.getInt(1));

				var pack = _messageApi.getMessageObjectArray();
				pack.add(player.getName());
				pack.add(move.get(0));
				pack.add(move.get(1));
				_messageApi.sendToRoom(phaserjsRoom, PlayerPhaserjs.MAIN_CHANNEL, "c", "m", "d", pack);

				return null;
			});

			_on(ExtensionEvent.FETCHED_CCU_NUMBER, params -> {
				var ccu = _getInteger(params[0]);

				_info("FETCHED_CCU_INFO", ccu);

				return null;
			});

		}

	}

}
