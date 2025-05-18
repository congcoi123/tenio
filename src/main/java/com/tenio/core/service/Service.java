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

package com.tenio.core.service;

import com.tenio.core.exception.ServiceRuntimeException;

/**
 * All supported APIs that is necessary to form a service.
 */
public interface Service {

  /**
   * Initializes a new service.
   *
   * @throws ServiceRuntimeException when something went wrong
   */
  void initialize() throws ServiceRuntimeException;

  /**
   * Start the service.
   *
   * @throws ServiceRuntimeException when something went wrong
   */
  void start() throws ServiceRuntimeException;

  /**
   * Shutdown the service.
   */
  void shutdown();

  /**
   * Determines whether the service is activated.
   *
   * @return {@code true} if the service is activated, {@code false} otherwise
   */
  boolean isActivated();

  /**
   * Retrieves the service's name.
   *
   * @return the {@link String} service's name
   */
  String getName();

  /**
   * Set the service's name.
   *
   * @param name the {@link String} service's name
   */
  void setName(String name);
}
