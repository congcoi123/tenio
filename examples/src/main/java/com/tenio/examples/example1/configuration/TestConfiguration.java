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

package com.tenio.examples.example1.configuration;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.bootstrap.annotation.Setting;
import com.tenio.core.configuration.CoreConfiguration;
import com.tenio.examples.server.ExampleConfigurationType;
import java.util.Map;

/**
 * Create your own configurations.
 */
@Setting
public final class TestConfiguration extends CoreConfiguration implements Configuration {

  @Override
  protected void extend(Map<String, String> extProperties) {
    for (Map.Entry<String, String> entry : extProperties.entrySet()) {
      var paramName = entry.getKey();
      push(ExampleConfigurationType.getByValue(paramName), String.valueOf(entry.getValue()));
    }
  }
}
