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

package com.tenio.core.bootstrap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenio.core.bootstrap.injector.BeanClass;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.exception.DuplicatedBeanCreationException;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.DuplicateFactory;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.FirstClient;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.FirstController;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.FirstService;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.FirstSystem;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.InactiveConsumer;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.InheritedParent;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.NamedConsumer;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.NamedService;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.OptionalConsumer;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.QualifiedConsumer;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.RuntimeConsumer;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.RuntimeProduct;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.RuntimeService;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.SecondClient;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.SecondController;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.SecondService;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.SecondSystem;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.SerializableTarget;
import com.tenio.core.fixture.injector.diagnostics.CollisionDiagnosticsFixtures.Service;
import com.tenio.core.utility.HttpUtility;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regression tests for actionable collision diagnostics, captured through the public logger. */
class InjectorCollisionDiagnosticsTest {

  private final Injector injector = new Injector();
  private LoggerContext loggerContext;
  private Configuration loggingConfiguration;
  private LoggerConfig previousLogger;
  private WarningAppender warnings;

  @BeforeEach
  void captureWarnings() throws Exception {
    resetInjector();
    loggerContext = (LoggerContext) LogManager.getContext(false);
    loggingConfiguration = loggerContext.getConfiguration();
    previousLogger = loggingConfiguration.getLoggers().get(Injector.class.getName());
    warnings = new WarningAppender();
    warnings.start();
    var capture = new LoggerConfig(Injector.class.getName(), Level.WARN, false);
    capture.addAppender(warnings, Level.WARN, null);
    loggingConfiguration.removeLogger(Injector.class.getName());
    loggingConfiguration.addLogger(Injector.class.getName(), capture);
    loggerContext.updateLoggers();
    assertTrue(injector.isWarnEnabled(), "No-warning assertions must run with warnings enabled");
  }

  @AfterEach
  void restoreLoggingAndInjector() throws Exception {
    try {
      resetInjector();
    } finally {
      if (loggingConfiguration != null) {
        loggingConfiguration.removeLogger(Injector.class.getName());
        if (previousLogger != null) {
          loggingConfiguration.addLogger(Injector.class.getName(), previousLogger);
        }
        loggerContext.updateLoggers();
      }
      if (warnings != null) {
        warnings.stop();
      }
    }
  }

  @Test
  void objectMapperAndHttpUtilityDoNotWarnForTheirSharedSerializableMarker() throws Exception {
    scan("serializable-unused");

    assertNotNull(injector.getBean(ObjectMapper.class));
    assertSame(HttpUtility.INSTANCE, injector.getBean(HttpUtility.class));
    assertNoCollisionWarnings();
  }

  @Test
  void unusedServiceOverlapIsQuietButAnAmbiguousGetBeanLookupWarnsAndFails() throws Exception {
    scan("service-unused");

    assertNotNull(injector.getBean(FirstService.class));
    assertNotNull(injector.getBean(SecondService.class));
    assertNoCollisionWarnings();

    assertThrows(IllegalStateException.class, () -> injector.getBean(Service.class));
    assertDiagnostic("[INJECTION AMBIGUITY]", "service-unused", "getBean()", Service.class.getName());
    assertNoProfileCollisionWarnings();
  }

  @Test
  void classQualifiedServiceInjectionDoesNotWarn() throws Exception {
    scan("service-qualified");

    var consumer = injector.getBean(QualifiedConsumer.class);
    assertNotNull(consumer);
    assertSame(injector.getBean(FirstService.class), consumer.dependency);
    assertNoCollisionWarnings();
  }

  @Test
  void nameQualifiedFactoryInjectionDoesNotWarn() throws Exception {
    scan("name-qualified");

    var consumer = injector.getBean(NamedConsumer.class);
    var blue = assertInstanceOf(NamedService.class, injector.getClassBeansMap()
        .get(new BeanClass(NamedService.class, "blue")));
    assertNotNull(consumer);
    assertSame(blue, consumer.dependency);
    assertNoCollisionWarnings();
  }

  @Test
  void profileExcludedRequiredAndOptionalFieldsDoNotProduceAmbiguityWarnings() throws Exception {
    scan("inactive-fields");

    var consumer = injector.getBean(InactiveConsumer.class);
    assertNotNull(consumer);
    assertAll(
        () -> assertNull(consumer.required),
        () -> assertNull(consumer.optional));
    assertNoCollisionWarnings();
  }

