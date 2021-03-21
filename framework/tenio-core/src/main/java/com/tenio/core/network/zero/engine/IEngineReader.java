package com.tenio.core.network.zero.engine;

import java.io.IOException;
import java.nio.channels.Selector;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.network.zero.handler.IOHandler;

public interface IEngineReader {

	void setConfiguration(final IConfiguration configuration);

	void setIoHandler(final IOHandler ioHandler);
	
	void setEngineAcceptor(final IEngineAcceptor engineAcceptor);
	
	void setEngineWriter(final IEngineWriter engineWriter);

	Selector getSelector();

	long getReadBytes();
	
	void setup() throws IOException;

	void start();

	void stop();

}
