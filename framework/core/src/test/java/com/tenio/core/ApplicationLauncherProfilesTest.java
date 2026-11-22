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

package com.tenio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.fixture.launcher.CommonLauncherComponent;
import com.tenio.core.fixture.launcher.DefaultLauncherComponent;
import com.tenio.core.fixture.launcher.GameLauncherComponent;
import com.tenio.core.fixture.launcher.LauncherProfileApplication;
import com.tenio.core.fixture.launcher.WorldLauncherComponent;
import com.tenio.core.fixture.launcher.ZUnexpectedLauncherComponent;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Unit Test Cases For ApplicationLauncher Profiles")
class ApplicationLauncherProfilesTest {

  @TempDir
  Path temporaryDirectory;

  @Test
  void xmlSelectionIsNormalizedAndInstalledBeforeComponentConstructorsRun() throws Exception {
    assertSelectedProfile(" world, world, ", "world");
  }

  @Test
  void xmlSelectionExcludesComponentsForOtherProfiles() throws Exception {
    assertSelectedProfile("game", "game");
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {" , , "})
  void missingOrBlankXmlSelectionFailsBeforeAnyApplicationConstructor(String selection) throws Exception {
    Path configuration = writeConfiguration(selection);
    var result = launch("xml", configuration.toString());

    assertEquals(1, result.exitCode(), result.output());
    assertFalse(Files.exists(result.marker()), result.output());
    assertTrue(result.output().contains("Mandatory server property 'profile'"), result.output());
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "empty"})
  void omittedArgumentsLoadTheEmbeddedDefaultProfileBeforeConstruction(String mode) throws Exception {
    var result = launch(mode);

    assertEquals(1, result.exitCode(), result.output());
    assertTrue(Files.exists(result.marker()), result.output());
    assertEquals(List.of("common", "default"), Files.readAllLines(result.marker()), result.output());
    assertTrue(result.output().contains("Launcher profile probe completed: default"), result.output());
  }

  private void assertSelectedProfile(String configured, String expected) throws Exception {
    var result = launch("xml", writeConfiguration(configured).toString());

    assertEquals(1, result.exitCode(), result.output());
    assertTrue(Files.exists(result.marker()), result.output());
    assertEquals(List.of("common", expected), Files.readAllLines(result.marker()), result.output());
    assertTrue(result.output().contains("Launcher profile probe completed: " + expected), result.output());
  }

  private Path writeConfiguration(String selection) throws Exception {
    Path configuration = temporaryDirectory.resolve("configuration.xml");
    String property = selection == null ? "" : "<Property name=\"profile\">" + selection + "</Property>";
    Files.writeString(configuration, "<Server><Properties>" + property + "</Properties></Server>");
    return configuration;
  }

  private ProbeResult launch(String... arguments) throws Exception {
    Path marker = temporaryDirectory.resolve("constructors.txt");
    Path output = temporaryDirectory.resolve("launcher.log");
    Path isolatedClasses = temporaryDirectory.resolve("probe-classes");
    // Production scans com.tenio.core.bootstrap, which also contains intentionally invalid test
    // fixtures. Give the child the real production classpath plus only this application probe.
    for (Class<?> fixture : List.of(LauncherProfileApplication.class, CommonLauncherComponent.class,
        DefaultLauncherComponent.class, GameLauncherComponent.class, WorldLauncherComponent.class,
        ZUnexpectedLauncherComponent.class)) {
      String resource = fixture.getName().replace('.', '/') + ".class";
      Path destination = isolatedClasses.resolve(resource);
      Files.createDirectories(destination.getParent());
      try (InputStream bytes = fixture.getClassLoader().getResourceAsStream(resource)) {
        if (bytes == null) {
          throw new IllegalStateException("Missing compiled launcher fixture: " + resource);
        }
        Files.copy(bytes, destination);
      }
    }

    // The suite's log4j2.xml suppresses output; the child asserts diagnostic text.
    // Own the child logger so those assertions do not depend on the parent classpath order.
    Files.writeString(isolatedClasses.resolve("log4j2.xml"), """
        <Configuration status="off">
          <Appenders><Console name="Probe" target="SYSTEM_OUT">
            <PatternLayout pattern="%m%n%throwable"/>
          </Console></Appenders>
          <Loggers><Root level="error"><AppenderRef ref="Probe"/></Root></Loggers>
        </Configuration>
        """);

    Path testClasses = Path.of(ApplicationLauncherProfilesTest.class.getProtectionDomain()
        .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
    String testClasspath = System.getProperty("surefire.test.class.path",
        System.getProperty("java.class.path"));
    List<String> classpath = new ArrayList<>();
    classpath.add(isolatedClasses.toAbsolutePath().toString());
    for (String entry : testClasspath.split(Pattern.quote(File.pathSeparator))) {
      Path path = Path.of(entry).toAbsolutePath().normalize();
      if (!path.equals(testClasses)) {
        classpath.add(path.toString());
      }
    }

    Path java = Path.of(System.getProperty("java.home"), "bin", "java");
    if (!Files.isRegularFile(java)) {
      java = java.resolveSibling("java.exe");
    }
    List<String> command = new ArrayList<>(List.of(java.toString(),
        "-D" + LauncherProfileApplication.MARKER_PROPERTY + "=" + marker.toAbsolutePath(),
        "-cp", String.join(File.pathSeparator, classpath), LauncherProfileApplication.class.getName()));
    command.addAll(List.of(arguments));
    Process process = new ProcessBuilder(command)
        .directory(temporaryDirectory.toFile())
        .redirectErrorStream(true)
        .redirectOutput(output.toFile())
        .start();
    try {
      boolean finished = process.waitFor(20, TimeUnit.SECONDS);
      assertTrue(finished, () -> "Launcher probe did not exit within 20 seconds. Output: " + output);
      return new ProbeResult(process.exitValue(), Files.readString(output), marker);
    } finally {
      if (process.isAlive()) {
        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);
      }
    }
  }

  private record ProbeResult(int exitCode, String output, Path marker) {}
}
