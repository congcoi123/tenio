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

package com.tenio.core.network.entity.protocol;

import com.tenio.core.configuration.define.ServerEvent;
import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.entity.session.Session;

/**
 * The request which server formed when it had received a message from a client.
 */
public interface Request {

  long getId();

  Object getAttribute(String key);

  Request setAttribute(String key, Object value);

  ServerEvent getEvent();

  Request setEvent(ServerEvent event);

  Session getSender();

  Request setSender(Session session);

  RequestPriority getPriority();

  Request setPriority(RequestPriority priority);

  long getTimestamp();
}
