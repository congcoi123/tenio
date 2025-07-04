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

package com.tenio.engine.ecs.basis.implement;

import java.util.Arrays;

/**
 * The context information.
 */
public final class ContextInfo {

  /**
   * List of component names of an entity.
   */
  private final String[] componentNames;
  /**
   * List of component classes of an entity.
   */
  private final Class<?>[] componentTypes;
  /**
   * The context's name.
   */
  private final String name;
  /**
   * The number of component for each entity.
   */
  private final int numberComponents;

  /**
   * Initialization.
   *
   * @param name             the context's name
   * @param componentNames   list of component's names
   * @param componentTypes   list of component's types
   * @param numberComponents the number of components
   */
  public ContextInfo(String name, String[] componentNames, Class<?>[] componentTypes,
                     int numberComponents) {
    this.name = name;
    this.componentNames = componentNames;
    this.componentTypes = componentTypes;
    this.numberComponents = numberComponents;
  }

  public String getName() {
    return name;
  }

  public String[] getComponentNames() {
    return componentNames;
  }

  public Class<?>[] getComponentTypes() {
    return componentTypes;
  }

  public int getNumberComponents() {
    return numberComponents;
  }

  @Override
  public String toString() {
    return String.format("ContextInfo{name=%s, numberComponents=%d, componentNames=%s}", name,
        numberComponents,
        Arrays.toString(componentNames));
  }
}
