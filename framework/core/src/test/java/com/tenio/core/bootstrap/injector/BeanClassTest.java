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

package com.tenio.core.bootstrap.injector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For BeanClass")
class BeanClassTest {

  @Test
  @DisplayName("Test create and access components")
  void testCreateAndAccessComponents() {
    BeanClass beanClass = new BeanClass(String.class, "myBean");
    assertEquals(String.class, beanClass.clazz());
    assertEquals("myBean", beanClass.name());
  }

  @Test
  @DisplayName("Test create with null name")
  void testCreateWithNullName() {
    BeanClass beanClass = new BeanClass(Integer.class, null);
    assertEquals(Integer.class, beanClass.clazz());
    assertNull(beanClass.name());
  }

  @Test
  @DisplayName("Test equality when same values")
  void testEqualityWhenSameValues() {
    BeanClass a = new BeanClass(String.class, "bean");
    BeanClass b = new BeanClass(String.class, "bean");
    assertEquals(a, b);
  }

  @Test
  @DisplayName("Test inequality when different values")
  void testInequalityWhenDifferentValues() {
    BeanClass a = new BeanClass(String.class, "bean");
    BeanClass b = new BeanClass(Integer.class, "bean");
    assertNotEquals(a, b);
  }

  @Test
  @DisplayName("Test hash code consistency for same values")
  void testHashCodeConsistency() {
    BeanClass a = new BeanClass(String.class, "bean");
    BeanClass b = new BeanClass(String.class, "bean");
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  @DisplayName("Test toString contains component values")
  void testToStringContainsValues() {
    BeanClass beanClass = new BeanClass(String.class, "myBean");
    String str = beanClass.toString();
    assertTrue(str.contains("String") || str.contains("java.lang.String"));
    assertTrue(str.contains("myBean"));
  }
}
