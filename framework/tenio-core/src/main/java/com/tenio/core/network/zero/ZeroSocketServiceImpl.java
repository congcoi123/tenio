package com.tenio.core.network.zero;

import java.lang.reflect.InvocationTargetException;

import com.tenio.core.events.EventManager;
import com.tenio.core.manager.AbstractManager;
import com.tenio.core.network.entities.session.SessionManager;
import com.tenio.core.network.security.ConnectionFilter;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
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

	private ConnectionFilter __connectionFilter;
	private DatagramIOHandler __datagramIOHandler;
	private SocketIOHandler __socketIOHandler;
	private SessionManager __sessionManager;

	private NetworkReaderStatistic __readerStatistic;
	private NetworkWriterStatistic __writerStatistic;

	public ZeroSocketServiceImpl(EventManager eventManager) {
		super(eventManager);
	}

	private void __setupAcceptor() {
		__acceptorEngine = ZeroAcceptorImpl.newInstance();

		__acceptorEngine.setConnectionFilter(__connectionFilter);
		__acceptorEngine.setDatagramIOHandler(__datagramIOHandler);
		// TODO: configure
		__acceptorEngine.setMaxBufferSize(1024);
		__acceptorEngine.setSessionManager(__sessionManager);
		// TODO: configure
		__acceptorEngine.setSocketConfigs(null);
		__acceptorEngine.setSocketIOHandler(__socketIOHandler);
		// TODO: configure
		__acceptorEngine.setThreadPoolSize(10);
		__acceptorEngine.setZeroReaderListener((ZeroReaderListener) __readerEngine);
	}

	private void __setupReader() {
		__readerEngine = ZeroReaderImpl.newInstance();

		__readerEngine.setDatagramIOHandler(__datagramIOHandler);
		__readerEngine.setMaxBufferSize(10);
		__readerEngine.setNetworkReaderStatistic(__readerStatistic);
		__readerEngine.setSessionManager(__sessionManager);
		__readerEngine.setSocketIOHandler(__socketIOHandler);
		__readerEngine.setThreadPoolSize(10);
		__readerEngine.setZeroAcceptorListener((ZeroAcceptorListener) __acceptorEngine);
		__readerEngine.setZeroWriterListener((ZeroWriterListener) __writerEngine);
	}

	private void __setupWriter() {
		__writerEngine = ZeroWriterImpl.newInstance();

		__writerEngine.setDatagramIOHandler(__datagramIOHandler);
		__writerEngine.setMaxBufferSize(10);
		__writerEngine.setNetworkWriterStatistic(__writerStatistic);
		__writerEngine.setSessionManager(__sessionManager);
		__writerEngine.setSocketIOHandler(__socketIOHandler);
		__writerEngine.setThreadPoolSize(10);
	}

	private void __setupHandlers() {
		__datagramIOHandler = DatagramIOHandlerImpl.newInstance();
		__socketIOHandler = SocketIOHandlerImpl.newInstance();
	}

	private Object __createInstance(String className) {
		Object object = null;
		try {
			Class<?> classDefinition = Class.forName(className);
			object = classDefinition.getDeclaredConstructor().newInstance();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}

	@Override
	public void initialize() {
		__setupAcceptor();
		__setupReader();
		__setupWriter();
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
	public int getAcceptorBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcceptorBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAcceptorWorkerSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcceptorWorkerSize(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getReaderBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setReaderBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getReaderWorkerSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setReaderWorkerSize(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWriterBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWriterBufferSize(int bufferSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWriterWorkerSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWriterWorkerSize(int workerSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConnectionFilter(ConnectionFilter connectionFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic readerStatistic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic writerStatistic) {
		// TODO Auto-generated method stub

	}

}
