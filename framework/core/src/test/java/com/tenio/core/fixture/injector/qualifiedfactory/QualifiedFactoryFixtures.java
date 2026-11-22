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

package com.tenio.core.fixture.injector.qualifiedfactory;

import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.AutowiredQualifier;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.Component;

/** Interface-returning factories must retain runtime class and name qualifier semantics. */
public final class QualifiedFactoryFixtures {

  public interface Service {
  }

  public interface AliasService extends Service {
  }

  public interface NullService {
  }

  public record ServiceImpl(String value) implements AliasService {
  }

  public record OtherService(String value) implements Service {
  }

  public static class NullServiceImpl implements NullService {
  }

  @BeanFactory
  public static class Factory {
    private final ServiceImpl shared = new ServiceImpl("default");

    @Bean
    public Service defaultService() {
      return shared;
    }

    @Bean("chosen")
    public Service namedService() {
      return new ServiceImpl("named");
    }

    @Bean("other")
    public Service otherService() {
      return new OtherService("other");
    }

    @Bean
    public NullService absentService() {
      return null;
    }

    @Bean(profile = "ambiguous")
    public AliasService equalButDistinctService() {
      return new ServiceImpl("default");
    }

    @Bean(profile = "same-instance")
    public AliasService aliasOfSameService() {
      return shared;
    }
  }

  @Component(profile = "matches")
  public static class MatchingConsumer {
    @Autowired
    @AutowiredQualifier(clazz = ServiceImpl.class)
    public Service defaultService;

    @Autowired
    @AutowiredQualifier(clazz = ServiceImpl.class, name = "chosen")
    public Service namedService;

    @AutowiredAcceptNull
    @AutowiredQualifier(clazz = OtherService.class)
    public Service wrongClass;

    @AutowiredAcceptNull
    @AutowiredQualifier(clazz = NullServiceImpl.class)
    public NullService absentService;

    @AutowiredAcceptNull
    @AutowiredQualifier(clazz = ServiceImpl.class)
    public NullService incompatibleContract;
  }

  @Component(profile = "wrong-class")
  public static class WrongClassConsumer {
    @Autowired
    @AutowiredQualifier(clazz = OtherService.class)
    public Service service;
  }

  @Component(profile = "wrong-name")
  public static class WrongNameConsumer {
    @Autowired
    @AutowiredQualifier(clazz = ServiceImpl.class, name = "other")
    public Service service;
  }

  @Component(profile = "missing-name")
  public static class MissingNameConsumer {
    @Autowired
    @AutowiredQualifier(clazz = ServiceImpl.class, name = "missing")
    public Service service;
  }

  @Component(profile = "required-null")
  public static class RequiredNullConsumer {
    @Autowired
    @AutowiredQualifier(clazz = NullServiceImpl.class)
    public NullService service;
  }

  @Component(profile = {"ambiguous", "same-instance"})
  public static class AliasedConsumer {
    @Autowired
    @AutowiredQualifier(clazz = ServiceImpl.class)
    public Service service;
  }

  private QualifiedFactoryFixtures() {
  }
}
