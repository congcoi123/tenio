package com.tenio.core.network.zero.engine;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.network.zero.handler.IOHandler;

public interface IEngineWriter {

	void continueWriteOp(SocketChannel socketChannel);

	long getDroppedPacketsCount();

	long getWrittenBytes();

	long getWrittenPackets();

	void setConfiguration(final IConfiguration configuration);

	void setIoHandler(final IOHandler ioHandler);

	void setup() throws IOException;

	void start();

	void stop();

}
