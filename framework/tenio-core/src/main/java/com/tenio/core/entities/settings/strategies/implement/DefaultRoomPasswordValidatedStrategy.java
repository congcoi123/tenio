package com.tenio.core.entities.settings.strategies.implement;

import com.tenio.core.entities.settings.strategies.RoomPasswordValidatedStrategy;

public class DefaultRoomPasswordValidatedStrategy implements RoomPasswordValidatedStrategy {

	@Override
	public boolean validate(String password) throws RuntimeException {
		return false;
	}

}
