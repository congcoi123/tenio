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
package com.tenio.example.example7;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObjectArray;
import com.tenio.common.utility.MathUtility;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

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
	public static void main(String[] args) {
		var game = new TestServerPhaserjs();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.xml");
	}

	@Override
	public void onStarted() {

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
		public void initialize(IConfiguration configuration) {
			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, args -> {
				try {
					var connection = _getConnection(args[0]);
					var message = _getCommonObject(args[1]);

					// Allow the connection login into server (become a player)
					String username = message.getString("u");
					// Should confirm that credentials by data from database or other services, here
					// is only for testing
					_playerApi.login(new PlayerPhaserjs(username), connection);
				} catch (ExtensionValueCastException e) {
					_error(e, e.getMessage());
				}

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, args -> {
				try {
					// The player has login successful
					var player = (PlayerPhaserjs) _getPlayer(args[0]);
					player.setIgnoreTimeout(true);
					// create the initial position
					int x = MathUtility.randInt(100, 400);
					int y = MathUtility.randInt(100, 400);
					player.setPosition(x, y);

					_playerApi.makePlayerJoinRoom(phaserjsRoom, player);
				} catch (ExtensionValueCastException e) {
					_error(e, e.getMessage());
				}

				return null;
			});

			_on(ExtEvent.PLAYER_JOIN_ROOM_HANDLE, args -> {
				try {
					var room = (RoomPhaserjs) _getRoom(args[1]);

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
				} catch (ExtensionValueCastException e) {
					_error(e, e.getMessage());
				}

				return null;
			});

			_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, args -> {
				try {
					var player = (PlayerPhaserjs) _getPlayer(args[0]);
					var message = _getCommonObject(args[2]);
					var move = message.getMessageObjectArray("d");

					player.setPosition(move.getInt(0), move.getInt(1));

					var pack = _messageApi.getMessageObjectArray();
					pack.add(player.getName());
					pack.add(move.get(0));
					pack.add(move.get(1));
					_messageApi.sendToRoom(phaserjsRoom, PlayerPhaserjs.MAIN_CHANNEL, "c", "m", "d", pack);
				} catch (ExtensionValueCastException e) {
					_error(e, e.getMessage());
				}

				return null;
			});

			_on(ExtEvent.FETCHED_CCU_NUMBER, args -> {
				try {
					var ccu = _getInteger(args[0]);

					_info("FETCHED_CCU_INFO", ccu);
				} catch (ExtensionValueCastException e) {
					_error(e, e.getMessage());
				}

				return null;
			});

		}

	}

}
