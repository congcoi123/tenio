package com.tenio.core.configuration;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.configuration.defines.ExtensionEvent;
import com.tenio.core.exceptions.NotDefinedSocketConnectionException;
import com.tenio.core.exceptions.NotDefinedSubscribersException;

public final class ConfigurationAssessment {
	
	public void assess() {
		
	}

	private void __checkSubscriberReconnection(Configuration configuration) throws NotDefinedSubscribersException {
		if (configuration.getBoolean(CoreConfigurationType.KEEP_PLAYER_ON_DISCONNECT)) {
			if (!__eventManager.getExtension().hasSubscriber(ExtensionEvent.PLAYER_RECONNECT_REQUEST_HANDLE)
					|| !__eventManager.getExtension().hasSubscriber(ExtensionEvent.PLAYER_RECONNECT_SUCCESS)) {
				throw new NotDefinedSubscribersException(ExtensionEvent.PLAYER_RECONNECT_REQUEST_HANDLE,
						ExtensionEvent.PLAYER_RECONNECT_SUCCESS);
			}
		}
	}

	private void __checkSubscriberSubConnectionAttach(Configuration configuration)
			throws NotDefinedSubscribersException {
		if (__socketPortsSize > 1 || __webSocketPortsSize > 1) {
			if (!__eventManager.getExtension().hasSubscriber(ExtensionEvent.ATTACH_CONNECTION_REQUEST_VALIDATE)
					|| !__eventManager.getExtension().hasSubscriber(ExtensionEvent.ATTACH_CONNECTION_SUCCESS)
					|| !__eventManager.getExtension().hasSubscriber(ExtensionEvent.ATTACH_CONNECTION_FAILED)) {
				throw new NotDefinedSubscribersException(ExtensionEvent.ATTACH_CONNECTION_REQUEST_VALIDATE,
						ExtensionEvent.ATTACH_CONNECTION_SUCCESS, ExtensionEvent.ATTACH_CONNECTION_FAILED);
			}
		}
	}

	private void __checkDefinedMainSocketConnection(Configuration configuration)
			throws NotDefinedSocketConnectionException {
		if (__socketPorts.isEmpty() && __webSocketPorts.isEmpty()) {
			throw new NotDefinedSocketConnectionException();
		}
	}

	private void __checkSubscriberHttpHandler(Configuration configuration) throws NotDefinedSubscribersException {
		if (!__httpPorts.isEmpty() && (!__eventManager.getExtension().hasSubscriber(ExtensionEvent.HTTP_REQUEST_VALIDATE)
				|| !__eventManager.getExtension().hasSubscriber(ExtensionEvent.HTTP_REQUEST_HANDLE))) {
			throw new NotDefinedSubscribersException(ExtensionEvent.HTTP_REQUEST_VALIDATE, ExtensionEvent.HTTP_REQUEST_HANDLE);
		}
	}
	
}
