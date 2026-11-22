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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.injector.BeanClass;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.bootstrap.profiles.commands.CommandFixtures;
import com.tenio.core.bootstrap.profiles.components.ComponentFixtures;
import com.tenio.core.bootstrap.profiles.factories.FactoryFixtures;
import com.tenio.core.bootstrap.profiles.fields.FieldFixtures;
import com.tenio.core.bootstrap.profiles.rest.RestFixtures;
import com.tenio.core.exception.NoImplementedClassFoundException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Profile selection across injector discovery, creation and registration")
class InjectorProfilesTest {

  private final Injector injector = new Injector();

  @BeforeEach
  void resetSideEffects() throws Exception {
    resetInjector();
    ComponentFixtures.worldConstructions = 0;
    ComponentFixtures.gameConstructions = 0;
    ComponentFixtures.settingConstructions = 0;
    ComponentFixtures.eventConstructions = 0;
    FactoryFixtures.factoryConstructions = 0;
    FactoryFixtures.worldInvocations = 0;
    FactoryFixtures.gameInvocations = 0;
    FactoryFixtures.restrictedReturnInvocations = 0;
    RestFixtures.worldConstructions = 0;
    RestFixtures.gameConstructions = 0;
    RestFixtures.sharedInvocations = 0;
    RestFixtures.combinedInvocations = 0;
    CommandFixtures.worldConstructions = 0;
    CommandFixtures.gameConstructions = 0;
  }

  @AfterEach
  void resetInjector() throws Exception {
    injector.setProfiles(Set.of());
    injector.scanPackages(null);
  }

  @Test
  void classSelectionPreventsInactiveConstructionAndKeepsSharedComponents() throws Exception {
    scan(ComponentFixtures.class, "world");

    assertInstanceOf(ComponentFixtures.WorldService.class, injector.getBean(ComponentFixtures.Service.class));
    assertNotNull(injector.getBean(ComponentFixtures.SharedComponent.class));
    assertNotNull(injector.getBean(ComponentFixtures.WorldSetting.class));
    assertNull(injector.getBean(ComponentFixtures.GameService.class));
    assertNull(injector.getBean(ComponentFixtures.GameEventHandler.class));
    assertEquals(1, ComponentFixtures.worldConstructions);
    assertEquals(1, ComponentFixtures.settingConstructions);
    assertEquals(0, ComponentFixtures.gameConstructions);
    assertEquals(0, ComponentFixtures.eventConstructions);
  }

  @Test
  void gameProfileSelectsEventHandlersAndExcludesWorldSettings() throws Exception {
    scan(ComponentFixtures.class, "game");

    assertInstanceOf(ComponentFixtures.GameService.class, injector.getBean(ComponentFixtures.Service.class));
    assertNotNull(injector.getBean(ComponentFixtures.GameEventHandler.class));
    assertNull(injector.getBean(ComponentFixtures.WorldSetting.class));
    assertEquals(0, ComponentFixtures.worldConstructions);
    assertEquals(0, ComponentFixtures.settingConstructions);
    assertEquals(1, ComponentFixtures.gameConstructions);
    assertEquals(1, ComponentFixtures.eventConstructions);
  }

  @Test
  void allAnnotationsOnAClassMustMatchBeforeDiscovery() throws Exception {
    scan(ComponentFixtures.class, "world");
    assertNull(injector.getBean(ComponentFixtures.CombinedEventHandler.class));

    scan(ComponentFixtures.class, "game");
    assertNull(injector.getBean(ComponentFixtures.CombinedEventHandler.class));

    scan(ComponentFixtures.class, "world", "game");
    assertNotNull(injector.getBean(ComponentFixtures.CombinedEventHandler.class));
  }

  @Test
  void overlappingProvidersRejectAnAmbiguousUnnamedLookup() throws Exception {
    scan(ComponentFixtures.class, "world", "game");

    assertThrows(IllegalStateException.class,
        () -> injector.getBean(ComponentFixtures.Service.class));
  }

  @Test
  void noActiveProfilesStillIncludesUnrestrictedDefinitions() throws Exception {
    scan(ComponentFixtures.class);

    assertNotNull(injector.getBean(ComponentFixtures.SharedComponent.class));
    assertNull(injector.getBean(ComponentFixtures.Service.class));
    assertEquals(0, ComponentFixtures.worldConstructions);
    assertEquals(0, ComponentFixtures.gameConstructions);
  }

