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
package com.tenio.core.entities.settings;

import com.tenio.core.entities.defines.RoomRemoveMode;
import com.tenio.core.entities.settings.strategies.RoomCredentialValidatedStrategy;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;

/**
 * @author kong
 */
// TODO: Add description
public final class InitialRoomSetting {

	private String __name;
	private String __password;
	private int __maxPlayers;
	private int __maxSpectators;
	private boolean __activated;
	private RoomRemoveMode __removeMode;
	private RoomCredentialValidatedStrategy __credentialValidatedStrategy;
	private RoomPlayerSlotGeneratedStrategy __playerSlotGeneratedStrategy;

	private InitialRoomSetting(Builder builder) {
		__name = builder.__name;
		__password = builder.__password;
		__maxPlayers = builder.__maxPlayers;
		__maxSpectators = builder.__maxSpectators;
		__activated = builder.__activated;
		__removeMode = builder.__removeMode;
		__credentialValidatedStrategy = builder.__credentialValidatedStrategy;
		__playerSlotGeneratedStrategy = builder.__playerSlotGeneratedStrategy;

	}

	public String getName() {
		return __name;
	}

	public String getPassword() {
		return __password;
	}

	public int getMaxPlayers() {
		return __maxPlayers;
	}

	public int getMaxSpectators() {
		return __maxSpectators;
	}

	public boolean isActivated() {
		return __activated;
	}

	public RoomRemoveMode getRoomRemoveMode() {
		return __removeMode;
	}

	public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
		return __credentialValidatedStrategy;
	}

	public RoomPlayerSlotGeneratedStrategy getRoomPlayerIdGeneratedStrategy() {
		return __playerSlotGeneratedStrategy;
	}

	public static class Builder {

		private String __name;
		private String __password;
		private int __maxPlayers;
		private int __maxSpectators;
		private boolean __activated;
		private RoomRemoveMode __removeMode;
		private RoomCredentialValidatedStrategy __credentialValidatedStrategy;
		private RoomPlayerSlotGeneratedStrategy __playerSlotGeneratedStrategy;

		public Builder() {
			__name = null;
			__password = null;
			__maxPlayers = 0;
			__maxPlayers = 0;
			__activated = false;
			__removeMode = RoomRemoveMode.DEFAULT;
			__credentialValidatedStrategy = null;
			__playerSlotGeneratedStrategy = null;
		}

		public Builder setName(String name) {
			__name = name;
			return this;
		}

		public Builder setPassword(String password) {
			__password = password;
			return this;
		}

		public Builder setMaxPlayers(int maxPlayers) {
			__maxPlayers = maxPlayers;
			return this;
		}

		public Builder setMaxSpectators(int maxSpectators) {
			__maxSpectators = maxSpectators;
			return this;
		}

		public Builder setActivated(boolean activated) {
			__activated = activated;
			return this;
		}

		public Builder setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
			__removeMode = roomRemoveMode;
			return this;
		}

		public Builder setRoomCredentialValidatedStrategy(
				RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
			__credentialValidatedStrategy = roomCredentialValidatedStrategy;
			return this;
		}

		public Builder setRoomPlayerSlotGeneratedStrategy(
				RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
			__playerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;
			return this;
		}

		public InitialRoomSetting build() {
			return new InitialRoomSetting(this);
		}

	}

}
