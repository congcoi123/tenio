package com.tenio.core.network.netty.websocket.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.tenio.common.loggers.SystemLogger;

public final class WebSocketSslContext extends SystemLogger {

	private SSLContext __serverContext;

	public WebSocketSslContext() {

		try {
			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			if (algorithm == null) {
				algorithm = "SunX509";
			}

			try {
				String keyStoreFilePath = System.getProperty("keystore.file.path");
				String keyStoreFilePassword = System.getProperty("keystore.file.password");
				KeyStore ks = KeyStore.getInstance("JKS");
				// TODO: Replace key path
				FileInputStream fin = new FileInputStream("test.bin");
				ks.load(fin, keyStoreFilePassword.toCharArray());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
				kmf.init(ks, keyStoreFilePassword.toCharArray());
				__serverContext = SSLContext.getInstance("TLS");
				__serverContext.init(kmf.getKeyManagers(), (TrustManager[]) null, (SecureRandom) null);
			} catch (Exception e) {
				error(e);
			}
		} catch (Exception e) {
			error(e);
		}
	}

	public SSLContext getServerContext() {
		return __serverContext;
	}

}
