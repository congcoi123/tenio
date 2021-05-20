package com.tenio.core.entities;

import java.util.List;

import com.tenio.core.entities.defines.RoomRemoveMode;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.settings.strategies.RoomCredentialValidatedStrategy;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exceptions.AddDuplicatedPlayerException;
import com.tenio.core.exceptions.CoreMessageCodeException;
import com.tenio.core.exceptions.RemoveNonExistentPlayerException;
import com.tenio.core.network.entities.session.Session;

public interface Room {

	long getId();

	String getName();

	void setName(String name) throws IllegalArgumentException;

	String getPassword();

	void setPassword(String password) throws IllegalArgumentException;

	RoomState getState();

	void setState(RoomState state);

	boolean isPublic();

	int getMaxPlayers();

	void setMaxPlayers(int maxPlayers) throws IllegalArgumentException;

	int getMaxSpectators();

	void setMaxSpectators(int maxSpectators) throws IllegalArgumentException;

	Player getOwner();

	void setOwner(Player owner);

	PlayerManager getPlayerManager();

	void setPlayerManager(PlayerManager playerManager);

	boolean isActivated();

	void setActivated(boolean activated);

	RoomRemoveMode getRoomRemoveMode();

	void setRoomRemoveMode(RoomRemoveMode roomRemoveMode);

	Object getProperty(String key);

	boolean containsProperty(String key);

	void addProperty(String key, Object value);

	void removeProperty(String key);

	int getCapacity();

	void setCapacity(int maxPlayers, int maxSpectators) throws IllegalArgumentException;

	List<Player> getPlayersList();

	List<Player> getSpectatorsList();

	int getPlayerCount();

	int getSpectatorCount();

	boolean containsPlayerName(String playerName);

	boolean containsPlayer(Player player);

	Player getPlayerById(long playerId);

	Player getPlayerByName(String playerName);

	Player getPlayerBySession(Session session);

	List<Player> getAllPlayersList();

	List<Session> getAllSessionList();

	void addPlayer(Player player, boolean asSpectator, int targetSlot)
			throws CoreMessageCodeException, AddDuplicatedPlayerException;

	void addPlayer(Player player, boolean asSpectator) throws CoreMessageCodeException, AddDuplicatedPlayerException;

	void addPlayer(Player player) throws CoreMessageCodeException, AddDuplicatedPlayerException;

	void removePlayer(Player player) throws RemoveNonExistentPlayerException;

	void switchPlayerToSpectator(Player player) throws CoreMessageCodeException;

	void switchSpectatorToPlayer(Player player, int targetSlot) throws CoreMessageCodeException;

	void switchSpectatorToPlayer(Player player) throws CoreMessageCodeException;

	boolean isEmpty();

	boolean isFull();

	RoomPlayerSlotGeneratedStrategy getPlayerSlotGeneratedStrategy();

	void setPlayerSlotGeneratedStrategy(RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy);

	RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy();

	void setRoomCredentialValidatedStrategy(RoomCredentialValidatedStrategy roomCredentialValidatedStrategy);

}
