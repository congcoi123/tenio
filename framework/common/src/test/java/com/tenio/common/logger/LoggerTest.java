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

package com.tenio.common.logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Logger")
class LoggerTest {

  private static final class TestLogger extends AbstractLogger {
  }

  private final TestLogger logger = new TestLogger();

  @BeforeAll
  static void initialization() {
    LoggerBootstrap.initialize("log4j2.example.xml", "log4j2.xml");
  }

  @Test
  void testLoggerBootstrap() {
    // Already called in BeforeAll, test different branches
    LoggerBootstrap.initialize("non-existed.xml", "non-existed.xml");
    System.setProperty("log4j.configurationFile", "log4j2.xml");
    LoggerBootstrap.initialize("log4j2.example.xml", "log4j2.xml");
    System.clearProperty("log4j.configurationFile");
  }

  @Test
  void testAbstractLoggerMethods() {
    var where = logger.buildgen("where");
    var tag = logger.buildgen("tag");
    var msg = logger.buildgen("msg");

    assertDoesNotThrow(() -> {
      logger.info(where, tag, msg);
      logger.info(logger.buildgen("where"), "tag", logger.buildgen("msg"));
      logger.info(logger.buildgen("where"), logger.buildgen("tag"), new Object());
      logger.info("where", "tag", logger.buildgen("msg"));
      logger.info("where", "tag", new Object());
      logger.info(logger.buildgen("tag"), logger.buildgen("msg"));
      logger.info("tag", logger.buildgen("msg"));
      logger.info("tag", new Object());

      logger.warn("warn", "msg");
      logger.error("error", "msg");
      logger.error(new RuntimeException("test"), "extra");
      logger.error(new RuntimeException("test"), logger.buildgen("msg"));
    });
  }

  @Test
  void testLoggerLevels() {
    assertNotNull(logger.isErrorEnabled());
    assertNotNull(logger.isWarnEnabled());
    assertNotNull(logger.isInfoEnabled());
    assertNotNull(logger.isDebugEnabled());
    assertNotNull(logger.isTraceEnabled());
  }

  @Test
  void testSystemLogger() {
    SystemLogger systemLogger = new SystemLogger() {};
    assertDoesNotThrow(() -> {
        systemLogger.trace("trace", "msg");
        systemLogger.debug("debug", "msg");
        systemLogger.info("info", "msg");
        systemLogger.warn("warn", "msg");
        systemLogger.error("error", "msg");
        systemLogger.error(new RuntimeException("test"), "extra");
    });
  }

  @Test
  @DisplayName("All logging body code should execute when logging level is enabled")
  void testLoggingMethodsWhenEnabled() {
    Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.TRACE);
    try {
      assertDoesNotThrow(() -> {
        logger.info(logger.buildgen("w"), logger.buildgen("t"), logger.buildgen("m"));
        logger.info(logger.buildgen("w"), "t", logger.buildgen("m"));
        logger.info(logger.buildgen("w"), logger.buildgen("t"), new Object());
        logger.info("w", "t", logger.buildgen("m"));
        logger.info("w", "t", new Object());
        logger.info(logger.buildgen("t"), logger.buildgen("m"));
        logger.info("t", logger.buildgen("m"));
        logger.info("t", new Object());
        logger.warn("warn");
        logger.error("error");
        logger.error(new RuntimeException("e"));
        logger.error(new RuntimeException("e"), "extra");
        logger.error(new RuntimeException("e"), logger.buildgen("msg"));
        logger.error(new RuntimeException("e"), logger.buildgen(""));
      });

      SystemLogger sysLogger = new SystemLogger() {};
      assertDoesNotThrow(() -> {
        sysLogger.debugEvent("event", "a", "b");
        sysLogger.debugEvent("event");
        sysLogger.debug("debug", "a", "b");
        sysLogger.trace("type", "a", "b");
        sysLogger.trace("type");
        sysLogger.trace("where", new Object(), "tag", "msg");
      });
    } finally {
      Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, Level.OFF);
    }
  }

  @Test
  @DisplayName("LoggerBootstrap should use framework config when no user config exists")
  void testLoggerBootstrapWithFrameworkConfig() {
    assertDoesNotThrow(
        () -> LoggerBootstrap.initialize("log4j2-test.xml", "no-such-config.xml"));
  }
}
