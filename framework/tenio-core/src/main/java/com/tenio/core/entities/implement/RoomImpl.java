package com.tenio.core.entities.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.RoomState;
import com.tenio.core.entities.defines.RoomRemoveMode;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.settings.strategies.RoomCredentialValidatedStrategy;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.network.entities.session.Session;

public final class RoomImpl implements Room {

	private static AtomicLong __idCounter = new AtomicLong();

	private final long __id;
	private String __name;
	private String __password;

	private int __maxPlayers;
	private int __maxSpectators;

	private Player __owner;
	private PlayerManager __playerManager;

	private RoomRemoveMode __roomRemoveMode;
	private RoomCredentialValidatedStrategy __roomCredentialValidatedStrategy;
	private RoomPlayerSlotGeneratedStrategy __roomPlayerSlotGeneratedStrategy;

	private final Lock __switchPlayerLock;

	private final Map<String, Object> __properties;
	private RoomState __state;

	private volatile boolean __activated;

	public RoomImpl(String name) {
		this(name, null, null);
	}

	private RoomImpl(String name, RoomCredentialValidatedStrategy roomPasswordValidatedStrategy,
			RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
		__id = __idCounter.getAndIncrement();
		__name = name;
		__password = null;

		__maxPlayers = 0;
		__maxSpectators = 0;

		__owner = null;
		__playerManager = null;

		__roomRemoveMode = RoomRemoveMode.DEFAULT;
		__roomCredentialValidatedStrategy = roomPasswordValidatedStrategy;
		__roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;

		__switchPlayerLock = new ReentrantLock();

		__properties = new ConcurrentHashMap<String, Object>();
		__activated = false;
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) throws RuntimeException {
		__roomCredentialValidatedStrategy.validateName(name);
		__name = name;
	}

	@Override
	public String getPassword() {
		return __password;
	}

	@Override
	public void setPassword(String password) throws RuntimeException {
		__roomCredentialValidatedStrategy.validatePassword(password);
		__password = password;
	}

	@Override
	public RoomState getState() {
		return __state;
	}

	@Override
	public void setState(RoomState state) {
		__state = state;
	}

	@Override
	public boolean isPublic() {
		return __password == null;
	}

	@Override
	public int getMaxPlayers() {
		return __maxPlayers;
	}

	@Override
	public void setMaxPlayers(int maxPlayers) {
		__maxPlayers = maxPlayers;
	}

	@Override
	public int getMaxSpectators() {
		return __maxSpectators;
	}

	@Override
	public void setMaxSpectators(int maxSpectators) {
		__maxPlayers = maxSpectators;
	}

	@Override
	public Player getOwner() {
		return __owner;
	}

	@Override
	public void setOwner(Player owner) {
		__owner = owner;
	}

	@Override
	public PlayerManager getPlayerManager() {
		return __playerManager;
	}

	@Override
	public void setPlayerManager(PlayerManager playerManager) {
		__playerManager = playerManager;
	}

	@Override
	public boolean isActivated() {
		return __activated;
	}

	@Override
	public void setActivated(boolean activated) {
		__activated = activated;
	}

	@Override
	public RoomRemoveMode getRoomRemoveMode() {
		return __roomRemoveMode;
	}

	@Override
	public void setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
		__roomRemoveMode = roomRemoveMode;
	}

	@Override
	public Object getProperty(String key) {
		return __properties.get(key);
	}

	@Override
	public boolean containsProperty(String key) {
		return __properties.containsKey(key);
	}

	@Override
	public void addProperty(String key, Object value) {
		__properties.put(key, value);
	}

	@Override
	public void removeProperty(String key) {
		__properties.remove(key);
	}

	@Override
	public int getCapacity() {
		return __maxPlayers + __maxSpectators;
	}

	@Override
	public void setCapacity(int maxPlayers, int maxSpectators) {
		__maxPlayers = maxPlayers;
		__maxSpectators = maxSpectators;
	}

	@Override
	public List<Player> getPlayersList() {
		var players = __playerManager.getAllPlayers().stream().filter(player -> !player.isSpectator())
				.collect(Collectors.toList());
		return new ArrayList<Player>(players);
	}

	@Override
	public List<Player> getSpectatorsList() {
		var spectators = __playerManager.getAllPlayers().stream().filter(player -> player.isSpectator())
				.collect(Collectors.toList());
		return new ArrayList<>(spectators);
	}

	@Override
	public boolean containsPlayerName(String playerName) {
		return __playerManager.containsPlayerName(playerName);
	}

	@Override
	public boolean containsPlayer(Player player) {
		return __playerManager.containsPlayer(player);
	}

	@Override
	public Player getPlayerById(long playerId) {
		return __playerManager.getPlayerById(playerId);
	}

	@Override
	public Player getPlayerByName(String playerName) {
		return __playerManager.getPlayerByName(playerName);
	}

	@Override
	public Player getPlayerBySession(Session session) {
		return __playerManager.getPlayerBySession(session);
	}

	@Override
	public List<Player> getAllPlayersList() {
		return new ArrayList<Player>(__playerManager.getAllPlayers());
	}

	@Override
	public List<Session> getAllSessionList() {
		return new ArrayList<Session>(__playerManager.getAllSessions());
	}

	@Override
	public void addPlayer(Player player, boolean asSpectator, int targetSlot, boolean allowHolding)
			throws RuntimeException {

	}

	@Override
	public void addPlayer(Player player, boolean asSpectator, int targetSlot) throws RuntimeException {
		addPlayer(player, asSpectator, targetSlot, false);
	}

	@Override
	public void addPlayer(Player player, boolean asSpectator) throws RuntimeException {
		addPlayer(player, asSpectator, -1);
	}

	@Override
	public void addPlayer(Player player) throws RuntimeException {
		addPlayer(player, false);
	}

	@Override
	public void removeUser(Player player) {

	}

	@Override
	public void switchPlayerToSpectator(Player player) throws RuntimeException {

	}

	@Override
	public void switchSpectatorToPlayer(Player player, int targetSlot) throws RuntimeException {

	}

	@Override
	public void switchSpectatorToPlayer(Player player) throws RuntimeException {
		switchSpectatorToPlayer(player, -1);
	}

	@Override
	public boolean isEmpty() {
		return __playerManager.getPlayerCount() == 0;
	}

	@Override
	public boolean isFull() {
		return __playerManager.getPlayerCount() == getCapacity();
	}

	@Override
	public boolean equals(Object room) {
		if (!(room instanceof Room)) {
			return false;
		} else {
			return getId() == ((Room) room).getId();
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public RoomPlayerSlotGeneratedStrategy getPlayerSlotGeneratedStrategy() {
		return __roomPlayerSlotGeneratedStrategy;
	}

	@Override
	public void setPlayerSlotGeneratedStrategy(RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
		__roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;
	}

	@Override
	public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
		return __roomCredentialValidatedStrategy;
	}

	@Override
	public void setRoomCredentialValidatedStrategy(RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
		__roomCredentialValidatedStrategy = roomCredentialValidatedStrategy;
	}

}
