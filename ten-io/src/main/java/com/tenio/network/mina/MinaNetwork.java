/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.network.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.INetwork;
import com.tenio.network.mina.codec.socket.ByteArrayCodecFactory;
import com.tenio.network.mina.socket.MinaSocketHandler;

/**
 * Use <a href="https://mina.apache.org/">Apache Mina</a> to handle a network
 * instance @see {@link INetwork}
 * 
 * @author kong
 * 
 */
public class MinaNetwork extends AbstractLogger implements INetwork {

	private IoAcceptor __tcp;

	@Override
	public void start(BaseConfiguration configuration) throws IOException, InterruptedException {
		if ((int) configuration.get(BaseConfiguration.SOCKET_PORT) != -1) {
			__bindTCP(configuration);
		}
		if ((int) configuration.get(BaseConfiguration.WEBSOCKET_PORT) != -1) {
			__bindWS(configuration);
		}
		if ((int) configuration.get(BaseConfiguration.DATAGRAM_PORT) != -1) {
			__bindUDP(configuration);
		}
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param configuration your own configuration @see {@link BaseConfiguration}
	 * @throws IOException
	 */
	private void __bindTCP(BaseConfiguration configuration) throws IOException {
		__tcp = new NioSocketAcceptor();

		__tcp.getFilterChain().addLast("logger", new LoggingFilter());
		__tcp.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ByteArrayCodecFactory()));

		__tcp.setHandler(new MinaSocketHandler(configuration));

		__tcp.getSessionConfig().setReadBufferSize(2048);

		__tcp.bind(new InetSocketAddress((int) configuration.get(BaseConfiguration.SOCKET_PORT)));

		info("SOCKET", buildgen("Start at port: ", (int) configuration.get(BaseConfiguration.SOCKET_PORT)));
	}

	/**
	 * Constructs a web socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param configuration configuration your own configuration @see
	 *                      {@link BaseConfiguration}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// FIXME
	private void __bindWS(BaseConfiguration configuration) throws IOException, InterruptedException {
		info("[UNSUPPORTED] WEB SOCKET",
				buildgen("Can not start at port: ", (int) configuration.get(BaseConfiguration.WEBSOCKET_PORT)));
	}

	/**
	 * Constructs a socket and binds it to the specified port on the local host
	 * machine.
	 * 
	 * @param configuration your own configuration @see {@link BaseConfiguration}
	 * @throws IOException
	 * @throws InterruptedException
	 */
	// FIXME
	private void __bindUDP(BaseConfiguration configuration) throws IOException, InterruptedException {
		info("[UNSUPPORTED] DATAGRAM",
				buildgen("Can not start at port: ", (int) configuration.get(BaseConfiguration.DATAGRAM_PORT)));
	}

	@Override
	public void shutdown() {
		__close(__tcp);
	}

	private void __close(IoAcceptor acceptor) {
		acceptor.setCloseOnDeactivation(true);
		for (IoSession ss : acceptor.getManagedSessions().values()) {
			ss.closeNow();
		}
		acceptor.unbind();
		acceptor.dispose();
	}

}
