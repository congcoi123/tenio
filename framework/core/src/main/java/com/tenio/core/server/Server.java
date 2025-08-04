/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

package com.tenio.core.server;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.api.ServerApi;
import com.tenio.core.bootstrap.BootstrapHandler;
import com.tenio.core.bootstrap.Bootstrapper;
import com.tenio.core.command.client.ClientCommandManager;
import com.tenio.core.entity.manager.ChannelManager;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.entity.manager.RoomManager;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.network.entity.protocol.Response;
import com.tenio.core.network.zero.engine.manager.DatagramChannelManager;
import io.netty.bootstrap.Bootstrap;

/**
 * This class manages the workflow of the current server. The instruction's orders are important,
 * event subscribes must be set last and all configuration values should be confirmed.
 */
public interface Server {

  /**
   * Starts the server bases on the configurations.
   *
   * @param bootstrapHandler a {@link BootstrapHandler} handles all processes concerning the DI
   *                         mechanism
   * @param params           a list of {@link String} arguments used by the boostrap
   * @throws Exception whenever any issue emerged
   * @see Bootstrap
   * @see Bootstrapper
   */
  void start(BootstrapHandler bootstrapHandler, String[] params) throws Exception;

  /**
   * Shuts down the server and closes all services.
   */
  void shutdown();

  /**
   * Retrieves a server APIs object which provides all supporting APIs on the server.
   *
   * @return an instance of {@link ServerApi}
   */
  ServerApi getApi();

  /**
   * Retrieves a management object of self-defined user commands.
   *
   * @return an instance of {@link ClientCommandManager}
   * @since 0.5.0
   */
  ClientCommandManager getClientCommandManager();

  /**
   * Retrieves a event manager object which manages all events supporting on the server.
   *
   * @return an instance of {@link EventManager}
   */
  EventManager getEventManager();

  /**
   * Retrieves a player manager object which manages all players on the server.
   *
   * @return an instance of {@link PlayerManager}
   */
  PlayerManager getPlayerManager();

  /**
   * Retrieves a room manager object which manages all rooms on the server.
   *
   * @return an instance of {@link RoomManager}
   */
  RoomManager getRoomManager();

  /**
   * Retrieves a channel manager object which manages channels.
   *
   * @since 0.6.3
   * @return an instance of {@link ChannelManager}
   */
  ChannelManager getChannelManager();

  /**
   * Retrieves a data channel manager object which allows managing Udp, Kcp related information.
   *
   * @return an instance of {@link DatagramChannelManager}
   */
  DatagramChannelManager getDatagramChannelManager();

  /**
   * Retrieves the current server's configuration.
   *
   * @return the current {@link Configuration} of server
   * @since 0.4.0
   */
  Configuration getConfiguration();

  /**
   * Retrieves the time when server starts in milliseconds.
   *
   * @return started time in milliseconds
   */
  long getStartedTime();

  /**
   * Retrieves the current uptime of server in milliseconds.
   *
   * @return the current server's uptime in milliseconds
   */
  long getUptime();

  /**
   * Writes down data to socket/channel to send them to client sides.
   *
   * @param response an instance of {@link Response} using to carry conveying information
   * @param markedAsLast marks as this writing is the last one
   */
  void write(Response response, boolean markedAsLast);
}
