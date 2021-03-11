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
package com.tenio.example.example1;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.MessageObject;
import com.tenio.common.element.MessageObjectArray;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.entity.annotation.EntityProcess;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

/**
 * This class shows how a server handle messages that came from a client
 * 
 * @author kong
 *
 */
public final class TestServerLogin extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		var game = new TestServerLogin();
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

		@Override
		public void initialize(IConfiguration configuration) {
			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getMessageObject(args[1]);

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerLogin(username), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, args -> {
				// The player has login successful
				var player = this.<PlayerLogin>_getPlayer(args[0]);

				try {
					info("PLAYER BACKUP", EntityProcess.exportToJSON(player));
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Now you can send messages to the client
				_taskApi.run(player.getName(), Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
					// Only sent 10 messages
					if (player.counter >= 10) {
						_taskApi.kill(player.getName());
						_playerApi.logOut(player);
					}

					player.counter++;

					// Sending, the data need to be packed
					var data = _messageApi.getMessageObjectArray();
					_messageApi.sendToPlayer(player, PlayerLogin.MAIN_CHANNEL, "c", "message", "d",
							data.put("H").put("3").put("L").put("O").put(true)
									.put(MessageObjectArray.newInstance().put("Sub").put("Value").put(100)));

					// Attempt to send internal message
					_messageApi.sendToInternalServer(player, 1,
							MessageObject.newInstance().add("internal", "this is a message in external server"));

					try {
						info("PLAYER BACKUP", EntityProcess.exportToJSON(player));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}, 0, 1, TimeUnit.SECONDS));

				return null;
			});

			_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, args -> {
				var message = _getMessageObject(args[1]);

				info(buildgen("RECEIVED INTERNAL MESSAGE").toString(), message.toString());

				return null;
			});

		}

	}

}
