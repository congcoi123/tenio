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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.tenio.core.bootstrap.bean.TestBeanClass;
import com.tenio.core.bootstrap.command.TestClientCommandHandler;
import com.tenio.core.bootstrap.command.TestSystemCommandHandler;
import com.tenio.core.bootstrap.eventhandler.TestEventHandlerClass;
import com.tenio.core.bootstrap.exception.two.BootstrapComponentTwo;
import com.tenio.core.bootstrap.injector.BeanClass;
import com.tenio.core.bootstrap.injector.Injector;
import com.tenio.core.bootstrap.setting.TestSettingClass;
import com.tenio.core.bootstrap.test.BootstrapComponent;
import com.tenio.core.bootstrap.test.factory.TestBeanCommon;
import com.tenio.core.bootstrap.test.impl.TestClassA;
import com.tenio.core.bootstrap.test.impl.TestClassAlone;
import com.tenio.core.bootstrap.test.impl.TestClassCCopy;
import com.tenio.core.bootstrap.test.impl.TestClassD;
import com.tenio.core.bootstrap.test.impl.TestClassDCopy;
import com.tenio.core.bootstrap.test.inf.TestInterfaceA;
import com.tenio.core.bootstrap.test.inf.TestInterfaceC;
import com.tenio.core.bootstrap.test.inf.TestInterfaceD;
import com.tenio.core.command.system.implement.HelpCommand;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.exception.DuplicatedBeanCreationException;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.exception.NoImplementedClassFoundException;
import com.tenio.core.fixture.injector.failure.FailingFactory;
import com.tenio.core.fixture.injector.inherited.InheritedBase;
import com.tenio.core.fixture.injector.inherited.InheritedComponent;
import com.tenio.core.fixture.injector.inherited.InheritedConsumer;
import com.tenio.core.fixture.injector.inherited.InheritedContract;
import com.tenio.core.fixture.injector.qualifiedfactory.QualifiedFactoryFixtures;
import jakarta.servlet.http.HttpServlet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For Injector")
class InjectorTest {

  private final Injector injector = new Injector();

  @BeforeEach
  @AfterEach
  void resetInjectorState() throws Exception {
    injector.setProfiles(Set.of());
    injector.scanPackages(null);
  }

  @Test
  @DisplayName("Registries exist before scanning")
  void testGettersNotNull() {
    Injector injector = new Injector();
    assertNotNull(injector.getClassesMap());
    assertNotNull(injector.getClassBeansMap());
    assertNotNull(injector.getServletBeansMap());
    assertNotNull(injector.getSystemCommandManager());
    assertNotNull(injector.getClientCommandManager());
  }

  @Test
  @DisplayName("getBean returns null for unknown class")
  void testGetBeanReturnsNullForUnknownClass() {
    Injector injector = new Injector();
    assertNull(injector.getBean(String.class));
  }

  @Test
  @DisplayName("getBean returns correct instance after scanPackages")
  void testGetBeanAfterScanPackages() throws Exception {
    Injector injector = new Injector();
    injector.scanPackages(BootstrapComponent.class, "com.tenio.core.bootstrap.bean");
    Object bean = injector.getBean(BootstrapComponent.class);
    assertNotNull(bean);
  }

