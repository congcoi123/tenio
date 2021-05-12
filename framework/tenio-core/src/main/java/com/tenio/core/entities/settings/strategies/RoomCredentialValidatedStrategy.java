package com.tenio.core.entities.settings.strategies;

public interface RoomCredentialValidatedStrategy {

	void validateName(String name) throws RuntimeException;

	void validatePassword(String password) throws RuntimeException;

}
