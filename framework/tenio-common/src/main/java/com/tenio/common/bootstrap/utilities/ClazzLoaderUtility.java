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
package com.tenio.common.bootstrap.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.tenio.common.utilities.StringUtility;

public final class ClazzLoaderUtility {

	private ClazzLoaderUtility() {

	}

	public static Class<?>[] getClazzs(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader clazzLoader = Thread.currentThread().getContextClassLoader();
		assert clazzLoader != null;

		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = clazzLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();

		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}

		List<Class<?>> clazzs = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			clazzs.addAll(findClazzs(directory, packageName));
		}

		return clazzs.toArray(new Class[clazzs.size()]);
	}

	public static List<Class<?>> findClazzs(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> clazzs = new ArrayList<Class<?>>();

		if (!directory.exists()) {
			return clazzs;
		}

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				clazzs.addAll(findClazzs(file, StringUtility.strgen(packageName, ".", file.getName())));
			} else if (file.getName().endsWith(".class")) {
				String clazzName = StringUtility.strgen(packageName, ".",
						file.getName().substring(0, file.getName().length() - 6));
				clazzs.add(Class.forName(clazzName));
			}
		}
		return clazzs;
	}

}
