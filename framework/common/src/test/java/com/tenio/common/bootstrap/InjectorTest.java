/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.common.bootstrap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.bootstrap.bean.TestBeanClass;
import com.tenio.common.bootstrap.injector.Injector;
import com.tenio.common.bootstrap.test.BootstrapComponent;
import com.tenio.common.bootstrap.test.impl.TestClassA;
import com.tenio.common.bootstrap.test.impl.TestClassAlone;
import com.tenio.common.bootstrap.test.impl.TestClassCCopy;
import com.tenio.common.bootstrap.test.inf.TestInterfaceA;
import com.tenio.common.bootstrap.test.inf.TestInterfaceC;
import com.tenio.common.custom.DisabledTestFindingSolution;
import com.tenio.common.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.common.exception.NoImplementedClassFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Injector")
class InjectorTest {

  private final Injector injector = Injector.newInstance();

  @Test
  @DisplayName("After scanning the package should retrieve an instance of A")
  void scanPackageShouldRetrieveInstanceOfA()
      throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.common.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).a;
    assertAll("scanPackageShouldRetrieveInstanceOfA",
        () -> assertTrue(bean instanceof TestInterfaceA),
        () -> assertTrue(bean instanceof TestClassA)
    );
  }

  @Test
  @DisplayName("After scanning the package should not retrieve an instance of B (null)")
  void scanPackageShouldRetrieveNullInstanceOfB()
      throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.common.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).b;
    assertEquals(bean, null);
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of C because of using " +
      "@AutowiredQualifier in BootstrapComponent class")
  void scanPackageShouldRetrieveInstanceOfC()
      throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.common.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).c;
    assertAll("scanPackageShouldRetrieveInstanceOfC",
        () -> assertTrue(bean instanceof TestInterfaceC),
        () -> assertTrue(bean instanceof TestClassCCopy)
    );
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of alone - a class with " +
      "@Component and without implementing any interface")
  void scanPackageShouldRetrieveInstanceOfAlone()
      throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.common.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).alone;
    assertTrue(bean instanceof TestClassAlone);
  }

  @Test
  @DisplayName("After scanning the package should throw an exception because there are more than " +
      "2 classes implement same interface without @AutowiredQualifier declaration")
  void scanPackageShouldThrowExceptionInInstanceOfD() {
    assertThrows(MultipleImplementedClassForInterfaceException.class, () -> {
      injector.scanPackages(null, "com.tenio.common.bootstrap.test.impl", "com.tenio.common" +
          ".bootstrap.test.inf", "com.tenio.common.bootstrap.exception.one");
    });
  }

  @Test
  @DisplayName("After scanning the package should throw an exception because there is no class " +
      "implement declared interface")
  void scanPackageShouldThrowExceptionInInstanceOfE() {
    assertThrows(NoImplementedClassFoundException.class, () -> {
      injector.scanPackages(null, "com.tenio.common.bootstrap.test.impl", "com.tenio.common" +
          ".bootstrap.test.inf", "com.tenio.common.bootstrap.exception.two");
    });
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of bean class - a class " +
      "declared by @Bean and @Configuration annotations")
  void scanPackageShouldRetrieveInstanceOfBean()
      throws IOException, ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.common.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).bean;
    assertAll("scanPackageShouldRetrieveInstanceOfBean",
        () -> assertTrue(bean instanceof TestBeanClass),
        () -> assertEquals(2022, bean.getValue()),
        () -> assertEquals("This is a bean class", bean.toString())
    );
  }

  @DisabledTestFindingSolution
  @DisplayName("Attempt fetching null bean should return null")
  void getNullBeanShouldReturnNull() {
  }
}
