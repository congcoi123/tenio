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
package com.tenio.example.example6;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.MessageObjectArray;
import com.tenio.common.utility.MathUtility;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

/**
 * This class shows how a server handle 1000 players and communications
 * 
 * @author kong
 *
 */
public final class TestServerStress extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		var game = new TestServerStress();
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

		@Override
		public void initialize(IConfiguration configuration) {
			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, args -> {
				var connection = _getConnection(args[0]);
				var message = _getMessageObject(args[1]);

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerStress(username), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, args -> {
				// The player has login successful
				var player = this.<PlayerStress>_getPlayer(args[0]);
				player.setIgnoreTimeout(true);

				// Now you can send messages to the client
				// Sending, the data need to be packed
				var data = _messageApi.getMessageObjectArray();
				_messageApi.sendToPlayer(player, PlayerStress.MAIN_CHANNEL, "p", player.getName(), "d",
						data.put("H").put("3").put("L").put("O").put(true)
								.put(MessageObjectArray.newInstance().put("Sub").put("Value").put(100)));

				return null;
			});

			_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, args -> {
				var player = this.<PlayerStress>_getPlayer(args[0]);

				var pack = __getSortRandomNumberArray();
				// Sending, the data need to be packed
				var data = _messageApi.getMessageObjectArray();
				for (int i = 0; i < pack.length; i++) {
					data.put(pack[i]);
				}

				_messageApi.sendToPlayer(player, PlayerStress.MAIN_CHANNEL, "p", player.getName(), "d", data);

				return null;
			});

			_on(ExtEvent.FETCHED_CCU_NUMBER, args -> {
				var ccu = _getInt(args[0]);

				_info("FETCHED_CCU_NUMBER", ccu);

				return null;
			});

			_on(ExtEvent.FETCHED_BANDWIDTH_INFO, args -> {
				long lastReadThroughput = _getLong(args[0]);
				long lastWriteThroughput = _getLong(args[1]);
				long realWriteThroughput = _getLong(args[2]);
				long currentReadBytes = _getLong(args[3]);
				long currentWrittenBytes = _getLong(args[4]);
				long realWrittenBytes = _getLong(args[5]);

				var bandwidth = String.format(
						"lastReadThroughput=%dKB/s;lastWriteThroughput=%dKB/s;realWriteThroughput=%dKB/s;currentReadBytes=%dKB;currentWrittenBytes=%dKB;realWrittenBytes=%dKB",
						lastReadThroughput, lastWriteThroughput, realWriteThroughput, currentReadBytes,
						currentWrittenBytes, realWrittenBytes);

				_info("FETCHED_BANDWIDTH_INFO", bandwidth);

				return null;
			});

		}

		private int[] __getSortRandomNumberArray() {
			int[] arr = new int[10];
			for (int i = 0; i < arr.length; i++) {
				// storing random integers in an array
				arr[i] = MathUtility.randInt(0, 100);
			}
			// bubble sort
			int n = arr.length;
			int temp = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 1; j < (n - i); j++) {
					if (arr[j - 1] > arr[j]) {
						// swap elements
						temp = arr[j - 1];
						arr[j - 1] = arr[j];
						arr[j] = temp;
					}
				}
			}

			return arr;
		}

	}

}
