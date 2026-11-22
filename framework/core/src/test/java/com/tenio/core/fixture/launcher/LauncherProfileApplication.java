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

package com.tenio.core.fixture.launcher;

import com.tenio.core.ApplicationLauncher;
import com.tenio.core.bootstrap.annotation.Bootstrap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** A fork-only launcher probe that stops during DI, before any server sockets are opened. */
@Bootstrap
public class LauncherProfileApplication {

  public static final String MARKER_PROPERTY = "tenio.test.launcher.marker";

  public static void main(String[] args) {
    String[] parameters = switch (args[0]) {
      case "null" -> null;
      case "empty" -> new String[0];
      case "xml" -> new String[] {args[1]};
      default -> throw new IllegalArgumentException("Unknown launcher probe mode");
    };
    ApplicationLauncher.run(LauncherProfileApplication.class, parameters);
    throw new AssertionError("The launcher probe must stop before starting the server");
  }

  public static void recordConstruction(String profile, boolean stop) {
    try {
      Files.writeString(Path.of(System.getProperty(MARKER_PROPERTY)), profile + "\n",
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    if (stop) {
      throw new IllegalStateException("Launcher profile probe completed: " + profile);
    }
  }
}
