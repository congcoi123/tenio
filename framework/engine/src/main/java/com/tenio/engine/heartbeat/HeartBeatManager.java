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

package com.tenio.engine.heartbeat;

import com.tenio.engine.message.ExtraMessage;

/**
 * The Java ExecutorService is a construct that allows you to pass a task to be
 * executed by a thread asynchronously. The executor service creates and
 * maintains a reusable pool of threads for executing submitted tasks. This
 * class helps you create and manage your HeartBeats. See:
 * {@link AbstractHeartBeat}
 */
public interface HeartBeatManager {

  /**
   * The number of maximum heart-beats that the server can handle.
   *
   * @param maxHeartbeat the number of maximum heart-beats that the server can
   *                     handle
   * @throws Exception an exception
   */
  void initialize(final int maxHeartbeat) throws Exception;

  /**
   * Create a new heart-beat.
   *
   * @param id        the unique id
   * @param heartbeat see {@link AbstractHeartBeat}
   */
  void create(final String id, final AbstractHeartBeat heartbeat);

  /**
   * Dispose a heart-beat.
   *
   * @param id the unique id
   */
  void dispose(final String id);

  /**
   * Check if a heart-beat is existed or not.
   *
   * @param id the unique id
   * @return <b>true</b> if the corresponding heart-beat has existed
   */
  boolean contains(final String id);

  /**
   * Destroy all heart-beats and clear all references.
   */
  void clear();

  /**
   * Send a message to a particular heart-beat with a delay time.
   *
   * @param id        the unique id
   * @param message   the message content, see {@link ExtraMessage}
   * @param delayTime the delay time in seconds
   */
  void sendMessage(final String id, final ExtraMessage message, final double delayTime);

  /**
   * Send a message to a particular heart-beat with no delay time.
   *
   * @param id      the unique id
   * @param message the message content, see {@link ExtraMessage}
   */
  void sendMessage(final String id, final ExtraMessage message);
}
