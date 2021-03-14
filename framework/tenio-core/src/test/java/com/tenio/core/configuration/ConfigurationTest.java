/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.core.configuration.define.ConnectionType;
import com.tenio.core.configuration.define.CoreConfigurationType;

/**
 * @author kong
 */
public final class ConfigurationTest {

	private Configuration __configuration;

	@BeforeEach
	public void initialize() {
		__configuration = new Configuration("TenIOConfig.example.xml");
	}

	@Test
	public void getConfigurationExtensionShouldReturnTrueValue() {
		assertAll("getExtensionConfiguration",
				() -> assertEquals("String", __configuration.getString(TestConfigurationType.CUSTOM_VALUE_1)),
				() -> assertEquals(1, __configuration.getInt(TestConfigurationType.CUSTOM_VALUE_2)),
				() -> assertEquals(1.5, __configuration.getFloat(TestConfigurationType.CUSTOM_VALUE_3)),
				() -> assertEquals(true, __configuration.getBoolean(TestConfigurationType.CUSTOM_VALUE_4)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getConfigurationSocketPortsShouldReturnTrueValue() {
		var listSockets = (List<Sock>) (__configuration.get(CoreConfigurationType.SOCKET_PORTS));
		assertAll("getSocketPortsConfiguration", () -> assertEquals(8032, listSockets.get(0).getPort()),
				() -> assertEquals(8033, listSockets.get(1).getPort()),
				() -> assertEquals(8034, listSockets.get(2).getPort()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getConfigurationSocketPortsTypeShouldReturnTrueType() {
		var listSockets = (List<Sock>) (__configuration.get(CoreConfigurationType.SOCKET_PORTS));
		assertAll("getSocketPortsTypeConfiguration",
				() -> assertEquals(ConnectionType.SOCKET, listSockets.get(0).getType()),
				() -> assertEquals(ConnectionType.DATAGRAM, listSockets.get(1).getType()),
				() -> assertEquals(ConnectionType.SOCKET, listSockets.get(2).getType()));
	}

	@AfterEach
	public void tearDown() {
		// do nothing
	}

}
