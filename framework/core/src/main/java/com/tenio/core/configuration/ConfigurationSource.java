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

package com.tenio.core.configuration;

import com.tenio.common.utility.XmlUtility;
import com.tenio.core.bootstrap.injector.ProfileSelection;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.configuration.define.CoreConfigurationType;
import java.io.File;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

/** XML read once for bootstrap profile selection and runtime configuration. */
public final class ConfigurationSource {

  private final Document document;
  private final String description;

  private ConfigurationSource(Document document, String description) {
    this.document = document;
    this.description = description;
  }

  public static ConfigurationSource read(String path, ClassLoader loader) throws Exception {
    if (path != null && new File(path).exists()) {
      return new ConfigurationSource(XmlUtility.parseFile(new File(path)),
          "The external configuration file: " + path);
    }
    try (var stream = loader.getResourceAsStream(CoreConstant.DEFAULT_CONFIGURATION_FILE)) {
      return new ConfigurationSource(XmlUtility.parseStream(stream),
          "The external configuration file is not found, use the default: "
              + CoreConstant.DEFAULT_CONFIGURATION_FILE);
    }
  }

  public Set<String> profiles() throws XPathExpressionException {
    var profiles = XmlUtility.getNodeList(document, "//Server/Properties/Property[@name='"
        + CoreConfigurationType.PROFILE.getValue() + "']");
    int count = profiles.getLength();
    String value = count == 0 ? null : profiles.item(count - 1).getTextContent();
    return value != null && value.isBlank() ? Set.of() : ProfileSelection.parseRequired(value);
  }

  Document document() {
    return document;
  }

  String description() {
    return description;
  }
}
