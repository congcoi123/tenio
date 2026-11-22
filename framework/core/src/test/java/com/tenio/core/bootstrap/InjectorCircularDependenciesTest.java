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

package com.tenio.core.bootstrap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.injector.BeanClass;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.exception.NoImplementedClassFoundException;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.AliasContract;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.AliasView;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.AliasedProduct;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.BrokenA;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.EqualContract;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.EqualProduct;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.LazyNamedNode;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.Leaf;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.Missing;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.MutualA;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.MutualB;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.NamedBlue;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.NamedConsumer;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.NamedContract;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.NamedRed;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.NullCompanion;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.OptionalA;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.OptionalB;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.RuntimeContract;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.RuntimeProduct;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.SelfNode;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.ThreeA;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.ThreeB;
import com.tenio.core.fixture.injector.circular.CircularDependenciesFixtures.ThreeC;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Regression tests for complete, finite wiring of cyclic singleton field dependencies. */
class InjectorCircularDependenciesTest {

  private final Injector injector = new Injector();

  @BeforeEach
  @AfterEach
  void resetInjector() throws Exception {
    injector.setProfiles(Set.of());
    injector.scanPackages(null);
  }

  @Test
  void selfReferenceUsesTheRegisteredInstanceAndStillWiresOtherFields() throws Exception {
    scan("self");

    var node = injector.getBean(SelfNode.class);
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(node);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(node, node.self),
        () -> assertSame(leaf, node.leaf),
        () -> assertNull(node.excluded));
  }

  @Test
  void mutualReferencesReuseBothRegisteredInstances() throws Exception {
    scan("mutual");

    var first = injector.getBean(MutualA.class);
    var second = injector.getBean(MutualB.class);
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(first);
    assertNotNull(second);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(second, first.peer),
        () -> assertSame(first, second.peer),
        () -> assertSame(leaf, first.leaf),
        () -> assertSame(leaf, second.leaf));
  }

  @Test
  void threeBeanCycleWiresEveryMemberAndItsSharedLeaf() throws Exception {
    scan("three");

    var first = injector.getBean(ThreeA.class);
    var second = injector.getBean(ThreeB.class);
    var third = injector.getBean(ThreeC.class);
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(first);
    assertNotNull(second);
    assertNotNull(third);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(second, first.next),
        () -> assertSame(third, second.next),
        () -> assertSame(first, third.next),
        () -> assertSame(leaf, first.leaf),
        () -> assertSame(leaf, second.leaf),
        () -> assertSame(leaf, third.leaf));
  }

  @Test
  void optionalDependencyCanCompleteACycleAndMissingOptionalBeanRemainsNull() throws Exception {
    scan("optional");

    var first = injector.getBean(OptionalA.class);
    var second = injector.getBean(OptionalB.class);
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(first);
    assertNotNull(second);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(second, first.peer),
        () -> assertSame(first, second.peer),
        () -> assertSame(leaf, first.leaf),
        () -> assertSame(leaf, second.leaf),
        () -> assertNull(first.missing));
  }

  @Test
  void namedComponentCreatedDuringWiringIsAlsoCompletelyWired() throws Exception {
    scan("lazy-named");

    var original = injector.getBean(LazyNamedNode.class);
    var secondary = assertInstanceOf(LazyNamedNode.class, injector.getClassBeansMap()
        .get(new BeanClass(LazyNamedNode.class, "secondary")));
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(original);
    assertNotNull(leaf);
    assertNotSame(original, secondary);
    assertAll(
        () -> assertSame(secondary, original.secondary),
        () -> assertSame(secondary, secondary.secondary),
        () -> assertSame(leaf, original.leaf),
        () -> assertSame(leaf, secondary.leaf));
  }

  @Test
  void namedInterfaceFactoryProductsFormACycleAndReceiveInheritedDependencies() throws Exception {
    scan("named-factory");

    var consumer = injector.getBean(NamedConsumer.class);
    var blue = assertInstanceOf(NamedBlue.class, injector.getClassBeansMap()
        .get(new BeanClass(NamedContract.class, "blue")));
    var red = assertInstanceOf(NamedRed.class, injector.getClassBeansMap()
        .get(new BeanClass(NamedContract.class, "red")));
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(consumer);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(blue, consumer.blue),
        () -> assertSame(red, consumer.red),
        () -> assertSame(red, blue.peer),
        () -> assertSame(blue, red.peer),
        () -> assertSame(leaf, blue.inheritedLeaf()),
        () -> assertSame(leaf, red.inheritedLeaf()),
        () -> assertNull(injector.getBean(NamedContract.class)));
  }

  @Test
  void unreferencedInterfaceFactoryProductReceivesItsRuntimeAndInheritedFields() throws Exception {
    scan("runtime-root");

    var product = assertInstanceOf(RuntimeProduct.class, injector.getBean(RuntimeContract.class));
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(product, product.self),
        () -> assertSame(leaf, product.ownLeaf),
        () -> assertSame(leaf, product.inheritedLeaf()));
  }

  @Test
  void factoryAliasesRetainOneCompletelyWiredObject() throws Exception {
    scan("aliases");

    var product = assertInstanceOf(AliasedProduct.class, injector.getBean(AliasContract.class));
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(leaf);
    assertAll(
        () -> assertSame(product, injector.getBean(AliasView.class)),
        () -> assertSame(product, product.self),
        () -> assertSame(leaf, product.leaf));
  }

  @Test
  void distinctFactoryObjectsThatCompareEqualAreBothWired() throws Exception {
    scan("equal-products");

    var left = assertInstanceOf(EqualProduct.class, injector.getClassBeansMap()
        .get(new BeanClass(EqualContract.class, "left")));
    var right = assertInstanceOf(EqualProduct.class, injector.getClassBeansMap()
        .get(new BeanClass(EqualContract.class, "right")));
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(leaf);
    assertNotSame(left, right);
    assertEquals(left, right);
    assertAll(
        () -> assertSame(leaf, left.leaf),
        () -> assertSame(leaf, right.leaf));
  }

  @Test
  void nullFactoryProductsDoNotPreventOtherBeansFromBeingWired() throws Exception {
    scan("null-products");

    var companion = injector.getBean(NullCompanion.class);
    var leaf = injector.getBean(Leaf.class);
    assertNotNull(companion);
    assertNotNull(leaf);
    assertAll(
        () -> assertNull(injector.getBean(Missing.class)),
        () -> assertSame(leaf, companion.leaf));
  }

  @Test
  void rescanningBuildsAFreshCompletelyWiredCycle() throws Exception {
    scan("mutual");
    var previousFirst = injector.getBean(MutualA.class);
    var previousSecond = injector.getBean(MutualB.class);

    scan("mutual");

    var first = injector.getBean(MutualA.class);
    var second = injector.getBean(MutualB.class);
    assertNotNull(first);
    assertNotNull(second);
    assertNotSame(previousFirst, first);
    assertNotSame(previousSecond, second);
    assertAll(
        () -> assertSame(second, first.peer),
        () -> assertSame(first, second.peer),
        () -> assertSame(injector.getBean(Leaf.class), first.leaf),
        () -> assertSame(injector.getBean(Leaf.class), second.leaf));

    scan("self");
    assertNull(injector.getBean(MutualA.class));
    assertNull(injector.getBean(MutualB.class));
    assertSame(injector.getBean(SelfNode.class), injector.getBean(SelfNode.class).self);
  }

  @Test
  void requiredFailureInACycleClearsPreviousAndPartialBeansAndAllowsRetry() throws Exception {
    scan("self");
    var previous = injector.getBean(SelfNode.class);
    assertNotNull(previous);

    var failure = assertThrows(NoImplementedClassFoundException.class, () -> scan("broken-cycle"));
    assertTrue(failure.getMessage().contains(Missing.class.getName()));
    assertAll(
        () -> assertTrue(injector.getClassBeansMap().isEmpty()),
        () -> assertTrue(injector.getClassesMap().isEmpty()),
        () -> assertTrue(injector.getServletBeansMap().isEmpty()),
        () -> assertNull(injector.getBean(SelfNode.class)),
        () -> assertNull(injector.getBean(BrokenA.class)));

    scan("self");
    var current = injector.getBean(SelfNode.class);
    assertNotNull(current);
    assertNotSame(previous, current);
    assertSame(current, current.self);
    assertSame(injector.getBean(Leaf.class), current.leaf);
  }

  @Test
  void presentOptionalFactoryProductCannotHideItsMissingRequiredDependency() {
    var failure = assertThrows(NoImplementedClassFoundException.class,
        () -> scan("optional-broken-child"));

    assertTrue(failure.getMessage().contains(Missing.class.getName()));
    assertAll(
        () -> assertTrue(injector.getClassBeansMap().isEmpty()),
        () -> assertTrue(injector.getClassesMap().isEmpty()),
        () -> assertTrue(injector.getServletBeansMap().isEmpty()));
  }

  private void scan(String profile) throws Exception {
    injector.setProfiles(Set.of(profile));
    injector.scanPackages(CircularDependenciesFixtures.class);
  }
}
