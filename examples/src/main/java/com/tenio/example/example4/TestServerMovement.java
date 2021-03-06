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
package com.tenio.example.example4;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.constant.RestMethod;
import com.tenio.core.configuration.constant.TEvent;
import com.tenio.core.entity.element.MessageObject;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.engine.heartbeat.HeartBeatManager;
import com.tenio.example.example4.constant.Constants;
import com.tenio.example.example4.entity.Inspector;
import com.tenio.example.server.Configuration;

/**
 * This class makes a simple simulator for the physic 2d movement.
 * 
 * @author kong
 *
 */
public final class TestServerMovement extends AbstractApp {

	/**
	 * The entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		var game = new TestServerMovement();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.attach.xml");
	}

	/**
	 * Handle your own logic here
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@SuppressWarnings("unchecked")
		@Override
		public void initialize() {

			_on(TEvent.CONNECTION_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getMessageObject(args[1]);

				info("CONNECTION", connection.getAddress());

				// can make a login request for this connection
				String username = message.getString("u");
				_playerApi.login(new Inspector(username), connection);

				return null;
			});

			_on(TEvent.DISCONNECT_CONNECTION, args -> {
				var connection = _getConnection(args[0]);

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			_on(TEvent.PLAYER_IN_SUCCESS, args -> {
				// the player has login successful
				var player = this.<Inspector>_getPlayer(args[0]);
				player.setIgnoreTimeout(true);

				info("PLAYER IN", player.getName());

				// now we can allow that client send request for UDP connection
				_messageApi.sendToPlayer(player, Inspector.MAIN_CHANNEL, "c", "udp");

				return null;
			});

			_on(TEvent.PLAYER_TIMEOUT, args -> {
				var player = this.<Inspector>_getPlayer(args[0]);

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			_on(TEvent.DISCONNECT_PLAYER, args -> {
				var player = this.<Inspector>_getPlayer(args[0]);

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});

			_on(TEvent.ATTACH_CONNECTION_REQUEST, args -> {
				var message = _getMessageObject(args[1]);
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			_on(TEvent.ATTACH_CONNECTION_SUCCESS, args -> {
				var index = _getInt(args[0]);
				var player = this.<Inspector>_getPlayer(args[1]);

				info("ATTACH CONNECTION SUCCESS",
						player.getName() + " " + player.getConnection(Inspector.MAIN_CHANNEL).getAddress() + " "
								+ player.getConnection(index).getAddress());

				_messageApi.sendToPlayer(player, Inspector.MAIN_CHANNEL, "c", "udp-done");

				return null;
			});

			_on(TEvent.ATTACH_CONNECTION_FAILED, args -> {
				String reason = _getString(args[2]);

				info("ATTACH CONNECTION FAILED", reason);

				return null;
			});

			_on(TEvent.HTTP_REQUEST_VALIDATE, args -> {
				var method = _getRestMethod(args[0]);
				// var request = _getHttpServletRequest(args[1]);
				var response = _getHttpServletResponse(args[2]);

				if (method.equals(RestMethod.DELETE)) {
					var json = new JSONObject();
					json.putAll(MessageObject.newInstance().add("status", "failed").add("message", "not supported"));
					try {
						response.getWriter().println(json.toString());
					} catch (IOException e) {
						error(e, "request");
					}
					return response;
				}

				return null;
			});

			_on(TEvent.HTTP_REQUEST_HANDLE, args -> {
				// var method = _getRestMethod(args[0]);
				// var request = _getHttpServletRequest(args[1]);
				var response = _getHttpServletResponse(args[2]);

				var json = new JSONObject();
				json.putAll(MessageObject.newInstance().add("status", "ok").add("message", "handler"));
				try {
					response.getWriter().println(json.toString());
				} catch (IOException e) {
					error(e, "handler");
				}

				return null;
			});

			// Create a world
			var world = new World(Constants.DESIGN_WIDTH, Constants.DESIGN_HEIGHT);
			world.debug("[TenIO] Server Debugger : Movement Simulation");
			var hearbeatManager = new HeartBeatManager();
			try {
				hearbeatManager.initialize(1);
				hearbeatManager.create("world", world);
			} catch (Exception e) {
				error(e, "world");
			}

		}

	}

}
