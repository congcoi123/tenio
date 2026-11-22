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

package com.tenio.core.bootstrap.profiles.factories;

import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Component;

/** Factory, method and return type restrictions each gate factory invocation. */
public final class FactoryFixtures {

  public static int factoryConstructions;
  public static int worldInvocations;
  public static int gameInvocations;
  public static int restrictedReturnInvocations;

  public record Product(String origin) {}

  @Component(profile = "game")
  public interface GameProduct {}

  public static class GameProductImpl implements GameProduct {}

  @BeanFactory(profile = "world")
  public static class WorldFactory {
    public WorldFactory() {
      factoryConstructions++;
    }

    @Bean(profile = "world")
    public Product worldProduct() {
      worldInvocations++;
      return new Product("world");
    }

    @Bean(value = "game", profile = "game")
    public Product gameProduct() {
      gameInvocations++;
      return new Product("game");
    }

    @Bean
    public GameProduct restrictedReturn() {
      restrictedReturnInvocations++;
      return new GameProductImpl();
    }

    @Bean(profile = "invalid")
    public int inactiveInvalidReturnType() {
      throw new AssertionError("An inactive factory method must never be invoked");
    }
  }

  private FactoryFixtures() {}
}
