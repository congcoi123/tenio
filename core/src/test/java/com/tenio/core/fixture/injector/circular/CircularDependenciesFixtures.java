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

package com.tenio.core.fixture.injector.circular;

import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Component;

/** Isolated dependency graphs for initialization-time circular field injection. */
public final class CircularDependenciesFixtures {

  public interface Missing {
  }

  @Component
  public static class Leaf {
  }

  @Component(profile = "self")
  public static class SelfNode {
    @Autowired public SelfNode self;
    @Autowired public Leaf leaf;
    @Autowired(profile = "excluded") public Missing excluded;
  }

  @Component(profile = "mutual")
  public static class MutualA {
    @Autowired public MutualB peer;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "mutual")
  public static class MutualB {
    @Autowired public MutualA peer;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "three")
  public static class ThreeA {
    @Autowired public ThreeB next;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "three")
  public static class ThreeB {
    @Autowired public ThreeC next;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "three")
  public static class ThreeC {
    @Autowired public ThreeA next;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "optional")
  public static class OptionalA {
    @AutowiredAcceptNull public OptionalB peer;
    @AutowiredAcceptNull public Missing missing;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "optional")
  public static class OptionalB {
    @Autowired public OptionalA peer;
    @Autowired public Leaf leaf;
  }

  @Component(profile = "lazy-named")
  public static class LazyNamedNode {
    @Autowired
    @AutowiredQualifier(name = "secondary")
    public LazyNamedNode secondary;

    @Autowired public Leaf leaf;
  }

  public interface NamedContract {
  }

  public abstract static class NamedParent {
    @Autowired private Leaf inheritedLeaf;

    public Leaf inheritedLeaf() {
      return inheritedLeaf;
    }
  }

  public static class NamedBlue extends NamedParent implements NamedContract {
    @Autowired
    @AutowiredQualifier(name = "red")
    public NamedContract peer;
  }

  public static class NamedRed extends NamedParent implements NamedContract {
    @Autowired
    @AutowiredQualifier(name = "blue")
    public NamedContract peer;
  }

  @BeanFactory(profile = "named-factory")
  public static class NamedFactory {
    @Bean("blue")
    public NamedContract blue() {
      return new NamedBlue();
    }

    @Bean("red")
    public NamedContract red() {
      return new NamedRed();
    }
  }

  @Component(profile = "named-factory")
  public static class NamedConsumer {
    @Autowired
    @AutowiredQualifier(name = "blue")
    public NamedContract blue;

    @Autowired
    @AutowiredQualifier(name = "red")
    public NamedContract red;
  }

  public interface RuntimeContract {
  }

  public abstract static class RuntimeParent {
    @Autowired private Leaf inheritedLeaf;

    public Leaf inheritedLeaf() {
      return inheritedLeaf;
    }
  }

  public static class RuntimeProduct extends RuntimeParent implements RuntimeContract {
    @Autowired public RuntimeContract self;
    @Autowired public Leaf ownLeaf;
  }

  @BeanFactory(profile = "runtime-root")
  public static class RuntimeFactory {
    @Bean
    public RuntimeContract product() {
      return new RuntimeProduct();
    }
  }

  public interface AliasContract {
  }

  public interface AliasView {
  }

  public static class AliasedProduct implements AliasContract, AliasView {
    @Autowired public AliasContract self;
    @Autowired public Leaf leaf;
  }

  @BeanFactory(profile = "aliases")
  public static class AliasFactory {
    private final AliasedProduct shared = new AliasedProduct();

    @Bean
    public AliasContract contract() {
      return shared;
    }

    @Bean
    public AliasView view() {
      return shared;
    }
  }

  public interface EqualContract {
  }

  public static class EqualProduct implements EqualContract {
    @Autowired public Leaf leaf;

    @Override
    public boolean equals(Object other) {
      return other instanceof EqualProduct;
    }

    @Override
    public int hashCode() {
      return 17;
    }
  }

  @BeanFactory(profile = "equal-products")
  public static class EqualFactory {
    @Bean("left")
    public EqualContract left() {
      return new EqualProduct();
    }

    @Bean("right")
    public EqualContract right() {
      return new EqualProduct();
    }
  }

  public static class NullCompanion {
    @Autowired public Leaf leaf;
  }

  @BeanFactory(profile = "null-products")
  public static class NullFactory {
    @Bean
    public Missing absent() {
      return null;
    }

    @Bean
    public NullCompanion companion() {
      return new NullCompanion();
    }
  }

  @Component(profile = "broken-cycle")
  public static class BrokenA {
    @Autowired public BrokenB peer;
  }

  @Component(profile = "broken-cycle")
  public static class BrokenB {
    @Autowired public BrokenA peer;
    @Autowired public Missing missing;
  }

  public interface OptionalProductContract {
  }

  public static class BrokenOptionalProduct implements OptionalProductContract {
    @Autowired public Missing missing;
  }

  @BeanFactory(profile = "optional-broken-child")
  public static class BrokenOptionalFactory {
    @Bean
    public OptionalProductContract product() {
      return new BrokenOptionalProduct();
    }
  }

  @Component(profile = "optional-broken-child")
  public static class OptionalConsumer {
    @AutowiredAcceptNull public OptionalProductContract product;
  }

  private CircularDependenciesFixtures() {
  }
}
