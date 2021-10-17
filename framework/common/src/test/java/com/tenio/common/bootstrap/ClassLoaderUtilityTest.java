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

package com.tenio.common.bootstrap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.common.bootstrap.utility.ClassLoaderUtility;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public final class ClassLoaderUtilityTest {

  @Test
  public void scanPackageShouldReturnListOfClasses() throws IOException, ClassNotFoundException {
    var listClasses = ClassLoaderUtility.getClasses("com.tenio.common.bootstrap.loader");
    assertAll("scanPackageShouldReturnListOfClasses",
        () -> assertEquals(listClasses.size(), 3),
        () -> assertTrue(
            listClasses.stream().map(clazz -> clazz.getName()).anyMatch(name -> name.equals(
                "com.tenio.common.bootstrap.loader.TestClassA"))),
        () -> assertTrue(
            listClasses.stream().map(clazz -> clazz.getName()).anyMatch(name -> name.equals(
                "com.tenio.common.bootstrap.loader.TestClassB"))),
        () -> assertTrue(
            listClasses.stream().map(clazz -> clazz.getName()).anyMatch(name -> name.equals(
                "com.tenio.common.bootstrap.loader.TestClassC")))
    );
  }

  @Test
  public void scanNonExistedPackageShouldReturnEmptyArray()
      throws IOException, ClassNotFoundException {
    var listClasses = ClassLoaderUtility.getClasses("com.tenio.common.bootstrap.null");
    assertTrue(listClasses.isEmpty());
  }

  @Test
  public void scanExternalLibraryShouldReturnListOfClasses()
      throws IOException, ClassNotFoundException {
    var listClasses = ClassLoaderUtility.getClasses("com.google.common.annotations");
    assertTrue(listClasses.size() > 0);
  }
}
