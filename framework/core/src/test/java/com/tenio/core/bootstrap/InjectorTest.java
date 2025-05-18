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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.bean.TestBeanClass;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.bootstrap.test.BootstrapComponent;
import com.tenio.core.bootstrap.test.impl.TestClassA;
import com.tenio.core.bootstrap.test.impl.TestClassAlone;
import com.tenio.core.bootstrap.test.impl.TestClassCCopy;
import com.tenio.core.bootstrap.test.inf.TestInterfaceA;
import com.tenio.core.bootstrap.test.inf.TestInterfaceC;
import com.tenio.core.custom.DisabledTestFindingSolution;
import com.tenio.core.exception.DuplicatedBeanCreationException;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.exception.NoImplementedClassFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Injector")
class InjectorTest {

  private final Injector injector = Injector.newInstance();

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating again")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = Injector.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  @DisplayName("All variables in the BootstrapComponent class should have expected values")
  void scanPackageShouldRetrieveExpectedValues()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.test.impl",
        "com.tenio.core.bootstrap.test.inf", "com.tenio.core.bootstrap.test.factory",
        "com.tenio.core.bootstrap.bean");
    var bean = injector.getBean(BootstrapComponent.class);
    assertAll("scanPackageShouldRetrieveExpectedValues",
        () -> assertInstanceOf(TestClassA.class, bean.a),
        () -> assertNull(bean.b),
        () -> assertInstanceOf(TestClassCCopy.class, bean.c),
        () -> assertInstanceOf(TestClassAlone.class, bean.alone),
        () -> assertInstanceOf(TestBeanClass.class, bean.bean),
        () -> assertEquals("common", bean.beanCommon.text()),
        () -> assertEquals("common a", bean.beanCommonA.text()),
        () -> assertEquals("common b", bean.beanCommonB.text())
    );
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of A")
  void scanPackageShouldRetrieveInstanceOfA()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).a;
    assertAll("scanPackageShouldRetrieveInstanceOfA",
        () -> assertInstanceOf(TestInterfaceA.class, bean),
        () -> assertInstanceOf(TestClassA.class, bean)
    );
  }

  @Test
  @DisplayName("After scanning the package should not retrieve an instance of B (null)")
  void scanPackageShouldRetrieveNullInstanceOfB()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).b;
    assertNull(bean);
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of C because of using " +
      "@AutowiredQualifier in BootstrapComponent class")
  void scanPackageShouldRetrieveInstanceOfC()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).c;
    assertAll("scanPackageShouldRetrieveInstanceOfC",
        () -> assertInstanceOf(TestInterfaceC.class, bean),
        () -> assertInstanceOf(TestClassCCopy.class, bean)
    );
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of alone - a class with " +
      "@Component and without implementing any interface")
  void scanPackageShouldRetrieveInstanceOfAlone()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).alone;
    assertInstanceOf(TestClassAlone.class, bean);
  }

  @Test
  @DisplayName("After scanning the package should throw an exception because there are more than " +
      "2 classes implement same interface without @AutowiredQualifier declaration")
  void scanPackageShouldThrowExceptionInInstanceOfD() {
    assertThrows(MultipleImplementedClassForInterfaceException.class, () -> injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl", "com.tenio.core" +
        ".bootstrap.test.inf", "com.tenio.core.bootstrap.exception.one"));
  }

  @Test
  @DisplayName("After scanning the package should throw an exception because there is no class " +
      "implement declared interface")
  void scanPackageShouldThrowExceptionInInstanceOfE() {
    assertThrows(NoImplementedClassFoundException.class, () -> injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl", "com.tenio.core" +
        ".bootstrap.test.inf", "com.tenio.core.bootstrap.exception.two"));
  }

  @Test
  @DisplayName("After scanning the package should retrieve an instance of bean class - a class " +
      "declared by @Bean and @BeanFactory annotations")
  void scanPackageShouldRetrieveInstanceOfBean()
      throws ClassNotFoundException, InvocationTargetException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, DuplicatedBeanCreationException {
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");

    var bean = injector.getBean(BootstrapComponent.class).bean;
    assertAll("scanPackageShouldRetrieveInstanceOfBean",
        () -> assertInstanceOf(TestBeanClass.class, bean),
        () -> assertEquals(2022, bean.getValue()),
        () -> assertEquals("This is a bean class", bean.toString())
    );
  }

  @DisabledTestFindingSolution
  @DisplayName("Attempt fetching null bean should return null")
  void getNullBeanShouldReturnNull() {
  }
}
