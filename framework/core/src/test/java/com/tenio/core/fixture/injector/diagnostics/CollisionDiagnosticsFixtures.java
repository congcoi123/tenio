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

package com.tenio.core.fixture.injector.diagnostics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenio.common.data.DataCollection;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.entity.Player;
import com.tenio.core.utility.HttpUtility;
import jakarta.servlet.http.HttpServlet;
import java.io.Serializable;
import java.util.List;

/** Isolated registrations distinguishing harmless overlap from an ambiguous dependency. */
public final class CollisionDiagnosticsFixtures {

  private CollisionDiagnosticsFixtures() {
  }

  @BeanFactory(profile = {"serializable-unused", "serializable-required", "inactive-fields"})
  public static class SerializableFactory {
    @Bean
    public ObjectMapper mapper() {
      return new ObjectMapper();
    }

    @Bean
    public HttpUtility http() {
      return HttpUtility.INSTANCE;
    }
  }

  @Component(profile = "serializable-required")
  public static class SerializableTarget {
    @Autowired public Serializable dependency;
  }

  @Component(profile = "inactive-fields")
  public static class InactiveConsumer {
    @Autowired(profile = "excluded") public Serializable required;
    @AutowiredAcceptNull(profile = "excluded") public Serializable optional;
  }

  public interface Service {
  }

  @Component(profile = {
      "service-unused", "service-qualified", "service-optional", "service-inherited"})
  public static class FirstService implements Service {
  }

  @Component(profile = {
      "service-unused", "service-qualified", "service-optional", "service-inherited"})
  public static class SecondService implements Service {
  }

  @Component(profile = "service-qualified")
  public static class QualifiedConsumer {
    @Autowired
    @AutowiredQualifier(clazz = FirstService.class)
    public Service dependency;
  }

  @Component(profile = "service-optional")
  public static class OptionalConsumer {
    @AutowiredAcceptNull public Service dependency;
  }

  public abstract static class InheritedParent {
    @Autowired private Service dependency;
  }

  @Component(profile = "service-inherited")
  public static class InheritedConsumer extends InheritedParent {
  }

  public interface NamedService {
  }

  public static class NamedProduct implements NamedService {
  }

  @BeanFactory(profile = "name-qualified")
  public static class NamedFactory {
    @Bean("blue")
    public NamedService blue() {
      return new NamedProduct();
    }

    @Bean("red")
    public NamedService red() {
      return new NamedProduct();
    }
  }

  @Component(profile = "name-qualified")
  public static class NamedConsumer {
    @Autowired
    @AutowiredQualifier(name = "blue")
    public NamedService dependency;
  }

  public interface RuntimeService {
  }

  public interface RuntimeAlias extends RuntimeService {
  }

  public static class RuntimeProduct implements RuntimeAlias {
  }

  @BeanFactory(profile = "runtime-qualified-ambiguous")
  public static class RuntimeFactory {
    @Bean("chosen")
    public RuntimeService first() {
      return new RuntimeProduct();
    }

    @Bean("chosen")
    public RuntimeAlias second() {
      return new RuntimeProduct();
    }
  }

  @Component(profile = "runtime-qualified-ambiguous")
  public static class RuntimeConsumer {
    @Autowired
    @AutowiredQualifier(name = "chosen", clazz = RuntimeProduct.class)
    public RuntimeService dependency;
  }

  @BeanFactory(profile = "duplicate-beans")
  public static class DuplicateFactory {
    @Bean("duplicate")
    public NamedService first() {
      return new NamedProduct();
    }

    @Bean("duplicate")
    public NamedService second() {
      return new NamedProduct();
    }
  }

  @ClientCommand(value = 84, profile = "duplicate-commands")
  public static class FirstClient extends AbstractClientCommandHandler<Player, DataCollection> {
    @Override
    public void execute(Player player, DataCollection message) {
    }
  }

  @ClientCommand(value = 84, profile = "duplicate-commands")
  public static class SecondClient extends AbstractClientCommandHandler<Player, DataCollection> {
    @Override
    public void execute(Player player, DataCollection message) {
    }
  }

  @SystemCommand(label = "diagnostics-status", profile = "duplicate-commands")
  public static class FirstSystem extends AbstractSystemCommandHandler {
    @Override
    public void execute(List<String> arguments) {
    }
  }

  @SystemCommand(label = "diagnostics-status", profile = "duplicate-commands")
  public static class SecondSystem extends AbstractSystemCommandHandler {
    @Override
    public void execute(List<String> arguments) {
    }
  }

  @RestController(value = "/diagnostics", profile = "duplicate-rest")
  public static class FirstController {
    @RestMapping("/same")
    public HttpServlet status() {
      return new HttpServlet() {};
    }
  }

  @RestController(value = "diagnostics/", profile = "duplicate-rest")
  public static class SecondController {
    @RestMapping("same/")
    public HttpServlet status() {
      return new HttpServlet() {};
    }
  }
}
