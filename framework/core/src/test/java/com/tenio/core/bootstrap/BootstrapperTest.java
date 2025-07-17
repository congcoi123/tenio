/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.annotation.Bootstrap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Bootstrapper")
class BootstrapperTest {

  private Bootstrapper bootstrapper;

  @BeforeEach
  void setUp() throws Exception {
    bootstrapper = Bootstrapper.newInstance();
    // Reset singleton state for test isolation
    Field handlerField = Bootstrapper.class.getDeclaredField("bootstrapHandler");
    handlerField.setAccessible(true);
    handlerField.set(bootstrapper, null);
  }

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating again")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = Bootstrapper.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  @DisplayName("run() with a class not annotated with @Bootstrap should not initialize handler")
  void runWithNonBootstrapClassShouldNotInitHandler() {
    class NotBootstrap {
    }
    assertDoesNotThrow(() -> bootstrapper.run(NotBootstrap.class));
    assertNull(bootstrapper.getBootstrapHandler());
  }

  @Test
  @DisplayName("run() with a class annotated with @Bootstrap should initialize handler")
  void runWithBootstrapClassShouldInitHandler() {
    @Bootstrap
    class Annotated {
    }
    assertDoesNotThrow(() -> bootstrapper.run(Annotated.class));
    // Handler should be set after run
    assertNotNull(bootstrapper.getBootstrapHandler());
  }

  @Test
  @DisplayName("getBootstrapHandler() before run() should return null")
  void getBootstrapHandlerBeforeRunShouldReturnNull() {
    // New instance, not run yet
    Bootstrapper fresh = Bootstrapper.newInstance();
    assertNull(fresh.getBootstrapHandler());
  }

  @Test
  @DisplayName("run() should handle exceptions gracefully")
  void runShouldHandleException() {
    class Failing {
    }
    // Use a spy or mock if needed for injector, but here just check no throw
    assertDoesNotThrow(() -> bootstrapper.run(Failing.class));
  }
}
