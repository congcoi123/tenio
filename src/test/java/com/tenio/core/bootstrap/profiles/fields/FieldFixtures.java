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

package com.tenio.core.bootstrap.profiles.fields;

import com.tenio.core.bootstrap.annotation.Asynchronous;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Component;

/** Profile selection chooses participation; qualifiers continue to choose bean identities. */
public final class FieldFixtures {

  public interface Service {}

  public interface MissingService {}

  public record Product(String name) {}

  @Component(profile = "world")
  public static class WorldService implements Service {}

  @Component(profile = "game")
  public static class GameService implements Service {}

  @BeanFactory
  public static class NamedFactory {
    @Bean
    public Product defaultProduct() {
      return new Product("default");
    }

    @Bean("world")
    public Product namedProduct() {
      return new Product("named-world");
    }

    @Bean(value = "game-only", profile = "game")
    public Product gameOnlyProduct() {
      return new Product("named-game");
    }

    @Bean("null-product")
    public Product nullProduct() {
      return null;
    }
  }

  @Component
  public static class Consumer {
    @Autowired(profile = "world")
    @AutowiredQualifier(clazz = WorldService.class)
    public Service world;

    @Autowired(profile = "game")
    @AutowiredQualifier(clazz = GameService.class)
    public Service game;

    @Autowired
    public Product defaultProduct;

    @Autowired
    @AutowiredQualifier(name = "world", profile = "game")
    public Product namedProduct;

    @Autowired(profile = "world")
    @AutowiredQualifier(name = "world", profile = "game")
    public Product bothProfiles;

    @Autowired(profile = "never")
    public MissingService inactiveRequired;

    @AutowiredAcceptNull(profile = "world")
    public GameService optionalGame;

    @AutowiredAcceptNull
    @AutowiredQualifier(clazz = GameService.class)
    public Service optionalQualifiedGame;

    @AutowiredAcceptNull
    @AutowiredQualifier(name = "game-only")
    public Product optionalNamedGame;

    @AutowiredAcceptNull
    @AutowiredQualifier(name = "null-product")
    public Product optionalNullProduct;

    @AutowiredAcceptNull
    @Asynchronous(profile = "game")
    public GameService asynchronousGame;

    @AutowiredAcceptNull
    public MissingService optionalMissing;
  }

  @Component(profile = "require-game")
  public static class RequiredGameConsumer {
    @Autowired
    @AutowiredQualifier(clazz = GameService.class)
    public Service game;
  }

  @Component(profile = "require-named-game")
  public static class RequiredNamedGameConsumer {
    @Autowired
    @AutowiredQualifier(name = "game-only")
    public Product game;
  }

  @Component(profile = "require-null-product")
  public static class RequiredNullProductConsumer {
    @Autowired
    @AutowiredQualifier(name = "null-product")
    public Product product;
  }

  private FieldFixtures() {}
}
