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

package com.tenio.core.bootstrap.injector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tenio.core.bootstrap.annotation.Asynchronous;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Bootstrap;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import com.tenio.core.bootstrap.annotation.Setting;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Startup profile selection policy")
class ProfileSelectionTest {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n", ",", " , , \t"})
  void requiredXmlValueRejectsMissingOrEmptyNames(String value) {
    var exception = assertThrows(IllegalArgumentException.class,
        () -> ProfileSelection.parseRequired(value));
    assertTrue(exception.getMessage().contains("profile"));
  }

  @Test
  void requiredXmlValueTrimsDeduplicatesAndPreservesDeclarationOrder() {
    Set<String> profiles = ProfileSelection.parseRequired(" world, game, world, , social ");

    assertEquals(List.of("world", "game", "social"), List.copyOf(profiles));
    assertThrows(UnsupportedOperationException.class, () -> profiles.add("extra"));
  }

  @Test
  void constructionNormalizesCsvEntriesAndDetachesFromCaller() {
    Set<String> input = new LinkedHashSet<>(Arrays.asList(" world,game ", null, "game", " "));
    ProfileSelection selection = new ProfileSelection(input);
    input.clear();
    input.add("social");

    assertEquals(List.of("world", "game"), List.copyOf(selection.activeProfiles()));
    assertThrows(UnsupportedOperationException.class, () -> selection.activeProfiles().clear());
  }

  @Test
  void emptySelectionAllowsUnrestrictedDefinitionsButNoNamedProfile() {
    for (ProfileSelection selection : List.of(new ProfileSelection(null), new ProfileSelection(Set.of()))) {
      assertTrue(selection.activeProfiles().isEmpty());
      assertTrue(selection.matches());
      assertTrue(selection.matches((String[]) null));
      assertTrue(selection.matches(null, " ", ",,"));
      assertFalse(selection.matches("world"));
    }
  }

  @Test
  void alternativesUseExactCaseSensitiveOrMatching() {
    ProfileSelection selection = new ProfileSelection(Set.of("world", "social"));

    assertTrue(selection.matches("game", " world,other "));
    assertTrue(selection.matches("social"));
    assertFalse(selection.matches("WORLD"));
    assertFalse(selection.matches("worldwide", "wor", "*"));
    assertEquals(Set.of("World", "world"), ProfileSelection.parseRequired("World,world"));
  }

  @Test
  void everyAnnotationOnAnElementMustAcceptTheSelection() throws Exception {
    AnnotatedElement field = Fields.class.getDeclaredField("bothProfiles");

    assertFalse(new ProfileSelection(Set.of("world")).matches(field));
    assertFalse(new ProfileSelection(Set.of("social")).matches(field));
    assertTrue(new ProfileSelection(Set.of("world", "social")).matches(field));
  }

  @Test
  void ignoresUnrelatedAnnotationsAndUnannotatedElements() throws Exception {
    ProfileSelection selection = new ProfileSelection(Set.of("world"));

    assertTrue(selection.matches(Fields.class));
    assertTrue(selection.matches(Fields.class.getDeclaredField("externalAnnotation")));
    assertTrue(selection.matches(Fields.class.getDeclaredField("unrestricted")));
  }

  @ParameterizedTest(name = "{0} exposes an unrestricted runtime profile by default")
  @MethodSource("bootstrapAnnotations")
  void allBootstrapAnnotationsExposeTheSameProfileContract(Class<? extends Annotation> type) throws Exception {
    var profile = type.getDeclaredMethod("profile");

    assertEquals(String[].class, profile.getReturnType());
    assertArrayEquals(new String[0], (String[]) profile.getDefaultValue());
    assertEquals(RetentionPolicy.RUNTIME, type.getAnnotation(Retention.class).value());
  }

  private static Stream<Class<? extends Annotation>> bootstrapAnnotations() {
    return Stream.of(Asynchronous.class, Autowired.class, AutowiredAcceptNull.class,
        AutowiredQualifier.class, Bean.class, BeanFactory.class, Bootstrap.class,
        ClientCommand.class, Component.class, EventHandler.class, RestController.class,
        RestMapping.class, Setting.class, SystemCommand.class);
  }

  private static class Fields {

    @Autowired(profile = {"world", "game"})
    @AutowiredAcceptNull(profile = "social")
    private Object bothProfiles;

    @Deprecated
    private Object externalAnnotation;

    @Autowired(profile = {" ", ",,"})
    private Object unrestricted;
  }
}
