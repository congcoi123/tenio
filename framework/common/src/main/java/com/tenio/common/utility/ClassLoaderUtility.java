/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.jar.JarEntry;

/**
 * This utility class provides methods to retrieve all classes from a java package.
 */
public final class ClassLoaderUtility {

  private ClassLoaderUtility() {
    throw new UnsupportedOperationException("This class does not support to create a new "
        + "instance");
  }

  private static void checkDirectory(File directory, String packages,
                                     HashSet<Class<?>> classes) throws ClassNotFoundException {
    File tmpDirectory;

    if (directory.exists() && directory.isDirectory()) {
      final var files = directory.list();

      assert files != null;

      for (final var file : files) {
        if (file.endsWith(".class")) {
          try {
            // ignores 6 characters of the word ".class"
            // only fetch the class name without its extension
            classes.add(Class.forName(packages + '.'
                + file.substring(0, file.length() - 6)));
          } catch (final NoClassDefFoundError e) {
            // do nothing. this class hasn't been found by the
            // loader, and we don't care.
          }
        } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
          checkDirectory(tmpDirectory, packages + "." + file, classes);
        }
      }
    }
  }

  private static void checkJarFile(JarURLConnection connection, String packages,
                                   HashSet<Class<?>> classes)
      throws ClassNotFoundException, IOException {
    final var jarFile = connection.getJarFile();
    final var entries = jarFile.entries();
    String name;

    for (JarEntry jarEntry; entries.hasMoreElements()
        && ((jarEntry = entries.nextElement()) != null); ) {
      name = jarEntry.getName();

      if (name.contains(".class")) {
        name = name.substring(0, name.length() - 6).replace('/', '.');

        if (name.contains(packages)) {
          classes.add(Class.forName(name));
        }
      }
    }
  }

  /**
   * Attempts to list all the classes in the specified package as determined
   * by the context class loader.
   *
   * @param packages the package name to search
   * @return a list of classes that exist within that package
   * @throws ClassNotFoundException if the finding class cannot be found
   */
  public static HashSet<Class<?>> getClasses(String packages)
      throws ClassNotFoundException {
    final var classes = new HashSet<Class<?>>();

    try {
      final var classLoader = Thread.currentThread().getContextClassLoader();

      if (Objects.isNull(classLoader)) {
        throw new ClassNotFoundException("Can't get class loader.");
      }

      final var resources = classLoader.getResources(packages
          .replace('.', '/'));
      URLConnection connection;

      for (URL url; resources.hasMoreElements()
          && ((url = resources.nextElement()) != null); ) {
        try {
          connection = url.openConnection();

          if (connection instanceof JarURLConnection) {
            checkJarFile((JarURLConnection) connection, packages,
                classes);
          } else {
            checkDirectory(
                new File(URLDecoder.decode(url.getPath(),
                    StandardCharsets.UTF_8)), packages, classes);
          }
        } catch (final IOException ioException) {
          throw new ClassNotFoundException(
              "IOException was thrown when trying to get all resources for "
                  + packages, ioException);
        }
      }
    } catch (final NullPointerException nullPointerException) {
      throw new ClassNotFoundException(
          packages
              + " does not appear to be a valid package (Null pointer exception)",
          nullPointerException);
    } catch (final IOException ioException) {
      throw new ClassNotFoundException(
          "IOException was thrown when trying to get all resources for "
              + packages, ioException);
    }

    return classes;
  }
}
