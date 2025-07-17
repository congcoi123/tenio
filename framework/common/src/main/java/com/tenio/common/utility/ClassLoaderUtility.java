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

package com.tenio.common.utility;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This utility class provides methods to retrieve all classes from a java package.
 */
public final class ClassLoaderUtility {

  private ClassLoaderUtility() {
    throw new UnsupportedOperationException("This class does not support to create a new "
        + "instance");
  }

  private static void scanDirectory(File directory, String packageName, Set<Class<?>> classes)
      throws ClassNotFoundException {
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }

    for (var file : files) {
      if (file.isDirectory()) {
        scanDirectory(file, packageName + "." + file.getName(), classes);
      } else if (file.getName().endsWith(".class")) {
        String className = packageName + '.' + file.getName().replace(".class", "");
        try {
          classes.add(
              Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
        } catch (NoClassDefFoundError ignored) {
        }
      }
    }
  }

  private static void scanJarFile(String jarPath, String packageName, Set<Class<?>> classes)
      throws ClassNotFoundException {
    try (JarFile jarFile = new JarFile(jarPath)) {
      String prefix = packageName.replace('.', '/');
      Enumeration<JarEntry> entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();

        if (name.endsWith(".class") && name.startsWith(prefix)) {
          String className = name.replace('/', '.').replace(".class", "");
          try {
            classes.add(
                Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
          } catch (NoClassDefFoundError ignored) {
          }
        }
      }
    } catch (IOException exception) {
      throw new ClassNotFoundException("Failed to read jar file: " + jarPath, exception);
    }
  }

  /**
   * Attempts to list all the classes in the specified packages as determined
   * by the context class loader.
   *
   * @param packageName the package name to search
   * @return a set of classes that exist within that package
   * @throws ClassNotFoundException if the finding class cannot be found
   */
  public static Set<Class<?>> getClasses(String packageName) throws ClassNotFoundException {
    var classes = new HashSet<Class<?>>();
    var packagePath = packageName.replace('.', '/');

    try {
      Enumeration<URL> resources =
          Thread.currentThread().getContextClassLoader().getResources(packagePath);
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        String decodedPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

        if (decodedPath.endsWith(".jar") || decodedPath.contains(".jar!")) {
          // Example: file:/path/to/your.jar!/com/example/
          String jarFilePath = decodedPath.substring(0, decodedPath.indexOf(".jar") + 4)
              .replace("file:", "");
          scanJarFile(jarFilePath, packageName, classes);
        } else {
          File directory = new File(decodedPath);
          if (directory.exists() && directory.isDirectory()) {
            scanDirectory(directory, packageName, classes);
          }
        }
      }
    } catch (IOException exception) {
      throw new ClassNotFoundException("Failed to read resources", exception);
    }

    return classes;
  }

  /**
   * Attempts to list all the classes in the specified packages as determined
   * by the context class loader.
   *
   * @param packages the set of package names to search
   * @return a set of classes that exist within that package
   * @throws ClassNotFoundException if the finding class cannot be found
   */
  public static Set<Class<?>> getClasses(Set<String> packages) throws ClassNotFoundException {
    var classes = new HashSet<Class<?>>();
    for (var packageName : packages) {
      classes.addAll(getClasses(packageName));
    }
    return classes;
  }

  /**
   * Fetches classes by their annotations.
   *
   * @param allClasses all scanned {@link Class}es
   * @param annotation {@link Annotation} which is annotated on classes
   * @return a set of classes if available, or an empty one
   */
  public static Set<Class<?>> getTypesAnnotatedWith(Set<Class<?>> allClasses,
                                                    Class<? extends Annotation> annotation) {
    var annotatedClasses = new HashSet<Class<?>>();
    for (var clazz : allClasses) {
      if (clazz.isAnnotationPresent(annotation)) {
        annotatedClasses.add(clazz);
      }
    }
    return annotatedClasses;
  }
}
