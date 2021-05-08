package com.tenio.core.network.zero.engine;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.handler.DatagramIOHandler;
import com.tenio.core.network.zero.handler.SocketIOHandler;

public interface ZeroEngine {
	
	boolean isActivated();

	void setActivated(boolean activated);

	void setConfiguration(Configuration configuration);

	Configuration getConfiguration();

	void setSocketIOHandler(SocketIOHandler socketIOHandler);

	SocketIOHandler getSocketIOHandler();

	void setDatagramIOHandler(DatagramIOHandler datagramIOHandler);

	DatagramIOHandler getDatagramIOHandler();

	void setSessionManager(SessionManager sessionManager);

	SessionManager getSessionManager();

}
