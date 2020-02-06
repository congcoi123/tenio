/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.entities;

import java.util.List;
import java.util.Map;

import com.tenio.entities.element.TObject;
import com.tenio.entities.manager.PlayerManager;

/**
 * A room or simpler is a group of related players @see {@link IPlayer}. These
 * players can be played in the same game or in the same location. This class is
 * only for logic handling. You can manage a list of players in a room as well
 * as hold the players' common data for sharing. For simple handling, one room
 * can hold a number of players, but one player only appears in one room at a
 * time. The player can be a free one and no need to join any rooms, there, it
 * is only under the {@link PlayerManager} class' management.
 * 
 * @author kong
 * 
 */
public interface IRoom extends IBackup<IRoom> {

	int getId();
	
	String getName();

	void setName(String name);

	boolean isState(int state);

	int getState();

	void setState(int state);

	boolean isFull();

	boolean isEmpty();

	void setPlayersState(int state);

	String getPassword();

	void setPassword(String password);

	int getMaxPlayers();

	void setMaxPlayers(int maxPlayer);

	int getMaxSpectators();

	void setMaxSpectators(int maxSpectators);

	boolean isHidden();

	void setHidden(boolean hidden);

	boolean isActive();

	void setActive(boolean active);

	Object getVariable(String key);

	TObject getVariables();

	void setVariable(String key, Object value);

	void setVariables(TObject variables);

	Map<String, IPlayer> getPlayers();

	List<IPlayer> getRealPlayersList();

	List<IPlayer> getSpectatorsList();

	IPlayer getFirstPlayer();

	IPlayer getOwner();

	IPlayer getPlayerByName(String playerName);

	void setOwner(IPlayer player);

	int countPlayers();

	int countRealPlayers();

	int countSpectators();

	void addPlayer(IPlayer player, boolean isSpectator);

	void addPlayer(IPlayer player);

	void removePlayer(IPlayer player);

	boolean containsPlayer(IPlayer player);

	boolean containsPlayer(String playerName);

	void switchPlayerToSpectator(IPlayer player);

	void switchSpectatorToPlayer(IPlayer player);

}
