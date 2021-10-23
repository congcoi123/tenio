/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

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

package com.tenio.core.bootstrap.event.handlers;

import com.tenio.common.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.common.bootstrap.annotation.Component;
import com.tenio.common.configuration.Configuration;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.event.Subscriber;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.extension.events.EventFetchedBandwidthInfo;
import com.tenio.core.extension.events.EventFetchedCcuInfo;
import com.tenio.core.extension.events.EventServerException;
import com.tenio.core.extension.events.EventServerInitialization;
import com.tenio.core.extension.events.EventServerTeardown;
import com.tenio.core.extension.events.EventSystemMonitoring;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Dispatching all events related to mixins.
 */
@Component
public final class MixinsEventHandler {

  @AutowiredAcceptNull
  private EventServerInitialization eventServerInitialization;

  @AutowiredAcceptNull
  private EventServerException eventServerException;

  @AutowiredAcceptNull
  private EventServerTeardown eventServerTeardown;

  @AutowiredAcceptNull
  private EventFetchedBandwidthInfo eventFetchedBandwidthInfo;

  @AutowiredAcceptNull
  private EventFetchedCcuInfo eventFetchedCcuInfo;

  @AutowiredAcceptNull
  private EventSystemMonitoring eventSystemMonitoring;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    final var eventServerInitializationOp =
        Optional.ofNullable(eventServerInitialization);
    final var eventServerExceptionOp =
        Optional.ofNullable(eventServerException);
    final var eventServerTeardownOp =
        Optional.ofNullable(eventServerTeardown);

    final var eventFetchedBandwidthInfoOp =
        Optional.ofNullable(eventFetchedBandwidthInfo);
    final var eventFetchedCcuInfoOp =
        Optional.ofNullable(eventFetchedCcuInfo);
    final var eventSystemMonitoringOp =
        Optional.ofNullable(eventSystemMonitoring);

    eventServerInitializationOp.ifPresent(new Consumer<EventServerInitialization>() {

      @Override
      public void accept(EventServerInitialization event) {
        eventManager.on(ServerEvent.SERVER_INITIALIZATION, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            var serverName = (String) params[0];
            var configuration = (Configuration) params[1];

            event.handle(serverName, configuration);

            return null;
          }
        });
      }
    });

    eventServerExceptionOp.ifPresent(new Consumer<EventServerException>() {

      @Override
      public void accept(EventServerException event) {
        eventManager.on(ServerEvent.SERVER_EXCEPTION, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            var throwable = (Throwable) params[0];

            event.handle(throwable);

            return null;
          }
        });
      }
    });

    eventServerTeardownOp.ifPresent(new Consumer<EventServerTeardown>() {

      @Override
      public void accept(EventServerTeardown event) {
        eventManager.on(ServerEvent.SERVER_TEARDOWN, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            var serverName = (String) params[0];

            event.handle(serverName);

            return null;
          }
        });
      }
    });

    eventFetchedBandwidthInfoOp.ifPresent(new Consumer<EventFetchedBandwidthInfo>() {

      @Override
      public void accept(EventFetchedBandwidthInfo event) {
        eventManager.on(ServerEvent.FETCHED_BANDWIDTH_INFO, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            long readBytes = (long) params[0];
            long readPackets = (long) params[1];
            long readDroppedPackets = (long) params[2];
            long writtenBytes = (long) params[3];
            long writtenPackets = (long) params[4];
            long writtenDroppedPacketsByPolicy = (long) params[5];
            long writtenDroppedPacketsByFull = (long) params[6];

            event.handle(readBytes, readPackets, readDroppedPackets, writtenBytes, writtenPackets,
                writtenDroppedPacketsByPolicy, writtenDroppedPacketsByFull);

            return null;
          }
        });
      }
    });

    eventFetchedCcuInfoOp.ifPresent(new Consumer<EventFetchedCcuInfo>() {

      @Override
      public void accept(EventFetchedCcuInfo event) {
        eventManager.on(ServerEvent.FETCHED_CCU_INFO, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            int numberPlayers = (int) params[0];

            event.handle(numberPlayers);

            return null;
          }
        });
      }
    });

    eventSystemMonitoringOp.ifPresent(new Consumer<EventSystemMonitoring>() {

      @Override
      public void accept(EventSystemMonitoring event) {
        eventManager.on(ServerEvent.SYSTEM_MONITORING, new Subscriber() {

          @Override
          public Object dispatch(Object... params) {
            double cpuUsage = (double) params[0];
            long totalMemory = (long) params[1];
            long usedMemory = (long) params[2];
            long freeMemory = (long) params[3];
            int countRunningThreads = (int) params[4];

            event.handle(cpuUsage, totalMemory, usedMemory, freeMemory, countRunningThreads);

            return null;
          }
        });
      }
    });
  }
}
