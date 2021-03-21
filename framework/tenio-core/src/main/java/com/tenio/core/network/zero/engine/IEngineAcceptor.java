package com.tenio.core.network.zero.engine;

import java.io.IOException;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.network.zero.handler.IOHandler;
import com.tenio.core.network.zero.security.IConnectionFilter;

public interface IEngineAcceptor {

	void setConfiguration(final IConfiguration configuration);
	
	void setConnectionFilter(final IConnectionFilter connectionFilter);
	
	void setIoHandler(final IOHandler ioHandler);

	void setEngineReader(final IEngineReader engineReader);

	void handleAcceptableChannels();

	void setup() throws UnsupportedOperationException, IOException;
	
	void start();

	void stop();

}