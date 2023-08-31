/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.core.server.service;

import com.tenio.common.data.DataType;
import com.tenio.core.controller.Controller;
import com.tenio.core.entity.manager.PlayerManager;
import com.tenio.core.network.entity.session.manager.SessionManager;
import com.tenio.core.network.statistic.NetworkReaderStatistic;
import com.tenio.core.network.statistic.NetworkWriterStatistic;

/**
 * The internal processor service, the heart of the server.
 */
public interface InternalProcessorService extends Controller {

  /**
   * Subscribes all events on the server.
   */
  void subscribe();

  /**
   * Set the data serialization type.
   *
   * @param dataType the {@link DataType} value
   */
  void setDataType(DataType dataType);

  /**
   * Sets the maximum number of players allowed participating on the server.
   *
   * @param maxPlayers {@code integer} value, the maximum number of players allowed
   *                   participating on the server
   */
  void setMaxNumberPlayers(int maxPlayers);

  /**
   * Determines if a player could be kept its connection when it is disconnected from the server
   * for a while.
   *
   * @param keepPlayerOnDisconnection sets to {@code true} if a player could be kept its
   *                                  connection when it is disconnected from the server for a
   *                                  while, otherwise returns {@code false}
   */
  void setKeepPlayerOnDisconnection(boolean keepPlayerOnDisconnection);

  /**
   * Determines if UDP channels can be in use for communication.
   *
   * @param enabledUdp sets it {@code true} if enabled, otherwise sets it {code false}
   */
  void setEnabledUdp(boolean enabledUdp);

  /**
   * Determines if UDP channels can use KCP transportation for communication.
   *
   * @param enabledKcp sets it {@code true} if enabled, otherwise sets it {code false}
   */
  void setEnabledKcp(boolean enabledKcp);

  /**
   * Sets a session manager instance.
   *
   * @param sessionManager a {@link SessionManager} instance
   */
  void setSessionManager(SessionManager sessionManager);

  /**
   * Sets a player manager for the server which is used to manage all players.
   *
   * @param playerManager an instance of {@link PlayerManager}
   */
  void setPlayerManager(PlayerManager playerManager);

  /**
   * Sets a network reader statistic instance which takes responsibility recording the
   * receiving data from clients.
   *
   * @param networkReaderStatistic a {@link NetworkReaderStatistic} instance
   */
  void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic);

  /**
   * Sets a network writer statistic instance which takes responsibility recording the
   * sending data from the network.
   *
   * @param networkWriterStatistic a {@link NetworkWriterStatistic} instance
   */
  void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic);
}
