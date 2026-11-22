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

package com.tenio.core.bootstrap.profiles.components;

import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.bootstrap.annotation.EventHandler;
import com.tenio.core.bootstrap.annotation.Setting;

/** Independent discovery fixtures: inactive constructors must have no side effects. */
public final class ComponentFixtures {

  public static int worldConstructions;
  public static int gameConstructions;
  public static int settingConstructions;
  public static int eventConstructions;

  public interface Service {}

  @Component
  public static class SharedComponent {}

  @Component(profile = "world")
  public static class WorldService implements Service {
    public WorldService() {
      worldConstructions++;
    }
  }

  @Component(profile = "game")
  public static class GameService implements Service {
    public GameService() {
      gameConstructions++;
    }
  }

  @Setting(profile = "world")
  public static class WorldSetting {
    public WorldSetting() {
      settingConstructions++;
    }
  }

  @EventHandler(profile = "game")
  public static class GameEventHandler {
    public GameEventHandler() {
      eventConstructions++;
    }
  }

  @EventHandler(profile = "world")
  @Component(profile = "game")
  public static class CombinedEventHandler {}

  private ComponentFixtures() {}
}
