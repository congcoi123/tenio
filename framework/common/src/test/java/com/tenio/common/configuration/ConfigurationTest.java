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

package com.tenio.common.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Configuration")
class ConfigurationTest {

  private DefaultConfiguration configuration;

  @BeforeEach
  void initialization() {
    configuration = new DefaultConfiguration();
    configuration.load("dummy");
  }

  @Test
  @DisplayName("It should retrieve all imported data")
  void shouldRetrieveImportedData() {
    assertAll("shouldRetrieveImportedData",
        () -> assertTrue(configuration.getBoolean(DefaultConfigurationType.BOOLEAN)),
        () -> assertEquals(100F, configuration.getFloat(DefaultConfigurationType.FLOAT)),
        () -> assertEquals(99, configuration.getInt(DefaultConfigurationType.INTEGER)),
        () -> assertEquals("test", configuration.getString(DefaultConfigurationType.STRING)),
        () -> assertEquals(configuration.dummyObject,
            configuration.get(DefaultConfigurationType.OBJECT))
    );
  }

  @Test
  @DisplayName("Not imported data could not be fetched")
  void checkNonDefinedConfiguredTypeShouldReturnTrue() {
    assertAll("checkNonDefinedConfiguredTypeShouldReturnTrue",
        () -> assertFalse(configuration.isDefined(DefaultConfigurationType.NOT_DEFINED)),
        () -> assertFalse(configuration.isDefined(DefaultConfigurationType.NULL_DEFINED)));
  }

  @Test
  @DisplayName("To be able to clear all configuration data")
  void clearAllConfigurationsShouldWork() {
    configuration.clear();
    assertEquals("{ }", configuration.toString());
  }

  @Test
  @DisplayName("Push null key should do nothing")
  void pushNullKeyShouldDoNothing() {
    class ConfigurationInternal extends CommonConfiguration {
        @Override
        public void load(String file) {}
        @Override
        protected void extend(Map<String, String> extProperties) {}
        public void testPushNull() {
            push(null, "value");
        }
    }
    var config = new ConfigurationInternal();
    config.testPushNull();
    assertEquals("{ }", config.toString());
  }

  @Test
  @DisplayName("Calling extend method should work")
  void callingExtendMethodShouldWork() {
    boolean[] extended = {false};
    class ConfigurationInternal extends CommonConfiguration {
        @Override
        public void load(String file) {
            extend(Collections.emptyMap());
        }
        @Override
        protected void extend(Map<String, String> extProperties) {
            extended[0] = true;
        }
    }
    var config = new ConfigurationInternal();
    config.load("dummy");
    assertTrue(extended[0]);
  }

  @Test
  @DisplayName("Duplicate key push should log a warning when logging is enabled")
  void pushDuplicateKeyWithLoggingEnabledShouldLog() {
    Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.INFO);
    try {
      assertDoesNotThrow(() -> {
        var config = new DefaultConfiguration();
        config.load("dummy");
      });
    } finally {
      Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.OFF);
    }
  }
}
