/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.configuration;

import java.util.Map;

import com.tenio.configuration.BaseConfiguration;
import com.tenio.entity.element.TObject;

/**
 * Create your own configurations
 * 
 * @see BaseConfiguration
 * 
 * @author kong
 *
 */
public final class Configuration extends BaseConfiguration {

	public static final String CUSTOM_VALUE_1 = "c_customvalue_1";
	public static final String CUSTOM_VALUE_2 = "c_customvalue_2";
	public static final String CUSTOM_VALUE_3 = "c_customvalue_3";
	public static final String CUSTOM_VALUE_4 = "c_customvalue_4";

	public Configuration(final String file) {
		super(file);
	}

	@Override
	protected void _extend(TObject extProperties) {
		for (Map.Entry<String, Object> entry : extProperties.entrySet()) {
			switch (entry.getKey()) {
			case "customValue1":
				_put(CUSTOM_VALUE_1, String.valueOf(entry.getValue()));
				break;

			case "customValue2":
				_put(CUSTOM_VALUE_2, String.valueOf(entry.getValue()));
				break;

			case "customValue3":
				_put(CUSTOM_VALUE_3, String.valueOf(entry.getValue()));
				break;

			case "customValue4":
				_put(CUSTOM_VALUE_4, String.valueOf(entry.getValue()));
				break;
			}
		}
	}

}
