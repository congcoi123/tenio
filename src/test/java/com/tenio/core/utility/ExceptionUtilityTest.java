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

package com.tenio.core.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test Cases For ExceptionUtility")
class ExceptionUtilityTest {

  @Test
  @DisplayName("Test messageContains returns false when exception is null")
  void testMessageContainsWithNullException() {
    assertFalse(ExceptionUtility.messageContains(null, Set.of("keyword")));
  }

  @Test
  @DisplayName("Test messageContains returns false when message is null")
  void testMessageContainsWithNullMessage() {
    Exception ex = new Exception((String) null);
    assertFalse(ExceptionUtility.messageContains(ex, Set.of("keyword")));
  }

  @Test
  @DisplayName("Test messageContains returns false when keywords is null")
  void testMessageContainsWithNullKeywords() {
    Exception ex = new Exception("some message");
    assertFalse(ExceptionUtility.messageContains(ex, null));
  }

  @Test
  @DisplayName("Test messageContains returns false when keywords is empty")
  void testMessageContainsWithEmptyKeywords() {
    Exception ex = new Exception("some message");
    assertFalse(ExceptionUtility.messageContains(ex, Set.of()));
  }

  @Test
  @DisplayName("Test messageContains returns true when keyword is found")
  void testMessageContainsWithMatchingKeyword() {
    Exception ex = new Exception("broken pipe detected");
    assertTrue(ExceptionUtility.messageContains(ex, Set.of("broken pipe")));
  }

  @Test
  @DisplayName("Test messageContains is case insensitive")
  void testMessageContainsIsCaseInsensitive() {
    Exception ex = new Exception("Broken Pipe");
    assertTrue(ExceptionUtility.messageContains(ex, ExceptionUtility.IGNORE_LOGGING_EXCEPTIONS));
  }

  @Test
  @DisplayName("Test messageContains with keyword in mixed case message")
  void testMessageContainsMixedCaseMessage() {
    Exception ex = new Exception("CONNECTION RESET by peer");
    assertTrue(ExceptionUtility.messageContains(ex, ExceptionUtility.IGNORE_LOGGING_EXCEPTIONS));
  }

  @Test
  @DisplayName("Test messageContains returns false when no keyword matches")
  void testMessageContainsNoMatch() {
    Exception ex = new Exception("unrelated error occurred");
    assertFalse(ExceptionUtility.messageContains(ex, Set.of("broken pipe", "connection reset")));
  }

  @Test
  @DisplayName("Test messageContains handles null keyword inside the set")
  void testMessageContainsWithNullKeywordInSet() {
    Set<String> keywords = new HashSet<>();
    keywords.add(null);
    keywords.add("broken pipe");
    Exception ex = new Exception("broken pipe issue");
    assertTrue(ExceptionUtility.messageContains(ex, keywords));
  }

  @Test
  @DisplayName("Test IGNORE_LOGGING_EXCEPTIONS constant contains 'broken pipe'")
  void testIgnoreLoggingExceptionsContainsBrokenPipe() {
    assertTrue(ExceptionUtility.IGNORE_LOGGING_EXCEPTIONS.contains("broken pipe"));
  }

  @Test
  @DisplayName("Test IGNORE_LOGGING_EXCEPTIONS constant contains 'connection reset'")
  void testIgnoreLoggingExceptionsContainsConnectionReset() {
    assertTrue(ExceptionUtility.IGNORE_LOGGING_EXCEPTIONS.contains("connection reset"));
  }

  @Test
  @DisplayName("Test private constructor throws UnsupportedOperationException")
  void testPrivateConstructorThrows() throws Exception {
    Constructor<ExceptionUtility> constructor =
        ExceptionUtility.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }
}