  @Test
  @DisplayName("getServletBeansMap returns map that can be modified")
  void testServletBeansMapModifiable() {
    Injector injector = new Injector();
    Map<String, HttpServlet> map = injector.getServletBeansMap();
    HttpServlet servlet = mock(HttpServlet.class);
    map.put("test", servlet);
    assertSame(servlet, injector.getServletBeansMap().get("test"));
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
    assertThrows(MultipleImplementedClassForInterfaceException.class,
        () -> injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl", "com.tenio.core" +
            ".bootstrap.test.inf", "com.tenio.core.bootstrap.exception.one"));
  }

  @Test
  @DisplayName("After scanning the package should throw an exception because there is no class " +
      "implement declared interface")
  void scanPackageShouldThrowExceptionInInstanceOfE() {
    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl", "com.tenio.core" +
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

  @Test
  @DisplayName("Attempt fetching null bean should return null")
  void getNullBeanShouldReturnNull() {
    assertNull(new Injector().getBean(null));
  }

  @Test
  @DisplayName("scanPackages with @SystemCommand class registers it in system command manager")
  void scanPackageWithSystemCommandRegistersCommand() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.command");
    assertNotNull(injector.getSystemCommandManager().getHandler("test-sys"));
  }

  @Test
  @DisplayName("Built-in system commands are created only with the monitor profile")
  void builtInSystemCommandsRequireMonitorProfile() throws Exception {
    injector.scanPackages(HelpCommand.class);

    assertTrue(injector.getSystemCommandManager().getHandlers().isEmpty());
    assertFalse(injector.getClassBeansMap().containsKey(new BeanClass(HelpCommand.class, "help")));

    injector.setProfiles(Set.of(CoreConstant.MONITOR_PROFILE));
    injector.scanPackages(HelpCommand.class);

    var manager = injector.getSystemCommandManager();
    assertSame(manager, manager.getHandler("help").getCommandManager());
    assertEquals(Set.of("help", "info", "server", "unban"), manager.getHandlers().keySet());
    assertSame(manager.getHandler("help"),
        injector.getClassBeansMap().get(new BeanClass(HelpCommand.class, "help")));

    injector.setProfiles(Set.of("world"));
    injector.scanPackages(HelpCommand.class);

    assertTrue(injector.getSystemCommandManager().getHandlers().isEmpty());
    assertTrue(manager.getHandlers().isEmpty());
  }

  @Test
  @DisplayName("scanPackages with @ClientCommand class registers it in client command manager")
  void scanPackageWithClientCommandRegistersCommand() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.command");
    assertNotNull(injector.getClientCommandManager().getHandler((short) 42));
  }

  @Test
  @DisplayName("scanPackages with @SystemCommand class stores it in class beans map")
  void scanPackageWithSystemCommandAddsToBeansMap() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.command");
    boolean found = injector.getClassBeansMap().entrySet().stream()
        .anyMatch(e -> e.getValue() instanceof TestSystemCommandHandler);
    assertTrue(found);
  }

  @Test
  @DisplayName("scanPackages with @ClientCommand class stores it in class beans map")
  void scanPackageWithClientCommandAddsToBeansMap() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.command");
    boolean found = injector.getClassBeansMap().entrySet().stream()
        .anyMatch(e -> e.getValue() instanceof TestClientCommandHandler);
    assertTrue(found);
  }

  @Test
  @DisplayName("scanPackages with @RestController class registers its @RestMapping methods in servletBeansMap")
  void scanPackageWithRestControllerRegistersServlets() throws Exception {
    injector.scanPackages(null, "com.tenio.core.network.jetty.controller");
    assertFalse(injector.getServletBeansMap().isEmpty());
  }

  @Test
  @DisplayName("scanPackages with @RestController adds the controller to classBeansMap")
  void scanPackageWithRestControllerAddsToClassBeansMap() throws Exception {
    injector.scanPackages(null, "com.tenio.core.network.jetty.controller");
    boolean found = injector.getClassBeansMap().entrySet().stream()
        .anyMatch(e -> e.getValue().getClass().getSimpleName().equals("PingController"));
    assertTrue(found);
  }

  @Test
  @DisplayName("scanPackages with @EventHandler class creates bean and stores it in classBeansMap")
  void scanPackageWithEventHandlerClassCreatesBean() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.eventhandler");
    boolean found = injector.getClassBeansMap().entrySet().stream()
        .anyMatch(e -> e.getValue() instanceof TestEventHandlerClass);
    assertTrue(found);
  }

  @Test
  @DisplayName("scanPackages with @Setting class creates bean and stores it in classBeansMap")
  void scanPackageWithSettingClassCreatesBean() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.setting");
    boolean found = injector.getClassBeansMap().entrySet().stream()
        .anyMatch(e -> e.getValue() instanceof TestSettingClass);
    assertTrue(found);
  }

  @Test
  @DisplayName("getBean retrieves @EventHandler annotated class by its own class")
  void getBeanReturnsEventHandlerClass() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.eventhandler");
    Object bean = injector.getBean(TestEventHandlerClass.class);
    assertNotNull(bean);
    assertInstanceOf(TestEventHandlerClass.class, bean);
  }

  @Test
  @DisplayName("getBean retrieves @Setting annotated class by its own class")
  void getBeanReturnsSettingClass() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.setting");
    Object bean = injector.getBean(TestSettingClass.class);
    assertNotNull(bean);
    assertInstanceOf(TestSettingClass.class, bean);
  }
  @Test
  @DisplayName("Initialization lookups should reuse the sole unnamed implementation")
  void getBeanShouldReuseUnnamedImplementation() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");

    var implementation = injector.getBean(TestClassA.class);
    assertNotNull(implementation);
    assertSame(implementation, injector.getBean(TestInterfaceA.class));
    assertSame(implementation, injector.getBean(TestInterfaceA.class));
  }

  @Test
  @DisplayName("Initialization lookups should reflect replaced and removed bean registrations")
  void getBeanShouldReflectReplacedAndRemovedRegistrations() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");
    var key = new BeanClass(TestClassA.class, "");
    var original = injector.getBean(TestInterfaceA.class);
    assertNotNull(original);

    var replacement = new TestClassA();
    injector.getClassBeansMap().put(key, replacement);

    assertSame(replacement, injector.getBean(TestInterfaceA.class));
    assertSame(replacement, injector.getBean(TestClassA.class));
    assertNotSame(original, replacement);

    injector.getClassBeansMap().remove(key);
    assertNull(injector.getBean(TestInterfaceA.class));
    assertNull(injector.getBean(TestClassA.class));
  }

  @Test
  @DisplayName("Initialization lookups should detect ambiguity introduced after an earlier lookup")
  void getBeanShouldDetectAmbiguityAfterRegistrationsChange() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");
    var extraKey = new BeanClass(TestClassDCopy.class, "");
    var extra = injector.getClassBeansMap().remove(extraKey);
    var original = injector.getClassBeansMap().get(new BeanClass(TestClassD.class, ""));
    assertNotNull(extra);
    assertNotNull(original);
    assertSame(original, injector.getBean(TestInterfaceD.class));

    injector.getClassBeansMap().put(extraKey, extra);

    var exception = assertThrows(IllegalStateException.class,
        () -> injector.getBean(TestInterfaceD.class));
    assertTrue(exception.getMessage().contains(TestInterfaceD.class.getName()));
  }

  @Test
  @DisplayName("Initialization lookups should ignore named beans when an unnamed bean exists")
  void getBeanShouldExcludeNamedBeans() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.factory");

    var bean = injector.getBean(TestBeanCommon.class);
    assertNotNull(bean);
    assertEquals("common", bean.text());
  }

  @Test
  @DisplayName("Ambiguous unnamed initialization lookups should fail explicitly")
  void getBeanShouldRejectAmbiguousUnnamedBeans() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");

    var exception = assertThrows(IllegalStateException.class,
        () -> injector.getBean(TestInterfaceD.class));
    assertTrue(exception.getMessage().contains(TestInterfaceD.class.getName()));
  }

  @Test
  @DisplayName("A lookup before scanning should not hide a subsequently created bean")
  void getBeanShouldFindBeanAfterScanning() throws Exception {
    assertNull(injector.getBean(TestInterfaceA.class));

    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");

    assertNotNull(injector.getBean(TestInterfaceA.class));
  }

  @Test
  @DisplayName("Rescanning should replace bean registrations from the previous generation")
  void scanPackagesShouldReplacePreviousGeneration() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");
    var previous = injector.getBean(TestInterfaceA.class);
    assertNotNull(previous);

    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");

    var current = injector.getBean(TestInterfaceA.class);
    assertNotNull(current);
    assertNotSame(previous, current);
    assertSame(current, injector.getBean(TestClassA.class));

    injector.scanPackages(null);
    assertNull(injector.getBean(TestInterfaceA.class));
    assertNull(injector.getBean(TestClassA.class));
  }

  @Test
  @DisplayName("Discovery should resolve inherited contracts and wire fields on abstract parents")
  void scanPackagesShouldResolveInheritedContractsAndFields() throws Exception {
    injector.scanPackages(InheritedComponent.class, "com.tenio.core.bootstrap.test.impl");

    var component = injector.getBean(InheritedComponent.class);
    assertNotNull(component);
    assertAll(
        () -> assertSame(component, injector.getBean(InheritedBase.class)),
        () -> assertSame(component, injector.getBean(InheritedContract.class)),
        () -> assertSame(component, injector.getBean(InheritedContract.Child.class)),
        () -> assertSame(injector.getBean(TestClassA.class), component.getDependency()),
        () -> assertSame(component, injector.getBean(InheritedConsumer.class).getContract()),
        () -> assertTrue(injector.getClassesMap().get(InheritedComponent.class)
            .containsAll(Set.of(InheritedBase.class, InheritedContract.Child.class,
                InheritedContract.class))));
  }

  @Test
  @DisplayName("Failed autowiring should discard old beans and all partial registrations")
  void failedScanShouldDiscardPartialGeneration() throws Exception {
    injector.scanPackages(null, "com.tenio.core.bootstrap.test.impl");
    assertNotNull(injector.getBean(TestInterfaceA.class));

    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(BootstrapComponentTwo.class,
            "com.tenio.core.bootstrap.command", "com.tenio.core.network.jetty.controller"));

    assertAll(
        () -> assertTrue(injector.getClassesMap().isEmpty()),
        () -> assertTrue(injector.getClassBeansMap().isEmpty()),
        () -> assertTrue(injector.getServletBeansMap().isEmpty()),
        () -> assertTrue(injector.getSystemCommandManager().getHandlers().isEmpty()),
        () -> assertNull(injector.getClientCommandManager().getHandler((short) 42)),
        () -> assertNull(injector.getBean(TestInterfaceA.class)),
        () -> assertNull(injector.getBean(BootstrapComponentTwo.class)));
  }

  @Test
  @DisplayName("Failed factories should retain the original exception and discard partial beans")
  void failedFactoryShouldPreserveCauseAndDiscardPartialGeneration() {
    var exception = assertThrows(InvocationTargetException.class,
        () -> injector.scanPackages(FailingFactory.class, "com.tenio.core.bootstrap.command"));

    assertAll(
        () -> assertInstanceOf(IllegalStateException.class, exception.getCause()),
        () -> assertEquals("Expected factory failure", exception.getCause().getMessage()),
        () -> assertTrue(injector.getClassesMap().isEmpty()),
        () -> assertTrue(injector.getClassBeansMap().isEmpty()),
        () -> assertTrue(injector.getServletBeansMap().isEmpty()),
        () -> assertTrue(injector.getSystemCommandManager().getHandlers().isEmpty()),
        () -> assertNull(injector.getClientCommandManager().getHandler((short) 42)));
  }
  @Test
  @DisplayName("Class qualifiers should resolve interface-returning factories by runtime type and name")
  void classQualifierShouldResolveFactoryRuntimeImplementation() throws Exception {
    injector.setProfiles(Set.of("matches"));
    injector.scanPackages(QualifiedFactoryFixtures.class);

    var consumer = injector.getBean(QualifiedFactoryFixtures.MatchingConsumer.class);
    var defaultService = injector.getClassBeansMap()
        .get(new BeanClass(QualifiedFactoryFixtures.Service.class, ""));
    var namedService = injector.getClassBeansMap()
        .get(new BeanClass(QualifiedFactoryFixtures.Service.class, "chosen"));
    assertAll(
        () -> assertNotNull(consumer),
        () -> assertSame(defaultService, consumer.defaultService),
        () -> assertSame(namedService, consumer.namedService),
        () -> assertNotSame(consumer.defaultService, consumer.namedService),
        () -> assertNull(consumer.wrongClass),
        () -> assertNull(consumer.absentService),
        () -> assertNull(consumer.incompatibleContract));
  }

  @Test
  @DisplayName("A class-qualified factory lookup must not use another name's matching implementation")
  void classQualifierShouldRejectWrongFactoryRuntimeImplementation() {
    injector.setProfiles(Set.of("wrong-class"));

    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(QualifiedFactoryFixtures.class));
  }

  @Test
  @DisplayName("Both the requested name and runtime implementation must match a factory bean")
  void classQualifierShouldNotIgnoreRequestedFactoryName() {
    injector.setProfiles(Set.of("wrong-name"));

    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(QualifiedFactoryFixtures.class));
  }

  @Test
  @DisplayName("A missing factory name cannot fall back to the default qualified implementation")
  void classQualifierShouldRejectMissingFactoryName() {
    injector.setProfiles(Set.of("missing-name"));

    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(QualifiedFactoryFixtures.class));
  }

  @Test
  @DisplayName("A null factory value cannot satisfy a required runtime class qualifier")
  void classQualifierShouldRejectRequiredNullFactoryValue() {
    injector.setProfiles(Set.of("required-null"));

    assertThrows(NoImplementedClassFoundException.class,
        () -> injector.scanPackages(QualifiedFactoryFixtures.class));
  }

  @Test
  @DisplayName("Equal but distinct class-qualified factory instances remain ambiguous")
  void classQualifierShouldRejectAmbiguousFactoryInstances() {
    injector.setProfiles(Set.of("ambiguous"));

    assertThrows(MultipleImplementedClassForInterfaceException.class,
        () -> injector.scanPackages(QualifiedFactoryFixtures.class));
  }

  @Test
  @DisplayName("Factory aliases of the same instance should not create class qualifier ambiguity")
  void classQualifierShouldAcceptAliasesOfSameFactoryInstance() throws Exception {
    injector.setProfiles(Set.of("same-instance"));
    injector.scanPackages(QualifiedFactoryFixtures.class);

    var consumer = injector.getBean(QualifiedFactoryFixtures.AliasedConsumer.class);
    var service = injector.getClassBeansMap()
        .get(new BeanClass(QualifiedFactoryFixtures.Service.class, ""));
    var alias = injector.getClassBeansMap()
        .get(new BeanClass(QualifiedFactoryFixtures.AliasService.class, ""));
    assertNotNull(consumer);
    assertSame(service, alias);
    assertSame(service, consumer.service);
  }
}
