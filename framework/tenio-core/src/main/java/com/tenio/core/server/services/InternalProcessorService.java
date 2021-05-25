package com.tenio.core.server.services;

import com.tenio.core.controller.Controller;
import com.tenio.core.entities.managers.PlayerManager;

public interface InternalProcessorService extends Controller {

	void subscribe();

	void setMaxNumberPlayers(int maxPlayers);
	
	void setKeepPlayerOnDisconnection(boolean keep);

	void setPlayerManager(PlayerManager playerManager);

}
