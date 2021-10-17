/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.common.bootstrap.injector;

import com.tenio.common.bootstrap.annotation.Autowired;
import com.tenio.common.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotation.AutowiredQualifier;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.bootstrap.utility.ClassLoaderUtility;
import com.tenio.common.exception.MultipleImplementedClassForInterfaceException;
import com.tenio.common.exception.NoImplementedClassFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;
import org.reflections.Reflections;

/**
 * This class helps us create and retrieve <b>"beans"</b> (the class instances).
 */
public final class Injector {

  private static final Injector instance = new Injector();

  /**
   * With the key is the interface and the value holds an implemented class.
   */
  @GuardedBy("this")
  private final Map<Class<?>, Class<?>> classesMap;
  /**
   * With the key is the interface implemented class and the value holds its instance.
   */
  @GuardedBy("this")
  private final Map<Class<?>, Object> classBeansMap;

  private Injector() {
    if (instance != null) {
      throw new ExceptionInInitializerError("Could not re-create the class instance");
    }

    classesMap = new HashMap<Class<?>, Class<?>>();
    classBeansMap = new HashMap<Class<?>, Object>();
  }

  public static Injector newInstance() {
    return instance;
  }

  /**
   * Scans all input packages to create beans and put them into map.
   *
   * @param entryClass the root class which should be located in the parent package of other
   *                   class' packages
   * @param packages   free to define the scanning packages by their names
   * @throws IOException               related to input/output exception
   * @throws IllegalArgumentException  related to illegal argument exception
   * @throws SecurityException         related to security exception
   * @throws ClassNotFoundException    caused by <b>getImplementedClass()</b>
   * @throws NoSuchMethodException     caused by <b>getDeclaredConstructor()</b>
   * @throws InvocationTargetException caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws InstantiationException    caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws IllegalAccessException    caused by <b>getDeclaredConstructor().newInstance()</b>
   */
  public void scanPackages(Class<?> entryClass, String... packages)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException,
      IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
      SecurityException {
    // clean first
    reset();

    var setPackageNames = new HashSet<String>();

    if (entryClass != null) {
      setPackageNames.add(entryClass.getPackage().getName());
    }

    if (packages != null) {
      for (var pack : packages) {
        setPackageNames.add(pack);
      }
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
    var implementedClasses = reflections.getTypesAnnotatedWith(Component.class);

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
      if (clazz.isAnnotationPresent(Component.class)) {
        var bean = clazz.getDeclaredConstructor().newInstance();
        classBeansMap.put(clazz, bean);
        // recursively create field instance for this class instance
        autowire(clazz, bean);
      }
    }
  }

  /**
   * Gets a bean by its declared interface.
   *
   * @param clazz the interface class
   * @param <T>   the returned type of interface
   * @return a bean (an instance of the interface
   */
  public <T> T getBean(Class<T> clazz) {
    var optional = classesMap.entrySet().stream()
        .filter(entry -> entry.getValue() == clazz).findFirst();

    if (optional.isPresent()) {
      return (T) classBeansMap.get(optional.get().getKey());
    }

    return null;
  }

  /**
   * Retrieves a bean which is declared in a class's field and put it in map of beans as well.
   *
   * @param classInterface The interface using to create a new bean
   * @param fieldName      The name of class's field that holds a reference of a bean in a class
   * @param qualifier      To differentiate which implemented class should be used to create the
   *                       bean
   * @param <T>            the type of implemented class
   * @return a bean object, an instance of the implemented class
   * @throws ClassNotFoundException    caused by <b>getImplementedClass()</b>
   * @throws NoSuchMethodException     caused by <b>getDeclaredConstructor()</b>
   * @throws InvocationTargetException caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws InstantiationException    caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws IllegalAccessException    caused by <b>getDeclaredConstructor().newInstance()</b>
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

      var bean = implementedClass.getDeclaredConstructor().newInstance();
      classBeansMap.put(implementedClass, bean);
      return bean;
    }
  }

  private Class<?> getImplementedClass(Class<?> classInterface, String fieldName,
                                       String qualifier) throws ClassNotFoundException {
    var implementedClasses = classesMap.entrySet().stream()
        .filter(entry -> entry.getValue() == classInterface).collect(Collectors.toSet());

    if (implementedClasses == null || implementedClasses.isEmpty()) {
      throw new NoImplementedClassFoundException(classInterface);
    } else if (implementedClasses.size() == 1) {
      // just only one implemented class for the interface
      var optional = implementedClasses.stream().findFirst();
      return optional.map(Entry::getKey).orElseThrow(ClassNotFoundException::new);
    } else if (implementedClasses.size() > 1) {
      // multiple implemented class from the interface, need to be selected by
      // "qualifier" value
      final var findBy =
          (qualifier == null || qualifier.trim().length() == 0) ? fieldName : qualifier;
      var optional = implementedClasses.stream()
          .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy)).findAny();
      // in case of could not find an appropriately single instance, so throw an exception
      return optional.map(Entry::getKey)
          .orElseThrow(() -> new MultipleImplementedClassForInterfaceException(classInterface));
    }

    return null;
  }

  /**
   * Assigns bean values to the corresponding fields in a class.
   *
   * @param clazz the target class that holds declared bean fields
   * @param bean  the bean instance associated with the declared field
   * @throws IllegalArgumentException  related to illegal argument exception
   * @throws SecurityException         related to security exception
   * @throws NoSuchMethodException     caused by <b>getDeclaredConstructor()</b>
   * @throws InvocationTargetException caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws InstantiationException    caused by <b>getDeclaredConstructor().newInstance()</b>
   * @throws IllegalAccessException    caused by <b>getDeclaredConstructor().newInstance()</b>
   * @see Injector
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
          field.set(bean, fieldInstance);
          autowire(fieldInstance.getClass(), fieldInstance);
        } catch (NoImplementedClassFoundException e) {
          // do nothing
        }
      } else if (field.isAnnotationPresent(Autowired.class)) {
        var fieldInstance =
            getBeanInstanceForInjector(field.getType(), field.getName(), qualifier);
        field.set(bean, fieldInstance);
        autowire(fieldInstance.getClass(), fieldInstance);
      }
    }
  }

  /**
   * Retrieves all the fields having {@link Autowired} or {@link AutowiredAcceptNull}
   * annotation used while declaration.
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

  /**
   * Clear all references and beans created by injector.
   */
  private void reset() {
    synchronized (this) {
      classesMap.clear();
      classBeansMap.clear();
    }
  }
}
