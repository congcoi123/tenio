package com.tenio.core.network.entity.protocol;

import java.util.Collection;

import com.tenio.core.entity.Player;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.entity.session.SessionType;

public interface Response extends Message {

	Collection<Session> getRecipientSocketSessions();

	Collection<Session> getRecipientWebSocketSessions();

	void setRecipients(Collection<Player> collection);

	void setRecipient(Player player);

	void setSessionType(SessionType sessionType);

	void write();

	void writeInDelay(int delay);

}
