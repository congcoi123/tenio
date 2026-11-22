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

package com.tenio.core.fixture.bootstrap;

import com.tenio.common.data.DataCollection;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.bootstrap.annotation.Autowired;
import com.tenio.core.bootstrap.annotation.Bean;
import com.tenio.core.bootstrap.annotation.BeanFactory;
import com.tenio.core.bootstrap.annotation.ClientCommand;
import com.tenio.core.bootstrap.annotation.RestController;
import com.tenio.core.bootstrap.annotation.RestMapping;
import com.tenio.core.bootstrap.annotation.SystemCommand;
import com.tenio.core.bootstrap.configuration.ConfigurationHandler;
import com.tenio.core.command.client.AbstractClientCommandHandler;
import com.tenio.core.command.system.AbstractSystemCommandHandler;
import com.tenio.core.entity.Player;
import com.tenio.core.event.handler.EventHandler;
import jakarta.servlet.http.HttpServlet;
import java.util.List;

/** Supplies framework beans and observable registrations for an isolated bootstrap context. */
@BeanFactory
public class BootstrapBeans {

  public static final short CLIENT_COMMAND_CODE = 321;
  public static final String SYSTEM_COMMAND_LABEL = "bootstrap-status";
  public static final String CONTROLLER_PATH = "/bootstrap";
  public static final String SERVLET_PATH = "bootstrap/status";

  @Bean
  public BootstrapHandler bootstrapHandler() {
    return new BootstrapHandler();
  }

  @Bean
  public ConfigurationHandler configurationHandler() {
    return new ConfigurationHandler();
  }

  @ClientCommand(CLIENT_COMMAND_CODE)
  public static class BootstrapClientCommand
      extends AbstractClientCommandHandler<Player, DataCollection> {
    @Autowired public EventHandler eventHandler;

    @Override
    public void execute(Player player, DataCollection message) {
    }
  }

  @SystemCommand(label = SYSTEM_COMMAND_LABEL)
  public static class BootstrapSystemCommand extends AbstractSystemCommandHandler {
    @Autowired public EventHandler eventHandler;

    @Override
    public void execute(List<String> arguments) {
    }
  }

  @RestController(CONTROLLER_PATH)
  public static class BootstrapRestController {
    @Autowired public EventHandler eventHandler;
    private final HttpServlet servlet = new HttpServlet() {};

    @RestMapping("/status")
    public HttpServlet status() {
      return servlet;
    }
  }
}
