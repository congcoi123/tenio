package com.tenio.core.entities.settings.strategies;

import com.tenio.core.entities.Room;

public interface RoomPlayerSlotGeneratedStrategy {

	void initialize();

	long getPlayerSlotInRoom();

	void freeLeftPlayerSlot(int slot);

	void takeSlot(int slot) throws RuntimeException;

	Room getRoom();

	void setRoom(Room room);

}
