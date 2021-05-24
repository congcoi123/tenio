/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.network.entities.protocols.implement;

import java.util.ArrayList;
import java.util.Collection;

import com.tenio.core.entities.Player;
import com.tenio.core.network.entities.protocols.Response;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public final class ResponseImpl extends AbstractMessage implements Response {

	private Collection<Player> __players;
	private Collection<Session> __socketSessions;
	private Collection<Session> __webSocketSessions;
	private boolean __useUdp;
	private boolean __foundUdp;

	public static Response newInstance() {
		return new ResponseImpl();
	}

	private ResponseImpl() {
		super();

		__players = null;
		__socketSessions = null;
		__webSocketSessions = null;
		__useUdp = false;
		__foundUdp = false;
	}

	@Override
	public Collection<Session> getRecipientSocketSessions() {
		return __socketSessions;
	}

	@Override
	public Collection<Session> getRecipientWebSocketSessions() {
		return __webSocketSessions;
	}

	@Override
	public Response setRecipients(Collection<Player> players) {
		if (__players == null) {
			__players = players;
		} else {
			__players.addAll(players);
		}

		return this;
	}

	@Override
	public Response setRecipient(Player player) {
		if (__players == null) {
			__players = new ArrayList<Player>();
		}
		__players.add(player);

		return this;
	}

	@Override
	public Response useUdp() {
		__useUdp = true;
		return this;
	}

	@Override
	public void write() {
		// TODO if use udp is in use in case of websocket, use websocket instead

	}

	@Override
	public void writeInDelay(int delayInSeconds) {
		// TODO Auto-generated method stub

	}

	private Response __construct() throws RuntimeException {
		__players.stream().forEach(player -> {
			var session = player.getSession();
			if (session.isTcp()) {
				if (__socketSessions == null) {
					__socketSessions = new ArrayList<Session>();
					// when the session contains an UDP connection and the response requires it, add
					// its session to the list
					if (__useUdp && session.containsUdp()) {
						__socketSessions.add(session);
						__foundUdp = true;
					} else {
						__socketSessions.add(session);
					}
				}
			} else if (session.isWebSocket()) {

			}
		});

		if (__useUdp && !__foundUdp) {
			throw new UdpConnectionNotFoundException();
		}

		return this;
	}

}
