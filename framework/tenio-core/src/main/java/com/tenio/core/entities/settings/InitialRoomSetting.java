package com.tenio.core.entities.settings;

import com.tenio.core.entities.settings.strategies.RoomCredentialValidatedStrategy;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;

public final class InitialRoomSetting {

	private String __name;
	private String __password;
	private int __maxPlayers;
	private int __maxSpectators;
	private boolean __activated;
	private RoomRemoveMode __removeMode;
	private RoomCredentialValidatedStrategy __credentialValidatedStrategy;
	private RoomPlayerSlotGeneratedStrategy __playerIdGeneratedStrategy;

	public static InitialRoomSetting newInstance() {
		return new InitialRoomSetting();
	}

	private InitialRoomSetting() {
		__name = null;
		__password = null;
		__maxPlayers = 0;
		__maxSpectators = 0;
		__activated = false;
		__removeMode = RoomRemoveMode.DEFAULT;
	}

	public String getName() {
		return __name;
	}

	public void setName(String name) {
		__name = name;
	}

	public String getPassword() {
		return __password;
	}

	public void setPassword(String password) {
		__password = password;
	}

	public int getMaxPlayers() {
		return __maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		__maxPlayers = maxPlayers;
	}

	public int getMaxSpectators() {
		return __maxSpectators;
	}

	public void setMaxSpectators(int maxSpectators) {
		__maxSpectators = maxSpectators;
	}

	public boolean isActivated() {
		return __activated;
	}

	public void setActivated(boolean activated) {
		__activated = activated;
	}

	public RoomRemoveMode getRoomRemoveMode() {
		return __removeMode;
	}

	public void setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
		__removeMode = roomRemoveMode;
	}

	public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
		return __credentialValidatedStrategy;
	}

	public void setRoomCredentialValidatedStrategy(RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
		__credentialValidatedStrategy = roomCredentialValidatedStrategy;
	}

	public RoomPlayerSlotGeneratedStrategy getRoomPlayerIdGeneratedStrategy() {
		return __playerIdGeneratedStrategy;
	}

	public void setRoomPlayerIdGeneratedStrategy(RoomPlayerSlotGeneratedStrategy roomPlayerIdGeneratedStrategy) {
		__playerIdGeneratedStrategy = roomPlayerIdGeneratedStrategy;
	}

}
