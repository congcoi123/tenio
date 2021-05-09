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
package com.tenio.common.utility;

import java.util.Locale;

/**
 * @author kong
 */
// TODO: Add description
public final class OsUtility {
	
	/**
	 * Types of Operating Systems
	 */
	public enum OSType {
		Windows, MacOS, Linux, Other
	};
	
	private OsUtility() {
		
	}

	/**
	 * Detect the operating system from the os.name System property and cache the
	 * result
	 * 
	 * @return The operating system detected
	 */
	public static OSType getOperatingSystemType() {

		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			return OSType.MacOS;
		} else if (OS.indexOf("win") >= 0) {
			return OSType.Windows;
		} else if (OS.indexOf("nux") >= 0) {
			return OSType.Linux;
		} else {
			return OSType.Other;
		}

	}
}
