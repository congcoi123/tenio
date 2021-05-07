package com.tenio.core.network.entity.protocol.implement;

import java.util.Collection;

import com.tenio.core.entity.Player;
import com.tenio.core.network.WriterManager;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.connection.SessionType;
import com.tenio.core.network.entity.protocol.Response;

public final class ResponseImpl extends AbstractMessage implements Response {

	private WriterManager __writerManager;

	@Override
	public Collection<Session> getRecipientSocketSessions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Collection<Session> getRecipientWebSocketSessions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRecipients(Collection<Player> collection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRecipient(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSessionType(SessionType sessionType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write() {
		__writerManager.write(this);
	}

	@Override
	public void writeInDelay(int delay) {
		// TODO Auto-generated method stub

	}

}