  @Test
  void factoryMethodAndReturnTypeSelectionPrecedeInvocation() throws Exception {
    scan(FactoryFixtures.class, "world");

    assertEquals("world", injector.getBean(FactoryFixtures.Product.class).origin());
    assertNull(injector.getBean(FactoryFixtures.GameProduct.class));
    assertFalse(injector.getClassesMap().containsKey(FactoryFixtures.GameProduct.class));
    assertEquals(1, FactoryFixtures.factoryConstructions);
    assertEquals(1, FactoryFixtures.worldInvocations);
    assertEquals(0, FactoryFixtures.gameInvocations);
    assertEquals(0, FactoryFixtures.restrictedReturnInvocations);
  }

  @Test
  void inactiveFactoryIsNotConstructedEvenWhenItsMethodsMatch() throws Exception {
    scan(FactoryFixtures.class, "game");

    assertNull(injector.getBean(FactoryFixtures.Product.class));
    assertNull(injector.getBean(FactoryFixtures.GameProduct.class));
    assertEquals(0, FactoryFixtures.factoryConstructions);
    assertEquals(0, FactoryFixtures.gameInvocations);
    assertEquals(0, FactoryFixtures.restrictedReturnInvocations);
  }

  @Test
  void selectedReturnTypeAndNamedFactoryBeansAreCreated() throws Exception {
    scan(FactoryFixtures.class, "world", "game");

    assertEquals("world", injector.getBean(FactoryFixtures.Product.class).origin());
    var namedBean = (FactoryFixtures.Product) injector.getClassBeansMap().get(new BeanClass(FactoryFixtures.Product.class, "game"));
    assertEquals("game", namedBean.origin());
    assertInstanceOf(FactoryFixtures.GameProductImpl.class, injector.getBean(FactoryFixtures.GameProduct.class));
    assertEquals(1, FactoryFixtures.factoryConstructions);
    assertEquals(1, FactoryFixtures.worldInvocations);
    assertEquals(1, FactoryFixtures.gameInvocations);
    assertEquals(1, FactoryFixtures.restrictedReturnInvocations);
  }

  @Test
  void inactiveRequiredFieldsAreSkippedAndMissingOptionalFieldsRemainNull() throws Exception {
    scan(FieldFixtures.class, "world");
    var consumer = injector.getBean(FieldFixtures.Consumer.class);

    assertInstanceOf(FieldFixtures.WorldService.class, consumer.world);
    assertNull(consumer.game);
    assertNull(consumer.inactiveRequired);
    assertNull(consumer.optionalGame);
    assertNull(consumer.optionalQualifiedGame);
    assertNull(consumer.optionalNamedGame);
    assertNull(consumer.optionalNullProduct);
    assertNull(consumer.asynchronousGame);
    assertNull(consumer.optionalMissing);
    assertNull(consumer.namedProduct);
    assertNull(consumer.bothProfiles);
    assertEquals("default", consumer.defaultProduct.name());
  }

  @Test
  void qualifierNamesDoNotActAsActiveProfileNames() throws Exception {
    scan(FieldFixtures.class, "game");
    var consumer = injector.getBean(FieldFixtures.Consumer.class);

    assertNull(consumer.world);
    assertInstanceOf(FieldFixtures.GameService.class, consumer.game);
    assertSame(consumer.game, consumer.optionalQualifiedGame);
    assertEquals("named-game", consumer.optionalNamedGame.name());
    assertSame(consumer.game, consumer.asynchronousGame);
    assertEquals("named-world", consumer.namedProduct.name());
    assertSame(injector.getClassBeansMap().get(new BeanClass(FieldFixtures.Product.class, "world")), consumer.namedProduct);
    assertNull(consumer.bothProfiles);
    assertNull(consumer.optionalGame);
  }

  @Test
  void fieldAnnotationRestrictionsCombineAndClassQualifiersDisambiguate() throws Exception {
    scan(FieldFixtures.class, "world", "game");
    var consumer = injector.getBean(FieldFixtures.Consumer.class);

    assertInstanceOf(FieldFixtures.WorldService.class, consumer.world);
    assertInstanceOf(FieldFixtures.GameService.class, consumer.game);
    assertSame(consumer.game, consumer.optionalGame);
    assertSame(consumer.game, consumer.optionalQualifiedGame);
    assertSame(consumer.game, consumer.asynchronousGame);
    assertSame(consumer.namedProduct, consumer.bothProfiles);
    assertSame(injector.getBean(FieldFixtures.Product.class), consumer.defaultProduct);
  }

  @Test
  void requiredClassQualifierCannotFallBackToAnotherActiveProfile() {
    assertThrows(NoImplementedClassFoundException.class,
        () -> scan(FieldFixtures.class, "world", "require-game"));
  }

