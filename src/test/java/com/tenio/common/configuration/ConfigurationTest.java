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

package com.tenio.common.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        () -> assertEquals(configuration.getFloat(DefaultConfigurationType.FLOAT), 100F),
        () -> assertEquals(configuration.getInt(DefaultConfigurationType.INTEGER), 99),
        () -> assertEquals(configuration.getString(DefaultConfigurationType.STRING), "test"),
        () -> assertEquals(configuration.get(DefaultConfigurationType.OBJECT),
            configuration.dummyObject)
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
    assertEquals(configuration.toString(), "{}");
  }
}
