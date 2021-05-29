/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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
package com.tenio.core.server.settings;

import java.util.List;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.ConfigurationException;
import com.tenio.core.exceptions.NotDefinedSubscribersException;
import com.tenio.core.extension.events.EventAttachConnectionRequestValidation;
import com.tenio.core.extension.events.EventAttachedConnectionResult;
import com.tenio.core.extension.events.EventHttpRequestHandle;
import com.tenio.core.extension.events.EventHttpRequestValidation;
import com.tenio.core.extension.events.EventPlayerReconnectRequestHandle;
import com.tenio.core.extension.events.EventPlayerReconnectedResult;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.SocketConfig;

public final class ConfigurationAssessment {

	private final EventManager __eventManager;
	private final Configuration __configuration;

	public static ConfigurationAssessment newInstance(EventManager eventManager, Configuration configuration) {
		return new ConfigurationAssessment(eventManager, configuration);
	}

	private ConfigurationAssessment(EventManager eventManager, Configuration configuration) {
		__eventManager = eventManager;
		__configuration = configuration;
	}

	public void assess() throws NotDefinedSubscribersException, ConfigurationException {
		__checkSubscriberReconnection();
		__checkSubscriberConnectionAttach();
		__checkDefinedMainSocketConnection();
		__checkSubscriberHttpHandler();
	}

	private void __checkSubscriberReconnection() throws NotDefinedSubscribersException {
		if (__configuration.getBoolean(CoreConfigurationType.PROP_KEEP_PLAYER_ON_DISCONNECTION)) {
			if (!__eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECT_REQUEST_HANDLE)
					|| !__eventManager.hasSubscriber(ServerEvent.PLAYER_RECONNECTED_RESULT)) {
				throw new NotDefinedSubscribersException(EventPlayerReconnectRequestHandle.class,
						EventPlayerReconnectedResult.class);
			}
		}
	}

	private void __checkSubscriberConnectionAttach() throws NotDefinedSubscribersException {
		if (__containsTcpSocketConfig() && __containsUdpSocketConfig()) {
			if (!__eventManager.hasSubscriber(ServerEvent.ATTACH_CONNECTION_REQUEST_VALIDATION)
					|| !__eventManager.hasSubscriber(ServerEvent.ATTACHED_CONNECTION_RESULT)) {
				throw new NotDefinedSubscribersException(EventAttachConnectionRequestValidation.class,
						EventAttachedConnectionResult.class);
			}
		}
	}

	private void __checkDefinedMainSocketConnection() throws ConfigurationException {
		if (!__containsTcpSocketConfig() && __containsUdpSocketConfig()) {
			throw new ConfigurationException("TCP connection was not defined");
		}
	}

	private void __checkSubscriberHttpHandler() throws NotDefinedSubscribersException {
		if (__containsHttpPathConfigs() && (!__eventManager.hasSubscriber(ServerEvent.HTTP_REQUEST_VALIDATION)
				|| !__eventManager.hasSubscriber(ServerEvent.HTTP_REQUEST_HANDLE))) {
			throw new NotDefinedSubscribersException(EventHttpRequestValidation.class, EventHttpRequestHandle.class);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean __containsTcpSocketConfig() {
		var socketConfigs = (List<SocketConfig>) __configuration.get(CoreConfigurationType.SOCKET_CONFIGS);
		return socketConfigs.stream().filter(socketConfig -> socketConfig.getType() == TransportType.TCP).findFirst()
				.isPresent();
	}

	@SuppressWarnings("unchecked")
	private boolean __containsUdpSocketConfig() {
		var socketConfigs = (List<SocketConfig>) __configuration.get(CoreConfigurationType.SOCKET_CONFIGS);
		return socketConfigs.stream().filter(socketConfig -> socketConfig.getType() == TransportType.UDP).findFirst()
				.isPresent();
	}

	@SuppressWarnings("unchecked")
	private boolean __containsHttpPathConfigs() {
		var httpConfigs = (List<HttpConfig>) __configuration.get(CoreConfigurationType.HTTP_CONFIGS);
		return !httpConfigs.isEmpty();
	}

}
