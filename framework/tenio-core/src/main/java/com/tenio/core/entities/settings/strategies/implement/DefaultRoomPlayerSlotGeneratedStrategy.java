package com.tenio.core.entities.settings.strategies.implement;

import com.tenio.core.entities.Room;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;

public final class DefaultRoomPlayerSlotGeneratedStrategy implements RoomPlayerSlotGeneratedStrategy {

	@Override
	public void initialize() {

	}

	@Override
	public long getPlayerSlotInRoom() {
		return 0;
	}

	@Override
	public void freeLeftPlayerSlot(int slot) {

	}

	@Override
	public boolean takeSlot(int slot) throws RuntimeException {
		return false;
	}

	@Override
	public Room getRoom() {
		return null;
	}

	@Override
	public void setRoom(Room room) {

	}

}
