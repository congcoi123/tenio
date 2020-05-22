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

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entity.element.TArray;
import com.tenio.example.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;
import com.tenio.utility.MathUtility;

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

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.xml");
	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		RoomPhaserjs phaserjsRoom = new RoomPhaserjs("phaserjs", "Phaserjs", 3);

		@Override
		public void initialize() {
			_on(TEvent.CONNECTION_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getTObject(args[1]);

				info("CONNECTION", connection.getAddress());

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerPhaserjs(username), connection);

				return null;
			});

			_on(TEvent.DISCONNECT_CONNECTION, args -> {
				var connection = _getConnection(args[0]);

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			_on(TEvent.PLAYER_IN_SUCCESS, args -> {
				// The player has login successful
				var player = this.<PlayerPhaserjs>_getPlayer(args[0]);
				player.setIgnoreTimeout(true);
				// create the initial position
				int x = MathUtility.randInt(100, 400);
				int y = MathUtility.randInt(100, 400);
				player.setPosition(x, y);

				info("PLAYER_IN_SUCCESS", player.getName());

				_playerApi.playerJoinRoom(phaserjsRoom, player);

				return null;
			});

			_on(TEvent.PLAYER_JOIN_ROOM, args -> {
				var player = this.<PlayerPhaserjs>_getPlayer(args[0]);
				var room = this.<RoomPhaserjs>_getRoom(args[1]);

				info("PLAYER_JOIN_ROOM", player.getName(), room.getName());

				var pack = _messageApi.getArrayPack();
				var players = room.getPlayers();
				for (var p : players.values()) {
					var pjs = (PlayerPhaserjs) p;
					var data = TArray.newInstance();
					data.add(pjs.getName());
					data.add(pjs.getX());
					data.add(pjs.getY());
					pack.add(data);
				}
				_messageApi.sendToRoom(room, PlayerPhaserjs.MAIN_CHANNEL, "c", "i", "d", pack);

				return null;
			});

			_on(TEvent.RECEIVED_FROM_PLAYER, args -> {
				var player = this.<PlayerPhaserjs>_getPlayer(args[0]);
				var message = _getTObject(args[2]);
				var move = message.getTArray("d");

				player.setPosition(move.getInt(0), move.getInt(1));

				info("RECEIVED_FROM_PLAYER", message);

				var pack = _messageApi.getArrayPack();
				pack.add(player.getName());
				pack.add(move.get(0));
				pack.add(move.get(1));
				_messageApi.sendToRoom(phaserjsRoom, PlayerPhaserjs.MAIN_CHANNEL, "c", "m", "d", pack);

				return null;
			});

			_on(TEvent.PLAYER_TIMEOUT, args -> {
				var player = this.<PlayerPhaserjs>_getPlayer(args[0]);

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			_on(TEvent.DISCONNECT_PLAYER, args -> {
				var player = this.<PlayerPhaserjs>_getPlayer(args[0]);

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			_on(TEvent.CCU, args -> {
				var ccu = _getInt(args[0]);

				info("CCU", ccu);

				return null;
			});

		}

	}

}
