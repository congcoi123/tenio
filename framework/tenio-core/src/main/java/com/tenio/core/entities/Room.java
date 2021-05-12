package com.tenio.core.entities;

import java.util.List;

import com.tenio.core.entities.implement.ZoneImpl;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.settings.RoomRemoveMode;
import com.tenio.core.network.entities.session.Session;

public interface Room {

	long getId();
		
	String getName();

	void setName(String name);

	String getPassword();

	void setPassword(String password) throws RuntimeException;

	RoomState getState();

	void setState(RoomState state);

	boolean isPublic();

	int getMaxPlayers();

	void setMaxPlayers(int maxPlayers);

	int getMaxSpectators();

	void setMaxSpectators(int maxSpectators);

	Player getOwner();

	void setOwner(Player owner);

	PlayerManager getPlayerManager();

	void setPlayerManager(PlayerManager playerManager);

	ZoneImpl getZone();

	void setZone(ZoneImpl zone);

	boolean isActivated();

	void setActivated(boolean activated);

	RoomRemoveMode getRoomRemoveMode();

	void setRoomRemoveMode(RoomRemoveMode roomRemoveMode);

	Object getProperty(String key);

	boolean containsProperty(String key);

	void addProperty(String key, Object value);

	void removeProperty(String key);

	int getCapacity();

	void setCapacity(int maxPlayers, int maxSpectators);

	List<Player> getPlayersList();

	List<Player> getSpectatorsList();

	boolean containsPlayerName(String playerName);

	boolean containsPlayer(Player player);

	Player getPlayerById(long playerId);

	Player getPlayerByName(String playerName);

	Player getPlayerBySession(Session session);

	List<Player> getAllPlayersList();

	List<Session> getAllSessionList();

	void addPlayer(Player player, boolean asSpectator, int targetSlot, boolean allowHolding)
			throws RuntimeException;

	void addPlayer(Player player, boolean asSpectator, int targetSlot) throws RuntimeException;

	void addPlayer(Player player, boolean asSpectator) throws RuntimeException;

	void addPlayer(Player player) throws RuntimeException;

	void removeUser(Player player);

	void switchPlayerToSpectator(Player player) throws RuntimeException;

	void switchSpectatorToPlayer(Player player, int targetSlot) throws RuntimeException;

	void switchSpectatorToPlayer(Player player) throws RuntimeException;

	boolean isEmpty();

	boolean isFull();

}