  @Test
  void requiredSerializableInjectionWarnsWithItsTargetAndFails() {
    assertAmbiguousField("serializable-required", SerializableTarget.class, Serializable.class);
  }

  @Test
  void optionalServiceInjectionStillWarnsAndFailsWhenProvidersAreAmbiguous() {
    assertAmbiguousField("service-optional", OptionalConsumer.class, Service.class);
  }

  @Test
  void inheritedInjectionWarningIdentifiesTheDeclaringClassAndField() {
    assertAmbiguousField("service-inherited", InheritedParent.class, Service.class);
  }

  @Test
  void ambiguousRuntimeFactoryProductsReportBothNameAndClassQualifiers() {
    assertAmbiguousField("runtime-qualified-ambiguous",
        RuntimeConsumer.class, RuntimeService.class);
    assertDiagnostic("[INJECTION AMBIGUITY]", "chosen", RuntimeProduct.class.getName());
  }

  @Test
  void duplicateFactoryRegistrationsStillWarnAndFail() {
    assertThrows(DuplicatedBeanCreationException.class, () -> scan("duplicate-beans"));
    assertDiagnostic("[PROFILE COLLISION]", "duplicate-beans",
        "bean:" + NamedService.class.getName() + ":duplicate", DuplicateFactory.class.getName());
  }

  @Test
  void duplicateClientAndSystemCommandRegistrationsStillWarn() throws Exception {
    injector.setProfiles(Set.of("duplicate-commands", "monitor"));
    injector.scanPackages(CollisionDiagnosticsFixtures.class);

    assertDiagnostic("[PROFILE COLLISION]", "duplicate-commands", "client-command:84",
        FirstClient.class.getName(), SecondClient.class.getName());
    assertDiagnostic("[PROFILE COLLISION]", "duplicate-commands",
        "system-command:diagnostics-status",
        FirstSystem.class.getName(), SecondSystem.class.getName());
  }

  @Test
  void duplicateNormalizedRestPathsStillWarnAndFail() {
    assertThrows(DuplicatedBeanCreationException.class, () -> scan("duplicate-rest"));
    assertDiagnostic("[PROFILE COLLISION]", "duplicate-rest", "rest:diagnostics/same",
        FirstController.class.getName(), SecondController.class.getName());
  }

  private void assertAmbiguousField(String profile, Class<?> owner, Class<?> dependency) {
    assertThrows(MultipleImplementedClassForInterfaceException.class, () -> scan(profile));
    assertDiagnostic("[INJECTION AMBIGUITY]", profile, owner.getName() + ".dependency",
        dependency.getName());
    assertNoProfileCollisionWarnings();
  }

  private void assertDiagnostic(String tag, String... context) {
    assertTrue(warnings.messages.stream().anyMatch(message -> message.contains(tag)
            && Arrays.stream(context).allMatch(message::contains)),
        () -> "Expected " + tag + " with " + Arrays.toString(context) + ": " + warnings.messages);
  }

  private void assertNoCollisionWarnings() {
    var diagnostics = warnings.messages.stream()
        .filter(message -> message.contains("[PROFILE COLLISION]")
            || message.contains("[INJECTION AMBIGUITY]"))
        .toList();
    assertTrue(diagnostics.isEmpty(), () -> "Unexpected collision diagnostics: " + diagnostics);
  }

  private void assertNoProfileCollisionWarnings() {
    assertTrue(warnings.messages.stream()
            .noneMatch(message -> message.contains("[PROFILE COLLISION]")),
        () -> "An ambiguous lookup must not report a registration collision: " + warnings.messages);
  }

  private void resetInjector() throws Exception {
    injector.setProfiles(Set.of());
    injector.scanPackages(null);
  }

  private void scan(String profile) throws Exception {
    injector.setProfiles(Set.of(profile));
    injector.scanPackages(CollisionDiagnosticsFixtures.class);
  }

  private static final class WarningAppender extends AbstractAppender {
    private final List<String> messages = new ArrayList<>();

    private WarningAppender() {
      super("InjectorCollisionDiagnosticsTest", null, PatternLayout.createDefaultLayout(),
          false, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      if (Level.WARN.equals(event.getLevel())) {
        messages.add(event.getMessage().getFormattedMessage());
      }
    }
  }
}
