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

import com.tenio.common.data.DataCollection;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.ClassLoaderUtility;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import com.tenio.core.bootstrap.annotation.Setting;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.command.system.SystemCommandManager;
import com.tenio.core.scheduler.ScheduledJobs;
import com.tenio.core.entity.Player;
import com.tenio.core.exception.DuplicatedBeanCreationException;
import com.tenio.core.exception.IllegalDefinedAccessControlException;
import com.tenio.core.exception.IllegalReturnTypeException;
import com.tenio.core.exception.InvalidRestMappingClassException;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.exception.NoImplementedClassFoundException;
import jakarta.servlet.http.HttpServlet;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Injector class provides a dependency injection mechanism for the application. It supports
 * automatic wiring of components and beans through annotations.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Automatic scanning and instantiation of components marked with {@link Component}
 *   <li>Support for bean factories and bean creation through {@link BeanFactory} and {@link Bean}
 *   <li>Dependency injection through {@link Autowired} and {@link AutowiredQualifier}
 *   <li>Initialization-time bean lookup
 *   <li>Component lifecycle management
 *   <li>Error handling and validation
 *   <li>Circular field dependencies between constructed beans
 * </ul>
 *
 * <p>Note: This class is designed to work in conjunction with the {@link Component}, {@link Bean},
 * {@link Autowired}, and {@link AutowiredQualifier} annotations for proper dependency injection.
 *
 * <p>Profile selection, scanning and bean lookups run sequentially during application
 * initialization, before application services start.
 *
 * @see Component
 * @see BeanFactory
 * @see Bean
 * @see Autowired
 * @see AutowiredQualifier
 * @see ClassLoaderUtility
 * @since 0.3.0
 */
public final class Injector extends SystemLogger {

  /**
   * A map contains values are interfaces and keys are implemented classes.
   */
  private final Map<Class<?>, List<Class<?>>> classesMap;

  /**
   * A map has keys are {@link #classesMap}'s key implemented classes and the value are keys'
   * instances.
   */
  private final Map<BeanClass, Object> classBeansMap;

  /**
   * A set of classes that are created by {@link Bean} and {@link BeanFactory} annotations.
   */
  private final Set<Class<?>> manualClassesSet;

  /** A map is using to initialize restful servlets. */
  private final Map<String, HttpServlet> servletBeansMap;

  /** The object manages all supported system commands. */
  private final SystemCommandManager systemCommandManager;

  /** The object manages all scheduled jobs. */
  private final ScheduledJobs scheduledJobs;

  /** The object manages all self-defined user commands. */
  private final ClientCommandManager clientCommandManager;

  /**
   * Active creation policy installed before scanning; unprofiled definitions always participate.
   */
  private ProfileSelection profiles;

  public Injector() {
    classesMap = new HashMap<>();
    manualClassesSet = new HashSet<>();
    classBeansMap = new HashMap<>();
    servletBeansMap = new HashMap<>();
    systemCommandManager = new SystemCommandManager();
    clientCommandManager = new ClientCommandManager();
    scheduledJobs = new ScheduledJobs();
    profiles = new ProfileSelection(Set.of());
  }

