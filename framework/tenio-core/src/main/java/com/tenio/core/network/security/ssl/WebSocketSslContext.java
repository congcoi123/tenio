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
package com.tenio.core.network.security.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.tenio.common.loggers.SystemLogger;

public final class WebSocketSslContext extends SystemLogger {

	private static final String KEY_MANAGER_FACTORY_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
	private static final String DEFAULT_ALGORITHM = "SunX509";
	private static final String DEFAULT_KEYSTORE = "JKS";
	private static final String DEFAULT_PROTOCOL = "TLS";

	private static final String KEYSTORE_FILE_PASSWORD = "keystore.file.password";
	private static final String DEFAULT_KEYSTORE_PATH = "websocket-ssl-key.bin";

	private SSLContext __serverContext;

	public WebSocketSslContext() {

		try {
			String algorithm = Security.getProperty(KEY_MANAGER_FACTORY_ALGORITHM);
			if (algorithm == null) {
				algorithm = DEFAULT_ALGORITHM;
			}

			try {
				String keyStoreFilePassword = System.getProperty(KEYSTORE_FILE_PASSWORD);
				KeyStore ks = KeyStore.getInstance(DEFAULT_KEYSTORE);
				FileInputStream fin = new FileInputStream(DEFAULT_KEYSTORE_PATH);
				ks.load(fin, keyStoreFilePassword.toCharArray());

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
				kmf.init(ks, keyStoreFilePassword.toCharArray());

				__serverContext = SSLContext.getInstance(DEFAULT_PROTOCOL);
				__serverContext.init(kmf.getKeyManagers(), null, null);
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
