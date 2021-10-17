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

package com.tenio.core.controller;

import com.tenio.core.exception.RequestQueueFullException;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.service.Service;
import com.tenio.core.service.ServiceListener;

/**
 * The supported APIs for a controller.
 */
public interface Controller extends Service, ServiceListener {

  /**
   * Enqueue a request for processing.
   *
   * @param request the request
   * @throws RequestQueueFullException when the queue is full
   */
  void enqueueRequest(Request request) throws RequestQueueFullException;

  /**
   * Retrieves the max request size.
   *
   * @return the max request size
   */
  int getMaxRequestQueueSize();

  /**
   * Set the maximum value size for the queue.
   *
   * @param maxSize the max size
   */
  void setMaxRequestQueueSize(int maxSize);

  /**
   * Retrieves the current percentage using of request queue.
   *
   * @return the percentage
   */
  float getPercentageUsedRequestQueue();

  /**
   * Retrieves the thread pool size.
   *
   * @return the thread pool size
   */
  int getThreadPoolSize();

  /**
   * Set the thread pool size.
   *
   * @param maxSize the max size
   */
  void setThreadPoolSize(int maxSize);
}