  /**
   * Scans all input packages to create classes' instances and initialize their dependencies.
   * Scanning runs sequentially during application initialization.
   *
   * @param entryClass the root class which should be located in the parent package of other class' packages
   * @param packages a list of packages' names. It allows to define the scanning packages by their names
   *
   * @throws InstantiationException if a class cannot be instantiated
   * @throws IllegalAccessException if a class or its constructor is not accessible
   * @throws ClassNotFoundException if a required class cannot be found
   * @throws IllegalArgumentException if invalid arguments are provided
   * @throws InvocationTargetException if a constructor throws an exception
   * @throws NoSuchMethodException if a required method cannot be found
   * @throws SecurityException if a security violation occurs
   * @throws DuplicatedBeanCreationException if a bean is created more than once
   */
  public void scanPackages(Class<?> entryClass, String... packages)
      throws InstantiationException,
          IllegalAccessException,
          ClassNotFoundException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          DuplicatedBeanCreationException {
    // A failed scan must not leave a partially wired generation available to subsequent lookups.
    cleanup();
    boolean completed = false;
    try {
      scanSelectedPackages(entryClass, packages);
      completed = true;
    } finally {
      if (!completed) {
        cleanup();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void scanSelectedPackages(Class<?> entryClass, String... packages)
      throws InstantiationException,
          IllegalAccessException,
          ClassNotFoundException,
          InvocationTargetException,
          NoSuchMethodException,
          DuplicatedBeanCreationException {
    var allPackageNames = new HashSet<String>();

    if (entryClass != null) {
      allPackageNames.add(entryClass.getPackage().getName());
    }

    if (packages != null) {
      allPackageNames.addAll(Arrays.asList(packages));
    }

    // fetches all classes that are in the same package as the root one
    var allClasses = ClassLoaderUtility.getClasses(allPackageNames).stream()
            .filter(profiles::matches)
            .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface())
            .sorted(Comparator.comparing(Class::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

    // Step 1: We collect classes
    // The implemented class is defined with the "Component" annotation declared inside it
    // in case you need more annotations with the same effect with this one, you should put them in here
    var listAnnotations = new Class[] {Component.class, EventHandler.class, Setting.class};
    var implementedComponentClasses = new HashSet<Class<?>>();
    Arrays.stream(listAnnotations)
        .forEach(annotation -> implementedComponentClasses.addAll(ClassLoaderUtility.getTypesAnnotatedWith(allClasses, annotation)));
    // scans all interfaces with their implemented classes
    for (var implementedClass : implementedComponentClasses) {
      classesMap.put(implementedClass, implementedTypes(implementedClass));
    }

    // Retrieves all classes those are declared by the @Bean annotation
    var implementedBeanClasses = new HashSet<Class<?>>();
    var beanFactoryClasses = ClassLoaderUtility.getTypesAnnotatedWith(allClasses, BeanFactory.class);
    for (var configurationClass : beanFactoryClasses) {
      for (var method : configurationClass.getMethods()) {
        if (method.isAnnotationPresent(Bean.class)
            && profiles.matches(method)
            && profiles.matches(method.getReturnType())
        ) {
          if (Modifier.isPublic(method.getModifiers())) {
            var clazz = method.getReturnType();
            if (clazz.isPrimitive()) {
              throw new IllegalReturnTypeException();
            } else if (clazz.equals(Void.TYPE)) {
              throw new IllegalReturnTypeException();
            } else {
              manualClassesSet.add(clazz);
              implementedBeanClasses.add(clazz);
            }
          } else {
            throw new IllegalDefinedAccessControlException();
          }
        }
      }
    }

    // Append all classes
    for (var implementedClass : implementedBeanClasses) {
      classesMap.put(implementedClass, implementedTypes(implementedClass));
    }

    // Add all classes annotated by @RestController
    var implementedRestClasses = ClassLoaderUtility.getTypesAnnotatedWith(allClasses, RestController.class);

    // Append all classes
    for (var implementedClass : implementedRestClasses) {
      classesMap.put(implementedClass, implementedTypes(implementedClass));
    }

    warnRegistrationCollisions(allClasses);

    // Step 2: We create instances
    // create beans (class instances) based on annotations
    for (var clazz : allClasses) {
      // in case you need to create a bean with another annotation, put it in here
      // but notices to put it in "implementedClasses" first
      // create beans automatically
      if (isClassAnnotated(clazz, listAnnotations)) {
        var beanClass = new BeanClass(clazz, "");
        if (classBeansMap.containsKey(beanClass)) {
          throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
        }
        var bean = clazz.getDeclaredConstructor().newInstance();
        classBeansMap.put(beanClass, bean);

        // create beans manually
      } else if (clazz.isAnnotationPresent(BeanFactory.class)) {
        // fetches all bean instances and save them to classes map
        var beanFactoryInstance = clazz.getDeclaredConstructor().newInstance();
        for (var method : clazz.getMethods()) {
          if (method.isAnnotationPresent(Bean.class)
              && profiles.matches(method)
              && profiles.matches(method.getReturnType())) {
            if (Modifier.isPublic(method.getModifiers())) {
              var methodClazz = method.getReturnType();
              if (methodClazz.isPrimitive()) {
                throw new IllegalReturnTypeException();
              } else if (methodClazz.equals(Void.TYPE)) {
                throw new IllegalReturnTypeException();
              } else {
                var beanClass =
                    new BeanClass(methodClazz, method.getAnnotation(Bean.class).value());
                if (classBeansMap.containsKey(beanClass)) {
                  throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
                }
                var bean = method.invoke(beanFactoryInstance);
                classBeansMap.put(beanClass, bean);
              }
            } else {
              throw new IllegalDefinedAccessControlException();
            }
          }
        }
      } else if (clazz.isAnnotationPresent(RestController.class)) {
        // fetches all bean instances and save them to rest controller map
        var restControllerInstance = clazz.getDeclaredConstructor().newInstance();
        var beanClass = new BeanClass(clazz, clazz.getAnnotation(RestController.class).value());
        if (classBeansMap.containsKey(beanClass)) {
          throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
        }
        classBeansMap.put(beanClass, restControllerInstance);
        for (var method : clazz.getMethods()) {
          if (method.isAnnotationPresent(RestMapping.class) && profiles.matches(method)) {
            if (Modifier.isPublic(method.getModifiers())) {
              var methodClazz = method.getReturnType();
              if (!methodClazz.equals(HttpServlet.class)) {
                throw new InvalidRestMappingClassException();
              } else {
                String uri = String.join(
                        "/",
                        StringUtility.trimStringByString(clazz.getAnnotation(RestController.class).value(), "/"),
                        StringUtility.trimStringByString(method.getAnnotation(RestMapping.class).value(), "/"));
                uri = StringUtility.trimStringByString(uri, "/");

                beanClass = new BeanClass(methodClazz, uri);
                if (servletBeansMap.containsKey(uri)) {
                  throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
                }
                var bean = method.invoke(restControllerInstance);
                servletBeansMap.put(uri, (HttpServlet) bean);
              }
            } else {
              throw new IllegalDefinedAccessControlException();
            }
          }
        }
      } else if (clazz.isAnnotationPresent(SystemCommand.class)) {
        try {
          var systemCommandAnnotation = clazz.getAnnotation(SystemCommand.class);
          var systemCommandInstance = clazz.getDeclaredConstructor().newInstance();
          if (systemCommandInstance instanceof AbstractSystemCommandHandler handler) {
            // manages by the class bean system
            var beanClass = new BeanClass(clazz, String.valueOf(systemCommandAnnotation.label()));
            if (classBeansMap.containsKey(beanClass)) {
              throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
            }
            classBeansMap.put(beanClass, systemCommandInstance);
            // add to its own management system
            handler.setCommandManager(systemCommandManager);
            systemCommandManager.registerCommand(systemCommandAnnotation.label(), handler);
          } else {
            if (isErrorEnabled()) {
              error(new IllegalArgumentException("Class " + clazz.getName() + " is not a AbstractSystemCommandHandler"));
            }
          }
        } catch (Exception exception) {
          if (isErrorEnabled()) {
            error(exception, "Failed to register command handler for ", clazz.getSimpleName());
          }
        }
      } else if (clazz.isAnnotationPresent(ClientCommand.class)) {
        try {
          var clientCommandAnnotation = clazz.getAnnotation(ClientCommand.class);
          var clientCommandInstance = clazz.getDeclaredConstructor().newInstance();
          if (clientCommandInstance instanceof AbstractClientCommandHandler<?, ?> handler) {
            // manages by the class bean system
            var beanClass = new BeanClass(clazz, String.valueOf(clientCommandAnnotation.value()));
            if (classBeansMap.containsKey(beanClass)) {
              throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
            }
            classBeansMap.put(beanClass, clientCommandInstance);
            // add to its own management system
            handler.setCommandManager(clientCommandManager);
            clientCommandManager.registerCommand(clientCommandAnnotation.value(), (AbstractClientCommandHandler<Player, DataCollection>) handler);
          } else {
            if (isErrorEnabled()) {
              error(new IllegalArgumentException("Class " + clazz.getName() + " is not a AbstractClientCommandHandler"));
            }
          }
        } catch (Exception exception) {
          if (isErrorEnabled()) {
            error(exception, "Failed to register command handler for ", clazz.getSimpleName());
          }
        }
      }
    }

    // Step 3: Wire every instance before making the completed graph available to the application.
    autowireBeans();

    // Step 4 (Extra): Discover to find and register scheduled jobs.
    scheduledJobs.discover(classBeansMap.values(), profiles.activeProfiles());
  }

  /**
   * Retrieves an unnamed active bean by its type during application initialization.
   *
   * <p>Call this method sequentially after scanning has completed and before application services
   * start. Each lookup resolves directly from the current bean registrations.
   *
   * @param <T> the returned type of interface
   * @param clazz the interface class
   * @return the sole unnamed active bean, or null if none exists
   * @throws IllegalStateException if several unnamed active beans satisfy the type
   */
  public <T> T getBean(Class<T> clazz) {
    if (clazz == null) {
      return null;
    }
    var candidates = classBeansMap.entrySet().stream()
        .filter(entry -> entry.getKey().name().isEmpty() && clazz.isInstance(entry.getValue()))
        .map(Entry::getValue)
        .distinct()
        .toList();
    if (candidates.size() > 1) {
      warnAmbiguousDependency(clazz, "getBean()", "", null);
      throw new IllegalStateException("Multiple active beans implement " + clazz.getName()
          + "; select disjoint profiles or use an injection qualifier");
    }
    return candidates.isEmpty() ? null : clazz.cast(candidates.getFirst());
  }

  /**
   * Installs an immutable selection for the next scan, independently of qualifiers.
   *
   * @param names active startup profile names
   */
  public void setProfiles(Set<String> names) {
    profiles = new ProfileSelection(names);
  }

  /**
   * Retrieves a map contains values are interfaces and keys are implemented classes.
   *
   * @return a {@link Map} of classes
   */
  public Map<Class<?>, List<Class<?>>> getClassesMap() {
    return classesMap;
  }

  /**
   * Retrieves all beans.
   *
   * @return a {@link Map} of generated beans
   */
  public Map<BeanClass, Object> getClassBeansMap() {
    return classBeansMap;
  }

  /**
   * Retrieves servlet beans.
   *
   * @return a {@link Map} of servlet beans
   */
  public Map<String, HttpServlet> getServletBeansMap() {
    return servletBeansMap;
  }

  /**
   * Retrieves the system command manager.
   *
   * @return an instance of {@link SystemCommandManager}
   */
  public SystemCommandManager getSystemCommandManager() {
    return systemCommandManager;
  }

  /**
   * Retrieves the client command manager.
   *
   * @return an instance of {@link ClientCommandManager}
   */
  public ClientCommandManager getClientCommandManager() {
    return clientCommandManager;
  }

  /**
   * Returns the jobs registered on fully injected instances during this scan.
   */
  public ScheduledJobs getScheduledJobs() {
    return scheduledJobs;
  }

  /**
   * Retrieves an instance which is declared in a class's field and put it in map of beans as well.
   * This method handles dependency injection for field-level autowiring.
   *
   * @param classInterface the interface using to create a new bean
   * @param nameQualifier this value aims to differentiate which implemented class should be used to create the bean (instance)
   * @param classQualifier this value aims to differentiate which implemented class should be used to create the bean (instance)
   * @return a bean object, an instance of the implemented class
   * @throws NoSuchMethodException if a required method cannot be found
   * @throws InvocationTargetException if a constructor throws an exception
   * @throws InstantiationException if a class cannot be instantiated
   * @throws IllegalAccessException if a class or its constructor is not accessible
   * @throws NoImplementedClassFoundException if no implementation class is found
   * @throws MultipleImplementedClassForInterfaceException if multiple implementations are found
   * @throws DuplicatedBeanCreationException if a bean is created more than once
   */
  private Object getBeanInstanceForInjector(Class<?> classInterface, String nameQualifier, Class<?> classQualifier)
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException,
          NoImplementedClassFoundException,
          MultipleImplementedClassForInterfaceException,
          DuplicatedBeanCreationException {

    if (classQualifier != null) {
      // Factories may declare an interface return type; qualify their active runtime instances too.
      Set<Object> candidates = Collections.newSetFromMap(new IdentityHashMap<>());
      classBeansMap.forEach((key, candidate) -> {
            if (key.name().equals(nameQualifier)
                && classInterface.isInstance(candidate)
                && classQualifier.isInstance(candidate)) {
              candidates.add(candidate);
            }
          });
      if (candidates.size() > 1) {
        throw new MultipleImplementedClassForInterfaceException(classInterface);
      }
      if (!candidates.isEmpty()) {
        return candidates.iterator().next();
      }
    }

    // check classes annotated by @Component and fields annotated by @Autowired
    var implementedClass = getImplementedClass(classInterface, classQualifier);

    var beanClass = new BeanClass(implementedClass, nameQualifier);
    if (classBeansMap.containsKey(beanClass)) {
      return classBeansMap.get(beanClass);
    }

    if (!profiles.matches(implementedClass)) {
      return null;
    }

    if (!manualClassesSet.contains(implementedClass)) {
      if (classBeansMap.containsKey(beanClass)) {
        throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
      }
      var bean = implementedClass.getDeclaredConstructor().newInstance();
      classBeansMap.put(beanClass, bean);
      return bean;
    }

    return null;
  }

  /** Includes ancestors so a concrete profiled handler inherits its abstract parent's events. */
  private List<Class<?>> implementedTypes(Class<?> type) {
    Set<Class<?>> types = new LinkedHashSet<>();
    collectTypes(type, types);
    return List.copyOf(types);
  }

  private void collectTypes(Class<?> type, Set<Class<?>> types) {
    if (type == null || type == Object.class || !types.add(type)) {
      return;
    }
    for (Class<?> contract : type.getInterfaces()) {
      collectTypes(contract, types);
    }
    collectTypes(type.getSuperclass(), types);
  }

  /** Warns before construction about duplicate explicit registration keys. */
  private void warnRegistrationCollisions(Set<Class<?>> definitions) {
    if (!isWarnEnabled()) {
      return;
    }

    Map<String, String> registrations = new HashMap<>();
    for (Class<?> definition : definitions) {
      if (definition.isAnnotationPresent(ClientCommand.class)) {
        warnRegistration(
            registrations,
            "client-command:" + definition.getAnnotation(ClientCommand.class).value(),
            definition.getName()
        );
      }

      if (definition.isAnnotationPresent(SystemCommand.class)) {
        warnRegistration(
            registrations,
            "system-command:" + definition.getAnnotation(SystemCommand.class).label(),
            definition.getName()
        );
      }

      for (var method : definition.getMethods()) {
        if (!profiles.matches(method)) {
          continue;
        }

        if (definition.isAnnotationPresent(BeanFactory.class)
            && method.isAnnotationPresent(Bean.class)
            && profiles.matches(method.getReturnType())) {
          warnRegistration(
              registrations,
              "bean:"
                  + method.getReturnType().getName()
                  + ":"
                  + method.getAnnotation(Bean.class).value(),
              method.toString()
          );
        }

        if (definition.isAnnotationPresent(RestController.class)
            && method.isAnnotationPresent(RestMapping.class)) {
          String uri = String.join(
                  "/",
                  StringUtility.trimStringByString(definition.getAnnotation(RestController.class).value(), "/"),
                  StringUtility.trimStringByString(method.getAnnotation(RestMapping.class).value(), "/"));
          warnRegistration(
              registrations,
              "rest:" + StringUtility.trimStringByString(uri, "/"),
              method.toString()
          );
        }
      }
    }
  }

  private void warnRegistration(Map<String, String> registrations, String key, String definition) {
    String previous = registrations.putIfAbsent(key, definition);
    if (previous != null) {
      warn(
          "[PROFILE COLLISION] ",
          "Active profiles ",
          profiles.activeProfiles(),
          " select duplicate ",
          key,
          ": ",
          previous,
          " and ",
          definition
      );
    }
  }

  /** Reports ambiguity only after the requested dependency's resolution rules have been applied. */
  private void warnAmbiguousDependency(Class<?> type, String target, String nameQualifier, Class<?> classQualifier) {
    if (isWarnEnabled()) {
      warn(
          "[INJECTION AMBIGUITY] ",
          "Active profiles ", profiles.activeProfiles(),
          " cannot resolve ", target,
          " of type ", type.getName(),
          " (name='", nameQualifier,
          "', classQualifier=", classQualifier == null ? "<none>" : classQualifier.getName(),
          "): multiple providers match"
      );
    }
  }

  private boolean isClassAnnotated(Class<?> clazz, Class<? extends Annotation>[] annotations) {
    for (Class<? extends Annotation> annotation : annotations) {
      if (clazz.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }

  private Class<?> getImplementedClass(Class<?> classInterface, Class<?> classQualifier) {
    var implementedClasses = classesMap.entrySet().stream()
            .filter(entry -> entry.getValue().contains(classInterface))
            .collect(Collectors.toSet());

    if (classQualifier != null) {
      // A profile-excluded qualified provider must never fall back to another active provider.
      return implementedClasses.stream()
          .map(Entry::getKey)
          .filter(classQualifier::equals)
          .findFirst()
          .orElseThrow(() -> new NoImplementedClassFoundException(classQualifier));
    }

    if (implementedClasses.isEmpty()) {
      throw new NoImplementedClassFoundException(classInterface);
    }
    if (implementedClasses.size() > 1) {
      // Multiple active implementations require an explicit class qualifier.
      throw new MultipleImplementedClassForInterfaceException(classInterface);
    }
    return implementedClasses.iterator().next().getKey();
  }

  /**
   * Wires registered beans and any named components created while resolving their fields.
   * Each object is queued once by identity, so circular references reuse existing instances
   * without recursively traversing the dependency graph.
   *
   * @throws IllegalArgumentException if invalid arguments are provided
   * @throws SecurityException if a security violation occurs
   * @throws NoSuchMethodException if a required method cannot be found
   * @throws InvocationTargetException if a constructor throws an exception
   * @throws InstantiationException if a class cannot be instantiated
   * @throws IllegalAccessException if a class or its constructor is not accessible
   * @throws DuplicatedBeanCreationException if a bean is created more than once
   */
  private void autowireBeans() throws
          InstantiationException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException,
          DuplicatedBeanCreationException {
    var pending = new ArrayDeque<>();
    Set<Object> scheduled = Collections.newSetFromMap(new IdentityHashMap<>());
    for (var bean : classBeansMap.values()) {
      if (bean != null && scheduled.add(bean)) {
        pending.addLast(bean);
      }
    }

    while (!pending.isEmpty()) {
      var bean = pending.removeFirst();
      // A factory's declared return type may hide fields on the actual implementation.
      for (var field : findFields(bean.getClass())) {
        if (!profiles.matches(field)) {
          continue;
        }
        Class<?> classQualifier = null;
        String nameQualifier = "";

        if (field.isAnnotationPresent(AutowiredQualifier.class)) {
          var qualifier = field.getAnnotation(AutowiredQualifier.class);
          if (!qualifier.clazz().equals(AutowiredQualifier.DEFAULT.class)) {
            classQualifier = qualifier.clazz();
          }
          nameQualifier = qualifier.name();
        }

        boolean optional = field.isAnnotationPresent(AutowiredAcceptNull.class);
        Object fieldInstance;
        try {
          fieldInstance = getBeanInstanceForInjector(field.getType(), nameQualifier, classQualifier);
        } catch (MultipleImplementedClassForInterfaceException exception) {
          warnAmbiguousDependency(
                  field.getType(),
              "field " + field.getDeclaringClass().getName() + "." + field.getName(),
                  nameQualifier,
                  classQualifier
          );
          throw exception;
        } catch (NoImplementedClassFoundException exception) {
          if (optional) {
            // Only this reference is optional; required fields on a resolved bean still must wire.
            continue;
          }
          throw exception;
        }
        if (fieldInstance == null) {
          if (optional) {
            continue;
          }
          // An active factory for this type does not guarantee the requested named bean exists.
          throw new NoImplementedClassFoundException(field.getType());
        }
        field.set(bean, fieldInstance);
        if (scheduled.add(fieldInstance)) {
          pending.addLast(fieldInstance);
        }
      }
    }
  }

  /**
   * Retrieves all the fields annotated by {@link Autowired} or {@link AutowiredAcceptNull}
   * annotation.
   *
   * @param clazz a target class
   * @return a set of fields in the class
   */
  private Set<Field> findFields(Class<?> clazz) {
    var fields = new HashSet<Field>();

    while (clazz != null) {
      for (var field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Autowired.class)
            || field.isAnnotationPresent(AutowiredAcceptNull.class)) {
          field.setAccessible(true);
          fields.add(field);
        }
      }
      // make recursion
      clazz = clazz.getSuperclass();
    }

    return fields;
  }

  /** Clear all references and beans created by the injector. */
  private void cleanup() {
    classesMap.clear();
    classBeansMap.clear();
    manualClassesSet.clear();
    servletBeansMap.clear();
    systemCommandManager.clear();
    systemCommandManager.shutdown();
    clientCommandManager.clear();
    scheduledJobs.shutdown();
  }
}
