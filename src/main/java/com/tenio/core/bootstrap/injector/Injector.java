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

package com.tenio.core.bootstrap.injector;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.concurrent.ThreadSafe;
import org.reflections.Reflections;

/**
 * The Injector class provides a dependency injection mechanism for the application.
 * It supports automatic wiring of components and beans through annotations.
 *
 * <p>Key features:
 * <ul>
 *   <li>Automatic scanning and instantiation of components marked with {@link Component}</li>
 *   <li>Support for bean factories and bean creation through {@link BeanFactory} and {@link Bean}</li>
 *   <li>Dependency injection through {@link Autowired} and {@link AutowiredQualifier}</li>
 *   <li>Thread-safe implementation for concurrent access</li>
 *   <li>Component lifecycle management</li>
 *   <li>Error handling and validation</li>
 *   <li>Circular dependency detection</li>
 * </ul>
 *
 * <p>Thread safety: This class is thread-safe and can be safely accessed
 * from multiple threads. All operations on the bean registry and component
 * scanning are synchronized.
 *
 * <p>Note: This class is designed to work in conjunction with the
 * {@link Component}, {@link Bean}, {@link Autowired}, and {@link AutowiredQualifier}
 * annotations for proper dependency injection.
 *
 * @see Component
 * @see BeanFactory
 * @see Bean
 * @see Autowired
 * @see AutowiredQualifier
 * @see ClassLoaderUtility
 * @since 0.3.0
 */
@ThreadSafe
public final class Injector extends SystemLogger {

  private static final Injector instance = new Injector();

  /**
   * A map contains values are interfaces and keys are implemented classes.
   *
   * <p>This map is protected by the class instance to ensure thread-safe.
   */
  private final Map<Class<?>, Class<?>> classesMap;
  /**
   * A map has keys are {@link #classesMap}'s key implemented classes and the value are keys'
   * instances.
   *
   * <p>This map is protected by the class instance to ensure thread-safe.
   */
  private final Map<BeanClass, Object> classBeansMap;
  /**
   * A set of classes that are created by {@link Bean} and {@link BeanFactory} annotations.
   * This map is protected by the class instance to ensure thread-safe.
   */
  private final Set<Class<?>> manualClassesSet;
  /**
   * A map is using to initialize restful servlets.
   */
  private final Map<String, HttpServlet> servletBeansMap;
  /**
   * The object manages all supported system commands.
   */
  private final SystemCommandManager systemCommandManager;
  /**
   * The object manages all self-defined user commands.
   */
  private final ClientCommandManager clientCommandManager;

  private Injector() {
    if (Objects.nonNull(instance)) {
      throw new ExceptionInInitializerError("Could not re-create the class instance");
    }

    classesMap = new HashMap<>();
    manualClassesSet = new HashSet<>();
    classBeansMap = new HashMap<>();
    servletBeansMap = new HashMap<>();
    systemCommandManager = new SystemCommandManager();
    clientCommandManager = new ClientCommandManager();
  }

  /**
   * Returns an instance of the injector.
   *
   * @return an instance of the injector
   */
  public static Injector newInstance() {
    return instance;
  }

