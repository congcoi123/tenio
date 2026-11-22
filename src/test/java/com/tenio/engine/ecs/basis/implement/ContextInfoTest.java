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

package com.tenio.engine.ecs.basis.implement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContextInfoTest {

  private ContextInfo contextInfo;
  private String[] componentNames;
  private Class<?>[] componentTypes;

  @BeforeEach
  void setUp() {
    componentNames = new String[]{"Position", "Velocity", "Animation"};
    componentTypes = new Class<?>[]{Object.class, Object.class, Object.class};
    contextInfo = new ContextInfo("GameContext", componentNames, componentTypes, 3);
  }

  @Test
  void testGetName() {
    assertEquals("GameContext", contextInfo.getName());
  }

  @Test
  void testGetComponentNames() {
    var names = contextInfo.getComponentNames();
    assertNotNull(names);
    assertEquals(3, names.length);
    assertEquals("Position", names[0]);
    assertEquals("Velocity", names[1]);
    assertEquals("Animation", names[2]);
  }

  @Test
  void testGetComponentTypes() {
    var types = contextInfo.getComponentTypes();
    assertNotNull(types);
    assertEquals(3, types.length);
  }

  @Test
  void testGetNumberComponents() {
    assertEquals(3, contextInfo.getNumberComponents());
  }

  @Test
  void testToString() {
    String result = contextInfo.toString();
    assertNotNull(result);
    assertFalse(result.isEmpty());
    // should contain the name
    assertFalse(result.indexOf("GameContext") < 0);
  }
}