  @Test
  void requiredNamedQualifierCannotSilentlySkipAnExcludedFactoryBean() {
    var exception = assertThrows(NoImplementedClassFoundException.class,
        () -> scan(FieldFixtures.class, "world", "require-named-game"));
    assertEquals("Unable to find any implementation for the class: "
        + FieldFixtures.Product.class.getName(), exception.getMessage());
  }

  @Test
  void nullFactoryResultCannotSatisfyARequiredField() {
    var exception = assertThrows(NoImplementedClassFoundException.class,
        () -> scan(FieldFixtures.class, "require-null-product"));
    assertEquals("Unable to find any implementation for the class: "
        + FieldFixtures.Product.class.getName(), exception.getMessage());
  }

  @Test
  void restClassAndMethodSelectionPrecedeMappingInvocation() throws Exception {
    scan(RestFixtures.class, "world");

    assertEquals(Set.of("api/status"), injector.getServletBeansMap().keySet());
    assertInstanceOf(RestFixtures.StatusServlet.class, injector.getServletBeansMap().get("api/status"));
    assertEquals(1, RestFixtures.worldConstructions);
    assertEquals(0, RestFixtures.gameConstructions);
    assertEquals(1, RestFixtures.sharedInvocations);
    assertEquals(0, RestFixtures.combinedInvocations);
  }

  @Test
  void restMethodRequiresBothItsOwnAndItsControllersProfiles() throws Exception {
    scan(RestFixtures.class, "game");
    assertEquals(Set.of("game/status"), injector.getServletBeansMap().keySet());
    assertEquals(0, RestFixtures.worldConstructions);
    assertEquals(0, RestFixtures.combinedInvocations);

    scan(RestFixtures.class, "world", "game");
    assertEquals(Set.of("api/status", "api/combined", "game/status"), injector.getServletBeansMap().keySet());
    assertEquals(1, RestFixtures.combinedInvocations);
  }

  @Test
  void profileChangeRemovesMappingsFromThePreviousScan() throws Exception {
    scan(RestFixtures.class, "world");
    assertTrue(injector.getServletBeansMap().containsKey("api/status"));

    scan(RestFixtures.class, "game");
    assertEquals(Set.of("game/status"), injector.getServletBeansMap().keySet());
  }

  @Test
  void disjointCommandProfilesReuseKeysAndInjectSelectedHandlers() throws Exception {
    scan(CommandFixtures.class, "world");
    var client = assertInstanceOf(CommandFixtures.WorldClient.class,
        injector.getClientCommandManager().getHandler((short) 42));
    var system = assertInstanceOf(CommandFixtures.WorldSystem.class,
        injector.getSystemCommandManager().getHandler("profile-status"));

    assertSame(injector.getBean(CommandFixtures.Dependency.class), client.dependency);
    assertSame(client.dependency, system.dependency);
    assertSame(injector.getClientCommandManager(), client.getCommandManager());
    assertSame(injector.getSystemCommandManager(), system.getCommandManager());
    assertEquals(2, CommandFixtures.worldConstructions);
    assertEquals(0, CommandFixtures.gameConstructions);
    assertEquals(1, injector.getClientCommandManager().getHandlers().size());
    assertEquals(1, injector.getSystemCommandManager().getHandlers().size());
  }

  @Test
  void profileChangesReplaceCommandHandlersAndClearInactiveRegistrations() throws Exception {
    scan(CommandFixtures.class, "world");
    scan(CommandFixtures.class, "game");

    var client = assertInstanceOf(CommandFixtures.GameClient.class,
        injector.getClientCommandManager().getHandler((short) 42));
    var system = assertInstanceOf(CommandFixtures.GameSystem.class,
        injector.getSystemCommandManager().getHandler("profile-status"));
    assertSame(injector.getBean(CommandFixtures.Dependency.class), client.dependency);
    assertSame(client.dependency, system.dependency);
    assertEquals(1, injector.getClientCommandManager().getHandlers().size());
    assertEquals(1, injector.getSystemCommandManager().getHandlers().size());

    var previousManager = injector.getSystemCommandManager();
    scan(CommandFixtures.class, "unrelated");
    assertTrue(injector.getClientCommandManager().getHandlers().isEmpty());
    assertTrue(injector.getSystemCommandManager().getHandlers().isEmpty());
    assertTrue(previousManager.getHandlers().isEmpty());
    assertTrue(previousManager.getAnnotations().isEmpty());
    assertEquals(2, CommandFixtures.worldConstructions);
  }

  private void scan(Class<?> fixtures, String... profiles) throws Exception {
    injector.setProfiles(Set.of(profiles));
    injector.scanPackages(null, fixtures.getPackageName());
  }
}