  /**
   * Scans all input packages to create classes' instances and put them into maps.
   * This method performs component scanning and dependency injection in a single
   * atomic operation.
   *
   * @param entryClass the root class which should be located in the parent package of other
   *                   class' packages
   * @param packages   a list of packages' names. It allows to define the scanning packages by
   *                   their names
   * @throws InstantiationException          if a class cannot be instantiated
   * @throws IllegalAccessException          if a class or its constructor is not accessible
   * @throws ClassNotFoundException          if a required class cannot be found
   * @throws IllegalArgumentException        if invalid arguments are provided
   * @throws InvocationTargetException       if a constructor throws an exception
   * @throws NoSuchMethodException           if a required method cannot be found
   * @throws SecurityException               if a security violation occurs
   * @throws DuplicatedBeanCreationException if a bean is created more than once
   */
  @SuppressWarnings("unchecked")
  public void scanPackages(Class<?> entryClass, String... packages)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException, DuplicatedBeanCreationException {

    // clean first
    reset();

    var setPackageNames = new HashSet<String>();

    if (Objects.nonNull(entryClass)) {
      setPackageNames.add(entryClass.getPackage().getName());
    }

    if (Objects.nonNull(packages)) {
      setPackageNames.addAll(Arrays.asList(packages));
    }

    // fetches all classes that are in the same package as the root one
    var classes = new HashSet<Class<?>>();
    // declares a reflection object based on the package of root class
    var reflections = new Reflections();
    for (var packageName : setPackageNames) {
      var packageClasses = ClassLoaderUtility.getClasses(packageName);
      classes.addAll(packageClasses);

      var reflectionPackage = new Reflections(packageName);
      reflections.merge(reflectionPackage);
    }

    // Step 1: We collect classes
    // The implemented class is defined with the "Component" annotation declared inside it
    // in case you need more annotations with the same effect with this one, you should put them
    // in here
    var listAnnotations = new Class[] {
        Component.class,
        EventHandler.class,
        Setting.class
    };
    var implementedComponentClasses = new HashSet<Class<?>>();
    Arrays.stream(listAnnotations).forEach(
        annotation -> implementedComponentClasses.addAll(
            reflections.getTypesAnnotatedWith(annotation)));
    // scans all interfaces with their implemented classes
    for (var implementedClass : implementedComponentClasses) {
      var classInterfaces = implementedClass.getInterfaces();
      // in case the class has not implemented any interfaces, it still can be created, so put
      // the class into the map
      if (classInterfaces.length == 0) {
        classesMap.put(implementedClass, implementedClass);
      } else {
        // normal case, put the pair of class and interface
        // the interface will be used to retrieved back the corresponding class when we want to
        // create a bean by its interface
        for (var classInterface : classInterfaces) {
          classesMap.put(implementedClass, classInterface);
        }
      }
    }

    // Retrieves all classes those are declared by the @Bean annotation
    var implementedBeanClasses = new HashSet<Class<?>>();
    var beanFactoryClasses = reflections.getTypesAnnotatedWith(BeanFactory.class);
    for (var configurationClass : beanFactoryClasses) {
      for (var method : configurationClass.getMethods()) {
        if (method.isAnnotationPresent(Bean.class)) {
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
    // append all classes
    for (var implementedClass : implementedBeanClasses) {
      classesMap.put(implementedClass, implementedClass);
    }

    // Add all classes annotated by @RestController
    var implementedRestClasses =
        new HashSet<>(reflections.getTypesAnnotatedWith(RestController.class));
    // append all classes
    for (var implementedClass : implementedRestClasses) {
      classesMap.put(implementedClass, implementedClass);
    }

    // Step 2: We create instances
    // create beans (class instances) based on annotations
    for (var clazz : classes) {
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
          if (method.isAnnotationPresent(Bean.class)) {
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
        var beanClass =
            new BeanClass(clazz, clazz.getAnnotation(RestController.class).value());
        if (classBeansMap.containsKey(beanClass)) {
          throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
        }
        classBeansMap.put(beanClass, restControllerInstance);
        for (var method : clazz.getMethods()) {
          if (method.isAnnotationPresent(RestMapping.class)) {
            if (Modifier.isPublic(method.getModifiers())) {
              var methodClazz = method.getReturnType();
              if (!methodClazz.equals(HttpServlet.class)) {
                throw new InvalidRestMappingClassException();
              } else {
                String uri = String.join("/", StringUtility.trimStringByString(
                        clazz.getAnnotation(RestController.class).value(), "/"),
                    StringUtility.trimStringByString(
                        method.getAnnotation(RestMapping.class).value(), "/"));
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
            var beanClass =
                new BeanClass(clazz, String.valueOf(systemCommandAnnotation.label()));
            if (classBeansMap.containsKey(beanClass)) {
              throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
            }
            classBeansMap.put(beanClass, systemCommandInstance);
            // add to its own management system
            handler.setCommandManager(systemCommandManager);
            systemCommandManager.registerCommand(systemCommandAnnotation.label(), handler);
          } else {
            error(new IllegalArgumentException("Class " + clazz.getName() + " is not a " +
                "AbstractSystemCommandHandler"));
          }
        } catch (Exception exception) {
          error(exception, "Failed to register command handler for ", clazz.getSimpleName());
        }
      } else if (clazz.isAnnotationPresent(ClientCommand.class)) {
        try {
          var clientCommandAnnotation = clazz.getAnnotation(ClientCommand.class);
          var clientCommandInstance = clazz.getDeclaredConstructor().newInstance();
          if (clientCommandInstance instanceof AbstractClientCommandHandler<?> handler) {
            // manages by the class bean system
            var beanClass =
                new BeanClass(clazz, String.valueOf(clientCommandAnnotation.value()));
            if (classBeansMap.containsKey(beanClass)) {
              throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
            }
            classBeansMap.put(beanClass, clientCommandInstance);
            // add to its own management system
            handler.setCommandManager(clientCommandManager);
            clientCommandManager.registerCommand(clientCommandAnnotation.value(),
                (AbstractClientCommandHandler<Player>) handler);
          } else {
            error(new IllegalArgumentException("Class " + clazz.getName() + " is not a " +
                "AbstractClientCommandHandler"));
          }
        } catch (Exception exception) {
          error(exception, "Failed to register command handler for ", clazz.getSimpleName());
        }
      }
    }

    // Step 3: Make mapping between classes and their instances
    // recursively create field instance for this class instance
    classBeansMap.forEach((clazz, bean) -> {
      try {
        autowire(clazz, bean);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | DuplicatedBeanCreationException exception) {
        error("Initialize class: ", clazz.clazz().getName(), "\n", getStackTrace(exception));
      }
    });
  }

  private String getStackTrace(Exception exception) {
    StringBuilder stringBuilder = new StringBuilder();
    StackTraceElement[] stackTraceElements = exception.getStackTrace();
    stringBuilder.append(exception.getClass().getName()).append(": ").append(exception.getMessage())
        .append("\n");
    for (StackTraceElement stackTraceElement : stackTraceElements) {
      stringBuilder.append("\t at ").append(stackTraceElement.toString()).append("\n");
    }
    return stringBuilder.toString();
  }

  /**
   * Retrieves an instance by using its corresponding declared interface.
   *
   * @param <T>   the returned type of interface
   * @param clazz the interface class
   * @return a bean (an instance of the interface)
   */
  @SuppressWarnings("unchecked")
  public <T> T getBean(Class<T> clazz) {
    var optional = classesMap.entrySet().stream()
        .filter(entry -> entry.getValue() == clazz).findFirst();

    return optional.map(
            classClassEntry -> (T) classBeansMap.get(new BeanClass(classClassEntry.getKey(), "")))
        .orElse(null);
  }

  /**
   * Retrieves a map contains values are interfaces and keys are implemented classes.
   *
   * @return a {@link Map} of classes
   */
  public Map<Class<?>, Class<?>> getClassesMap() {
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
   * Retrieves an instance which is declared in a class's field and put it in map of beans as well.
   * This method handles dependency injection for field-level autowiring.
   *
   * @param classInterface the interface using to create a new bean
   * @param fieldName      the name of class's field that holds a reference of a bean in a class
   * @param nameQualifier  this value aims to differentiate which implemented class should be
   *                       used to create the bean (instance)
   * @param classQualifier this value aims to differentiate which implemented class should be
   *                       used to create the bean (instance)
   * @return a bean object, an instance of the implemented class
   * @throws ClassNotFoundException                        if a required class cannot be found
   * @throws NoSuchMethodException                         if a required method cannot be found
   * @throws InvocationTargetException                     if a constructor throws an exception
   * @throws InstantiationException                        if a class cannot be instantiated
   * @throws IllegalAccessException                        if a class or its constructor is not accessible
   * @throws NoImplementedClassFoundException              if no implementation class is found
   * @throws MultipleImplementedClassForInterfaceException if multiple implementations are found
   * @throws DuplicatedBeanCreationException               if a bean is created more than once
   */
  private Object getBeanInstanceForInjector(Class<?> classInterface, String fieldName,
                                            String nameQualifier, Class<?> classQualifier)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException, NoImplementedClassFoundException,
      MultipleImplementedClassForInterfaceException, DuplicatedBeanCreationException {

    // check classes annotated by @Component and fields annotated by @Autowired
    var implementedClass = getImplementedClass(classInterface, fieldName, classQualifier);

    var beanClass = new BeanClass(implementedClass, nameQualifier);
    if (classBeansMap.containsKey(beanClass)) {
      return classBeansMap.get(beanClass);
    }

    if (Objects.nonNull(implementedClass) && !manualClassesSet.contains(beanClass.getClass())) {
      if (classBeansMap.containsKey(beanClass)) {
        throw new DuplicatedBeanCreationException(beanClass.clazz(), beanClass.name());
      }
      var bean = implementedClass.getDeclaredConstructor().newInstance();
      classBeansMap.put(beanClass, bean);
      return bean;
    }

    return null;
  }

  private boolean isClassAnnotated(Class<?> clazz, Class<? extends Annotation>[] annotations) {
    for (Class<? extends Annotation> annotation : annotations) {
      if (clazz.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }

  private Class<?> getImplementedClass(Class<?> classInterface, String fieldName,
                                       Class<?> classQualifier) throws ClassNotFoundException {
    var implementedClasses = classesMap.entrySet().stream()
        .filter(entry -> entry.getValue() == classInterface).collect(Collectors.toSet());

    if (implementedClasses.isEmpty()) {
      throw new NoImplementedClassFoundException(classInterface);
    } else if (implementedClasses.size() == 1) {
      // just only one implemented class for the interface
      var optional = implementedClasses.stream().findFirst();
      return optional.map(Entry::getKey).orElseThrow(ClassNotFoundException::new);
    } else {
      // multiple implemented class from the interface, need to be selected by
      // "qualifier" value
      final var findBy =
          (Objects.isNull(classQualifier)) ? fieldName : classQualifier;
      var optional = implementedClasses.stream()
          .filter(entry -> entry.getKey().equals(findBy)).findAny();
      // in case of could not find an appropriately single instance, so throw an exception
      return optional.map(Entry::getKey)
          .orElseThrow(
              () -> new MultipleImplementedClassForInterfaceException(classInterface));
    }
  }

  /**
   * Assigns bean (instance) values to its corresponding fields in a class.
   * This method performs field-level dependency injection.
   *
   * @param beanClass the target class that holds declared bean fields
   * @param bean      the bean (instance) associated with the declared field
   * @throws IllegalArgumentException        if invalid arguments are provided
   * @throws SecurityException               if a security violation occurs
   * @throws NoSuchMethodException           if a required method cannot be found
   * @throws InvocationTargetException       if a constructor throws an exception
   * @throws InstantiationException          if a class cannot be instantiated
   * @throws IllegalAccessException          if a class or its constructor is not accessible
   * @throws DuplicatedBeanCreationException if a bean is created more than once
   */
  private void autowire(BeanClass beanClass, Object bean)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException,
      DuplicatedBeanCreationException {
    var fields = findFields(beanClass.clazz());
    for (var field : fields) {
      Class<?> classQualifier = null;
      String nameQualifier = "";

      if (field.isAnnotationPresent(AutowiredQualifier.class)) {
        var classDefault = field.getAnnotation(AutowiredQualifier.class).clazz();
        if (!classDefault.equals(AutowiredQualifier.DEFAULT.class)) {
          classQualifier = classDefault;
        }
        nameQualifier = field.getAnnotation(AutowiredQualifier.class).name();
      }

      if (field.isAnnotationPresent(AutowiredAcceptNull.class)) {
        try {
          var fieldInstance =
              getBeanInstanceForInjector(field.getType(), field.getName(), nameQualifier,
                  classQualifier);
          if (Objects.nonNull(fieldInstance)) {
            field.set(bean, fieldInstance);
            autowire(new BeanClass(fieldInstance.getClass(), nameQualifier), fieldInstance);
          }
        } catch (NoImplementedClassFoundException e) {
          // do nothing
        }
      } else if (field.isAnnotationPresent(Autowired.class)) {
        var fieldInstance =
            getBeanInstanceForInjector(field.getType(), field.getName(), nameQualifier,
                classQualifier);
        if (Objects.nonNull(fieldInstance)) {
          field.set(bean, fieldInstance);
          autowire(new BeanClass(fieldInstance.getClass(), nameQualifier), fieldInstance);
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

    while (Objects.nonNull(clazz)) {
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

  /**
   * Clear all references and beans created by the injector.
   */
  private void reset() {
    classesMap.clear();
    classBeansMap.clear();
    manualClassesSet.clear();
    servletBeansMap.clear();
    systemCommandManager.clear();
    clientCommandManager.clear();
  }
}

