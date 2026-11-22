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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.injector.ProfileSelection;
import com.tenio.core.configuration.define.CoreConfigurationType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("XML startup profile configuration")
class CoreProfileConfigurationTest {

  @TempDir
  Path directory;

  @Test
  void embeddedConfigurationSelectsDefaultProfile() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    configuration.load(null);

    assertEquals(Set.of("default", "metrics"), selectedProfiles(configuration));
  }

  @Test
  void missingExternalFileFallsBackToEmbeddedProfile() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    configuration.load(directory.resolve("not-present.xml").toString());

    assertEquals(Set.of("default", "metrics"), selectedProfiles(configuration));
  }

  @Test
  void externalConfigurationPreservesCsvForSharedSelectionPolicy() throws Exception {
    String value = " world, game, world ";
    var configuration = new DefaultCoreConfiguration();
    configuration.load(writeXml("<Properties><Property name=\"profile\">" + value
        + "</Property></Properties>").toString());

    assertEquals(value, configuration.getString(CoreConfigurationType.PROFILE));
    assertEquals(Set.of("world", "game"), selectedProfiles(configuration));
  }

  @Test
  void missingRequiredPropertyFailsBeforeCustomExtensionProcessing() throws Exception {
    var configuration = new TrackingConfiguration();
    Path file = writeXml("<Properties><Property name=\"server-name\">Test</Property>"
        + "</Properties><Extension><Properties><Property name=\"profile\">world</Property>"
        + "</Properties></Extension>");

    var exception = assertThrows(IllegalArgumentException.class,
        () -> configuration.load(file.toString()));

    assertTrue(exception.getMessage().contains("profile"));
    assertFalse(configuration.extended);
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {" ", "\t\n", ",", " , , "})
  void blankProfileIsUnrestrictedAndCommaOnlyIsRejected(String value) throws Exception {
    var configuration = new DefaultCoreConfiguration();
    Path file = writeXml("<Properties><Property name=\"profile\">" + value
        + "</Property></Properties>");

    if (value.isBlank()) {
      configuration.load(file.toString());
      assertEquals(Set.of(), ConfigurationSource.read(file.toString(), getClass().getClassLoader()).profiles());
    } else {
      assertThrows(IllegalArgumentException.class, () -> configuration.load(file.toString()));
    }
  }

  @Test
  void reusedConfigurationDoesNotSatisfyMissingProfileWithPreviousValue() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    configuration.load(writeXml("<Properties><Property name=\"profile\">world</Property>"
        + "</Properties>").toString());
    assertEquals(Set.of("world"), selectedProfiles(configuration));
    Path missingProfile = writeXml("<Properties/>");

    assertThrows(IllegalArgumentException.class,
        () -> configuration.load(missingProfile.toString()));
  }

  @Test
  void clearingConfigurationAllowsLoadingANewProfileSelection() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    configuration.load(writeXml("<Properties><Property name=\"profile\">world</Property>"
        + "</Properties>").toString());
    // CommonConfiguration deliberately preserves the first value pushed for each key.
    configuration.clear();
    configuration.load(writeXml("<Properties><Property name=\"profile\">game</Property>"
        + "</Properties>").toString());

    assertEquals(Set.of("game"), selectedProfiles(configuration));
  }

  @Test
  void profileUnderNetworkPropertiesDoesNotSatisfyMandatoryServerProperty() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    Path file = writeXml("<Properties/><Network><Properties>"
        + "<Property name=\"profile\">world</Property></Properties></Network>");

    assertThrows(IllegalArgumentException.class, () -> configuration.load(file.toString()));
  }

  @Test
  void legacyIntervalsAreIgnoredWhileProfileStillLoads() throws Exception {
    var configuration = new DefaultCoreConfiguration();
    var schedules = new StringBuilder("<Configuration><Schedules>");
    for (String name : Set.of("ccu-scan-interval", "deadlock-scan-interval",
        "system-monitoring-interval", "traffic-counter-interval")) {
      assertTrue(CoreConfigurationType.getByValue(name) == null);
      schedules.append("<Task name='").append(name).append("'>invalid</Task>");
    }
    schedules.append("<Task name='removed-room-scan-interval'>17</Task>");
    schedules.append("<Task name='main-connection-keepalive-scan-interval'>5</Task></Schedules></Configuration>");
    configuration.load(writeXml("<Properties><Property name='profile'>world</Property></Properties>"
        + schedules).toString());
    assertFalse(configuration.isDefined(CoreConfigurationType.INTERVAL_MAIN_CONNECTION_KEEPALIVE_SCAN));
    assertTrue(CoreConfigurationType.getByValue("removed-room-scan-interval") == null);
    assertEquals(Set.of("world"), selectedProfiles(configuration));
  }

  @Test
  void startupUsesTheSelectedXmlEvenIfTheExternalFileDisappears() throws Exception {
    Path file = writeXml("<Properties><Property name='profile'>world</Property></Properties>");
    var source = ConfigurationSource.read(file.toString(), getClass().getClassLoader());
    assertEquals(Set.of("world"), source.profiles());
    Files.delete(file);

    var configuration = new TrackingConfiguration();
    configuration.loadPrepared(file.toString(), source);
    assertEquals(Set.of("world"), selectedProfiles(configuration));
    assertTrue(configuration.extended);
  }

  private Path writeXml(String body) throws Exception {
    Path file = Files.createTempFile(directory, "configuration-", ".xml");
    return Files.writeString(file, "<?xml version=\"1.0\"?><Server>" + body + "</Server>");
  }

  private Set<String> selectedProfiles(CoreConfiguration configuration) {
    return ProfileSelection.parseRequired(configuration.getString(CoreConfigurationType.PROFILE));
  }

  private static class TrackingConfiguration extends CoreConfiguration {

    private boolean extended;

    @Override
    protected void extend(Map<String, String> properties) {
      extended = true;
    }
  }
}
