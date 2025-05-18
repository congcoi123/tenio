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

package com.tenio.core.controller;

import com.tenio.core.exception.RequestQueueFullException;
import com.tenio.core.network.entity.protocol.Request;
import com.tenio.core.service.Service;
import com.tenio.core.service.ServiceListener;

/**
 * Represents a controller that manages request processing in the application.
 * This interface extends both {@link Service} and {@link ServiceListener} to provide
 * request handling capabilities with service lifecycle management.
 *
 * <p>Key features:
 * <ul>
 *   <li>Request queue management with configurable size limits</li>
 *   <li>Thread pool management for request processing</li>
 *   <li>Service lifecycle integration</li>
 *   <li>Request processing monitoring and metrics</li>
 *   <li>Priority-based request handling</li>
 *   <li>Request validation and error handling</li>
 *   <li>Performance monitoring capabilities</li>
 * </ul>
 *
 * <p>Thread safety: Implementations of this interface should be thread-safe
 * as they handle concurrent request processing. The interface provides atomic
 * operations for request queue management and thread pool control.
 *
 * <p>Note: This interface is designed to work in conjunction with the
 * {@link Service} and {@link ServiceListener} interfaces for proper lifecycle
 * management and event handling.
 *
 * @see Service
 * @see ServiceListener
 * @see Request
 * @see RequestQueueFullException
 * @since 0.3.0
 */
public interface Controller extends Service, ServiceListener {

  /**
   * Enqueue a request from a request queue for processing.
   *
   * @param request the processing {@link Request}
   * @throws RequestQueueFullException when the request queue is full
   */
  void enqueueRequest(Request request) throws RequestQueueFullException;

  /**
   * Retrieves the maximum size of a request queue.
   *
   * @return the maximum request queue size ({@code integer} value)
   */
  int getMaxRequestQueueSize();

  /**
   * Sets the maximum value size for the request queue.
   *
   * @param maxSize the maximum size of a request queue ({@code integer} value)
   */
  void setMaxRequestQueueSize(int maxSize);

  /**
   * Retrieves the current percentage using of a request queue.
   *
   * @return the percentage of using ({@code float} value)
   */
  float getPercentageUsedRequestQueue();

  /**
   * Retrieves the thread pool size using for processes.
   *
   * @return the thread pool size ({@code integer} value)
   */
  int getThreadPoolSize();

  /**
   * Set the thread pool size using for processes.
   *
   * @param maxSize the thread pool size ({@code integer} value)
   */
  void setThreadPoolSize(int maxSize);
}
