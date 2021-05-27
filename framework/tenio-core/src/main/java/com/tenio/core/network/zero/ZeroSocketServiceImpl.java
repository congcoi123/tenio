package com.tenio.core.network.zero;

import java.util.List;

import com.tenio.core.event.implement.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.engines.ZeroAcceptor;
import com.tenio.core.network.zero.engines.ZeroReader;
import com.tenio.core.network.zero.engines.ZeroWriter;
import com.tenio.core.network.zero.engines.implement.ZeroAcceptorImpl;
import com.tenio.core.network.zero.engines.implement.ZeroReaderImpl;
import com.tenio.core.network.zero.engines.implement.ZeroWriterImpl;
import com.tenio.core.network.zero.engines.listeners.ZeroAcceptorListener;
import com.tenio.core.network.zero.engines.listeners.ZeroReaderListener;
import com.tenio.core.network.zero.engines.listeners.ZeroWriterListener;
import com.tenio.core.network.zero.handlers.DatagramIOHandler;
import com.tenio.core.network.zero.handlers.SocketIOHandler;
import com.tenio.core.network.zero.handlers.implement.DatagramIOHandlerImpl;
import com.tenio.core.network.zero.handlers.implement.SocketIOHandlerImpl;

public final class ZeroSocketServiceImpl extends AbstractManager implements ZeroSocketService {

	private ZeroAcceptor __acceptorEngine;
	private ZeroReader __readerEngine;
	private ZeroWriter __writerEngine;

	private DatagramIOHandler __datagramIOHandler;
	private SocketIOHandler __socketIOHandler;

	public static ZeroSocketService newInstance(EventManager eventManager) {
		return new ZeroSocketServiceImpl(eventManager);
	}

	private ZeroSocketServiceImpl(EventManager eventManager) {
		super(eventManager);

		__acceptorEngine = ZeroAcceptorImpl.newInstance(eventManager);
		__readerEngine = ZeroReaderImpl.newInstance(eventManager);
		__writerEngine = ZeroWriterImpl.newInstance(eventManager);

		__datagramIOHandler = DatagramIOHandlerImpl.newInstance(eventManager);
		__socketIOHandler = SocketIOHandlerImpl.newInstance(eventManager);
	}

	private void __setupAcceptor() {
		__acceptorEngine.setDatagramIOHandler(__datagramIOHandler);
		__acceptorEngine.setSocketIOHandler(__socketIOHandler);
		__acceptorEngine.setZeroReaderListener((ZeroReaderListener) __readerEngine);
	}

	private void __setupReader() {
		__readerEngine.setDatagramIOHandler(__datagramIOHandler);
		__readerEngine.setSocketIOHandler(__socketIOHandler);
		__readerEngine.setZeroAcceptorListener((ZeroAcceptorListener) __acceptorEngine);
		__readerEngine.setZeroWriterListener((ZeroWriterListener) __writerEngine);
	}

	private void __setupWriter() {
		__writerEngine.setDatagramIOHandler(__datagramIOHandler);
		__writerEngine.setSocketIOHandler(__socketIOHandler);
	}

	@Override
	public void initialize() {
		__setupAcceptor();
		__setupReader();
		__setupWriter();
		
		__readerEngine.initialize();
		__writerEngine.initialize();
		__acceptorEngine.initialize();
	}

	@Override
	public void start() {
		__acceptorEngine.start();
		__readerEngine.start();
		__writerEngine.start();
	}

	@Override
	public void resume() {
		__acceptorEngine.resume();
		__readerEngine.resume();
		__writerEngine.resume();
	}

	@Override
	public void pause() {
		__acceptorEngine.pause();
		__readerEngine.pause();
		__writerEngine.pause();
	}

	@Override
	public void halt() {
		__acceptorEngine.halt();
		__readerEngine.halt();
		__writerEngine.halt();
	}

	@Override
	public void destroy() {
		__acceptorEngine.destroy();
		__readerEngine.destroy();
		__writerEngine.destroy();

		__datagramIOHandler = null;
		__socketIOHandler = null;

		__acceptorEngine = null;
		__readerEngine = null;
		__writerEngine = null;
	}

	@Override
	public boolean isActivated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return "zero-socket-service";
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAcceptorBufferSize(int bufferSize) {
		__acceptorEngine.setMaxBufferSize(bufferSize);
	}

	@Override
	public void setAcceptorWorkerSize(int workerSize) {
		__acceptorEngine.setThreadPoolSize(workerSize);
	}

	@Override
	public void setReaderBufferSize(int bufferSize) {
		__readerEngine.setMaxBufferSize(bufferSize);
	}

	@Override
	public void setReaderWorkerSize(int workerSize) {
		__readerEngine.setThreadPoolSize(workerSize);
	}

	@Override
	public void setWriterBufferSize(int bufferSize) {
		__writerEngine.setMaxBufferSize(bufferSize);
	}

	@Override
	public void setWriterWorkerSize(int workerSize) {
		__writerEngine.setThreadPoolSize(workerSize);
	}

	@Override
	public void setConnectionFilter(ConnectionFilter connectionFilter) {
		__acceptorEngine.setConnectionFilter(connectionFilter);
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__acceptorEngine.setSessionManager(sessionManager);
		__readerEngine.setSessionManager(sessionManager);
		__writerEngine.setSessionManager(sessionManager);

		__datagramIOHandler.setSessionManager(sessionManager);
		__socketIOHandler.setSessionManager(sessionManager);
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic) {
		__readerEngine.setNetworkReaderStatistic(readerStatistic);

		__datagramIOHandler.setNetworkReaderStatistic(readerStatistic);
		__socketIOHandler.setNetworkReaderStatistic(readerStatistic);
	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic) {
		__writerEngine.setNetworkWriterStatistic(writerStatistic);
	}

	@Override
	public void setSocketConfigs(List<SocketConfig> socketConfigs) {
		__acceptorEngine.setSocketConfigs(socketConfigs);
	}

	@Override
	public void setPacketEncoder(BinaryPacketEncoder packetEncoder) {
		__writerEngine.setPacketEncoder(packetEncoder);
	}

	@Override
	public void setPacketDecoder(BinaryPacketDecoder packetDecoder) {
		__socketIOHandler.setPacketDecoder(packetDecoder);
	}

	@Override
	public void write(Packet packet) {
		__writerEngine.enqueuePacket(packet);
	}

}
