/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tenio.core.configuration.define.CoreConfigurationType;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoreKeepaliveConfigurationTest {

  @TempDir
  Path temporaryDirectory;

  @Test
  void embeddedDefaultsDisableMainConnectionExpiryAndScheduling() throws Exception {
    DefaultCoreConfiguration configuration = new DefaultCoreConfiguration();

    configuration.load(null);

    assertEquals(0, configuration.getInt(
        CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS));
    assertFalse(configuration.isDefined(CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN));
  }

  @Test
  void customXmlLoadsBothMainConnectionKeepaliveSettings() throws Exception {
    Path file = temporaryDirectory.resolve("keepalive.xml");
    Files.writeString(file, """
        <Server>
          <Properties><Property name="profile">test</Property></Properties>
          <Configuration>
            <Properties>
              <Property name="main-connection-keepalive-scan-interval">7</Property>
              <Property name="main-connection-keepalive-timeout-seconds">45</Property>
            </Properties>
          </Configuration>
        </Server>
        """);
    DefaultCoreConfiguration configuration = new DefaultCoreConfiguration();

    configuration.load(file.toString());

    assertEquals(45, configuration.getInt(
        CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS));
    assertEquals(7, configuration.getInt(
        CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN));
  }

  @Test
  void omittedOptionalSettingsAreAbsentForServerToApplyDisabledDefaults() throws Exception {
    Path file = temporaryDirectory.resolve("existing.xml");
    Files.writeString(file, """
        <Server><Properties><Property name="profile">test</Property></Properties></Server>
        """);
    DefaultCoreConfiguration configuration = new DefaultCoreConfiguration();

    configuration.load(file.toString());

    assertFalse(configuration.isDefined(
        CoreConfigurationType.PROP_MAIN_CONNECTION_KEEPALIVE_TIMEOUT_SECONDS));
    assertFalse(configuration.isDefined(
        CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN));
  }
}
