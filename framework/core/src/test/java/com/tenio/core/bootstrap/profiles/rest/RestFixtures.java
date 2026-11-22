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

package com.tenio.core.bootstrap.profiles.rest;

import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import jakarta.servlet.http.HttpServlet;

/** HTTP mapping methods are inspected without starting an HTTP listener. */
public final class RestFixtures {

  public static int worldConstructions;
  public static int gameConstructions;
  public static int sharedInvocations;
  public static int combinedInvocations;

  public static class StatusServlet extends HttpServlet {}

  @RestController(value = "/api", profile = "world")
  public static class WorldController {
    public WorldController() {
      worldConstructions++;
    }

    @RestMapping("/status")
    public HttpServlet status() {
      sharedInvocations++;
      return new StatusServlet();
    }

    @RestMapping(value = "/combined", profile = "game")
    public HttpServlet combined() {
      combinedInvocations++;
      return new StatusServlet();
    }

    @RestMapping(value = "/invalid", profile = "never")
    public String inactiveInvalidMapping() {
      throw new AssertionError("An inactive mapping must never be invoked");
    }
  }

  @RestController(value = "/game", profile = "game")
  public static class GameController {
    public GameController() {
      gameConstructions++;
    }

    @RestMapping("/status")
    public HttpServlet status() {
      return new StatusServlet();
    }
  }

  private RestFixtures() {}
}
