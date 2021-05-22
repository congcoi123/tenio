package com.tenio.core.network.zero.handlers;

import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.statistics.NetworkReaderStatistic;

public interface BaseIOHandler {

	void setSessionManager(SessionManager sessionManager);

	void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic);

}
