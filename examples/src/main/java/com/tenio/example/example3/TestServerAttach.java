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
package com.tenio.example.example3;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.RestMethod;
import com.tenio.core.entity.element.MessageObject;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

/**
 * This class shows how a server handle messages that came from a client
 * 
 * @author kong
 *
 */
public final class TestServerAttach extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		var game = new TestServerAttach();
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

		@SuppressWarnings("unchecked")
		@Override
		public void initialize(IConfiguration configuration) {
			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getMessageObject(args[1]);

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerAttach(username), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, args -> {
				// The player has login successful
				var player = this.<PlayerAttach>_getPlayer(args[0]);

				// Now you can allow the player make a UDP connection request
				_messageApi.sendToPlayer(player, PlayerAttach.MAIN_CHANNEL, "c", "udp");

				return null;
			});

			_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, args -> {
				var player = this.<PlayerAttach>_getPlayer(args[0]);
				int index = _getInt(args[1]);

				_messageApi.sendToPlayer(player, index, "hello",
						"from server > " + index + " > " + player.getConnection(index).getAddress());

				return null;
			});

			_on(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, args -> {
				var message = _getMessageObject(args[1]);
				String name = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(name);
			});

			_on(ExtEvent.ATTACH_CONNECTION_SUCCESS, args -> {
				var player = this.<PlayerAttach>_getPlayer(args[1]);

				_messageApi.sendToPlayer(player, PlayerAttach.MAIN_CHANNEL, "c", "udp-done");

				return null;
			});

			_on(ExtEvent.HTTP_REQUEST_VALIDATE, args -> {
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

			_on(ExtEvent.HTTP_REQUEST_HANDLE, args -> {
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

		}

	}

}
