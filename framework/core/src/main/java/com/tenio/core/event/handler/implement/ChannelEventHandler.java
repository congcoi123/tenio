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

package com.tenio.core.event.handler.implement;

import com.tenio.common.data.DataCollection;
import com.tenio.common.utility.TimeUtility;
import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.entity.Channel;
import com.tenio.core.entity.Player;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.handler.event.EventChannelCreated;
import com.tenio.core.handler.event.EventChannelWillBeRemoved;
import com.tenio.core.handler.event.EventBroadcastToChannel;
import com.tenio.core.handler.event.EventPlayerSubscribedChannel;
import com.tenio.core.handler.event.EventPlayerUnsubscribedChannel;
import java.util.Optional;

/**
 * Dispatching all events related to channels.
 */
@Component
public final class ChannelEventHandler {

  @AutowiredAcceptNull
  private EventChannelCreated eventChannelCreated;

  @AutowiredAcceptNull
  private EventChannelWillBeRemoved eventChannelWillBeRemoved;

  @AutowiredAcceptNull
  private EventPlayerSubscribedChannel<Player> eventPlayerSubscribedChannel;

  @AutowiredAcceptNull
  private EventPlayerUnsubscribedChannel<Player> eventPlayerUnsubscribedChannel;

  @AutowiredAcceptNull
  private EventBroadcastToChannel<Player, DataCollection> eventBroadcastToChannel;

  /**
   * Initialization.
   *
   * @param eventManager the event manager
   */
  public void initialize(EventManager eventManager) {

    final var eventChannelCreatedOp = Optional.ofNullable(eventChannelCreated);
    final var eventChannelWillBeRemovedOp = Optional.ofNullable(eventChannelWillBeRemoved);

    final var eventPlayerSubscribedChannelOp = Optional.ofNullable(eventPlayerSubscribedChannel);
    final var eventPlayerUnsubscribedChannelOp = Optional.ofNullable(eventPlayerUnsubscribedChannel);

    final var eventBroadcastToChannelOp = Optional.ofNullable(eventBroadcastToChannel);

    eventChannelCreatedOp.ifPresent(
        event -> eventManager.on(ServerEvent.CHANNEL_CREATED, params -> {
          var channel = (Channel) params[0];

          event.handle(channel);

          return null;
        }));

    eventChannelWillBeRemovedOp.ifPresent(
        event -> eventManager.on(ServerEvent.CHANNEL_WILL_BE_REMOVED, params -> {
          var channel = (Channel) params[0];

          event.handle(channel);

          return null;
        }));

    eventPlayerSubscribedChannelOp.ifPresent(
        event -> eventManager.on(ServerEvent.PLAYER_SUBSCRIBED_CHANNEL, params -> {
          var channel = (Channel) params[0];
          var player = (Player) params[1];

          event.handle(channel, player);

          return null;
        }));

    eventPlayerUnsubscribedChannelOp.ifPresent(
        event -> eventManager.on(ServerEvent.PLAYER_UNSUBSCRIBED_CHANNEL, params -> {
          var channel = (Channel) params[0];
          var player = (Player) params[1];

          event.handle(channel, player);

          return null;
        }));

    eventBroadcastToChannelOp.ifPresent(
        event -> eventManager.on(ServerEvent.BROADCAST_TO_CHANNEL, params -> {
          var channel = (Channel) params[0];
          var player = (Player) params[1];
          var message = (DataCollection) params[2];
          player.setLastWriteTime(TimeUtility.currentTimeMillis());

          event.handle(channel, player, message);

          return null;
        }));
  }
}
