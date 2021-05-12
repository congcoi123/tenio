package com.tenio.core.entities.settings.strategies;

public interface RoomPasswordValidatedStrategy {

	boolean validate(String password) throws RuntimeException;

}
