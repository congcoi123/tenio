/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Setting;
import com.tenio.core.exception.IllegalDefinedAccessControlException;
import com.tenio.core.exception.IllegalReturnTypeException;
import com.tenio.core.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.core.exception.NoImplementedClassFoundException;
import com.tenio.common.utility.ClassLoaderUtility;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.EventHandler;
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
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.reflections.Reflections;

/**
 * The Injector class supports creating the mechanism for autowiring.
 *
 * @see ClassLoaderUtility
 */
@ThreadSafe
public final class Injector {

  private static final Injector instance = new Injector();

  /**
   * A map contains keys are interfaces and values hold keys' implemented classes.
   *
   * <p>This map is protected by the class instance to ensure thread-safe.
   */
  @GuardedBy("this")
  private final Map<Class<?>, Class<?>> classesMap;
  /**
   * A map has keys are {@link #classesMap}'s key implemented classes and the value are keys'
   * instances.
   *
   * <p>This map is protected by the class instance to ensure thread-safe.
   */
  @GuardedBy("this")
  private final Map<Class<?>, Object> classBeansMap;

  private Injector() {
    if (Objects.nonNull(instance)) {
      throw new ExceptionInInitializerError("Could not re-create the class instance");
    }

    classesMap = new HashMap<>();
    classBeansMap = new HashMap<>();
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
   *
   * @param entryClass the root class which should be located in the parent package of other
   *                   class' packages
   * @param packages   a list of packages' names. It allows to define the scanning packages by
   *                   their names
   * @throws InstantiationException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalAccessException    it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws ClassNotFoundException    it is caused by
   *                                   {@link #getImplementedClass(Class, String, String)}
   * @throws IllegalArgumentException  it is related to the illegal argument exception
   * @throws InvocationTargetException it is caused by
   *                                   Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws NoSuchMethodException     it is caused by
   *                                   {@link Class#getDeclaredConstructor(Class[])}
   * @throws SecurityException         it is related to the security exception
   */
  public void scanPackages(Class<?> entryClass, String... packages)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {

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

    // the implemented class is defined with the "Component" annotation declared inside it
    // in case you need more annotations with the same effect with this one, you should put them
    // in here
    var listAnnotations = new Class[] {
        Component.class,
        EventHandler.class,
        Setting.class
    };
    var implementedClasses = new HashSet<Class<?>>();
    Arrays.stream(listAnnotations).forEach(
        annotation -> implementedClasses.addAll(reflections.getTypesAnnotatedWith(annotation)));

    // retrieves all classes those are declared by the @Bean annotation
    var configurationClasses = reflections.getTypesAnnotatedWith(BeanFactory.class);
    for (var configurationClass : configurationClasses) {
      for (var method : configurationClass.getMethods()) {
        if (method.isAnnotationPresent(Bean.class)) {
          if (Modifier.isPublic(method.getModifiers())) {
            var clazz = method.getReturnType();
            if (clazz.isPrimitive()) {
              throw new IllegalReturnTypeException();
            } else if (clazz.equals(Void.TYPE)) {
              throw new IllegalReturnTypeException();
            } else {
              implementedClasses.add(clazz);
            }
          } else {
            throw new IllegalDefinedAccessControlException();
          }
        }
      }
    }

    // scans all interfaces with their implemented classes
    for (var implementedClass : implementedClasses) {
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

    // create beans (class instances) based on annotations
    for (var clazz : classes) {
      // in case you need to create a bean with another annotation, put it in here
      // but notices to put it in "implementedClasses" first
      if (isClassAnnotated(clazz, listAnnotations)) {
        var bean = clazz.getDeclaredConstructor().newInstance();
        classBeansMap.put(clazz, bean);
        // recursively create field instance for this class instance
        autowire(clazz, bean);
      }

      // fetches all bean instances and save them to classes map
      if (clazz.isAnnotationPresent(BeanFactory.class)) {
        var configurationBean = clazz.getDeclaredConstructor().newInstance();
        for (var method : clazz.getMethods()) {
          if (method.isAnnotationPresent(Bean.class)) {
            if (Modifier.isPublic(method.getModifiers())) {
              var methodClazz = method.getReturnType();
              if (methodClazz.isPrimitive()) {
                throw new IllegalReturnTypeException();
              } else if (methodClazz.equals(Void.TYPE)) {
                throw new IllegalReturnTypeException();
              } else {
                var bean = method.invoke(configurationBean);
                classBeansMap.put(methodClazz, bean);
                // recursively create field instance for this class instance
                autowire(methodClazz, bean);
              }
            } else {
              throw new IllegalDefinedAccessControlException();
            }
          }
        }
      }
    }
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

    return optional.map(classClassEntry -> (T) classBeansMap.get(classClassEntry.getKey()))
        .orElse(null);
  }

  /**
   * Retrieves an instance which is declared in a class's field and put it in map of beans as well.
   *
   * @param classInterface the interface using to create a new bean
   * @param fieldName      the name of class's field that holds a reference of a bean in a class
   * @param qualifier      this value aims to differentiate which implemented class should be
   *                       used to create the bean (instance)
   * @param <T>            the type of implemented class
   * @return a bean object, an instance of the implemented class
   * @throws ClassNotFoundException                        it is caused by
   *                                                       {@link #getImplementedClass(Class, String, String)}
   * @throws NoSuchMethodException                         it is caused by
   *                                                       Class#getDeclaredConstructor(Class[])
   * @throws InvocationTargetException                     it is caused by
   *                                                       Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws InstantiationException                        it is caused by
   * @throws IllegalAccessException                        it is caused by
   *                                                       Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws NoImplementedClassFoundException              this exception should be thrown
   *                                                       when there is no {@link Component} annotation associated class found for the corresponding
   *                                                       declared field in a class
   * @throws MultipleImplementedClassForInterfaceException this exception would be thrown when
   *                                                       there are more than 1 {@link Component} annotation associated with classes that implement
   *                                                       a same interface
   *                                                       Class#getDeclaredConstructor(Class[])#newInstance()
   */
  private <T> Object getBeanInstanceForInjector(Class<T> classInterface, String fieldName,
                                                String qualifier)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
      InstantiationException, IllegalAccessException, NoImplementedClassFoundException,
      MultipleImplementedClassForInterfaceException {

    var implementedClass = getImplementedClass(classInterface, fieldName, qualifier);

    synchronized (classBeansMap) {
      if (classBeansMap.containsKey(implementedClass)) {
        return classBeansMap.get(implementedClass);
      }

      if (Objects.nonNull(implementedClass)) {
        var bean = implementedClass.getDeclaredConstructor().newInstance();
        classBeansMap.put(implementedClass, bean);
        return bean;
      }

      return null;
    }
  }

  private boolean isClassAnnotated(Class<?> clazz, Class<?>[] annotations) {
    for (Class annotation : annotations) {
      if (clazz.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }

  private Class<?> getImplementedClass(Class<?> classInterface, String fieldName,
                                       String qualifier) throws ClassNotFoundException {
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
          (Objects.isNull(qualifier) || qualifier.trim().length() == 0) ? fieldName : qualifier;
      var optional = implementedClasses.stream()
          .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
      // in case of could not find an appropriately single instance, so throw an exception
      return optional.map(Entry::getKey)
          .orElseThrow(() -> new MultipleImplementedClassForInterfaceException(classInterface));
    }
  }

  /**
   * Assigns bean (instance) values to its corresponding fields in a class.
   *
   * @param clazz the target class that holds declared bean fields
   * @param bean  the bean (instance) associated with the declared field
   * @throws IllegalArgumentException  it is related to the illegal argument exception
   * @throws SecurityException         it is related to the security exception
   * @throws NoSuchMethodException     it is caused by Class#getDeclaredConstructor(Class[])
   * @throws InvocationTargetException it is caused by Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws InstantiationException    it is caused by Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws IllegalAccessException    it is caused by Class#getDeclaredConstructor(Class[])#newInstance()
   * @throws InstantiationException    it is caused by Class#getDeclaredConstructor(Class[])#newInstance()
   */
  private void autowire(Class<?> clazz, Object bean)
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException {
    var fields = findFields(clazz);
    for (var field : fields) {
      var qualifier = field.isAnnotationPresent(AutowiredQualifier.class)
          ? field.getAnnotation(AutowiredQualifier.class).value()
          : null;
      if (field.isAnnotationPresent(AutowiredAcceptNull.class)) {
        try {
          var fieldInstance =
              getBeanInstanceForInjector(field.getType(), field.getName(), qualifier);
          if (Objects.nonNull(fieldInstance)) {
            field.set(bean, fieldInstance);
            autowire(fieldInstance.getClass(), fieldInstance);
          }
        } catch (NoImplementedClassFoundException e) {
          // do nothing
        }
      } else if (field.isAnnotationPresent(Autowired.class)) {
        var fieldInstance =
            getBeanInstanceForInjector(field.getType(), field.getName(), qualifier);
        if (Objects.nonNull(fieldInstance)) {
          field.set(bean, fieldInstance);
          autowire(fieldInstance.getClass(), fieldInstance);
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
    synchronized (this) {
      classesMap.clear();
      classBeansMap.clear();
    }
  }
}
