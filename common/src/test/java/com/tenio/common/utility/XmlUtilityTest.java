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

package com.tenio.common.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.imageio.metadata.IIOMetadataNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@DisplayName("Unit Test Cases For XML Utility")
class XmlUtilityTest {

  @Test
  @DisplayName("Throw an exception when the class's instance is attempted creating")
  void createNewInstanceShouldThrowException() throws NoSuchMethodException {
    var constructor = XmlUtility.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    assertThrows(InvocationTargetException.class, () -> {
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }

  @Test
  void testParseFileAndNavigation() throws Exception {
    File file = new File("src/test/resources/test.xml");
    Document doc = XmlUtility.parseFile(file);
    assertNotNull(doc);

    NodeList nodes = XmlUtility.getNodeList(doc, "//item");
    assertEquals(2, nodes.getLength());

    Node firstNode = XmlUtility.getNode(doc, "//item[@id='1']");
    assertNotNull(firstNode);
    assertEquals("Value 1", XmlUtility.getNodeValue(firstNode));
    assertEquals("1", XmlUtility.getAttrVal(firstNode, "id"));
  }

  @Test
  void testParseStream() throws Exception {
    try (FileInputStream fis = new FileInputStream("src/test/resources/test.xml")) {
      Document doc = XmlUtility.parseStream(fis);
      assertNotNull(doc);
    }
  }

  @Test
  void testGetNodeValue() throws DOMException {
    Node node = mock(Node.class);
    when(node.getTextContent()).thenReturn("Not all who wander are lost");
    assertEquals("Not all who wander are lost", XmlUtility.getNodeValue(node));
    verify(node).getTextContent();
  }

  @Test
  void testGetAttrVal() {
    IIOMetadataNode node = new IIOMetadataNode("Node Name");
    node.setAttribute("Name", "Value");
    assertEquals("Value", XmlUtility.getAttrVal(node, "Name"));
  }
}
