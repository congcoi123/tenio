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

package com.tenio.core.network.entity.packet;

import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.Session;
import java.util.Collection;

/**
 * The smallest unit to hold and transfer data from the server to clients.
 */
public interface Packet {

  long getId();

  byte[] getData();

  void setData(byte[] binary);

  TransportType getTransportType();

  void setTransportType(TransportType type);

  ResponsePriority getPriority();

  void setPriority(ResponsePriority priority);

  boolean isEncrypted();

  void setEncrypted(boolean encrypted);

  Collection<Session> getRecipients();

  void setRecipients(Collection<Session> recipients);

  long getCreatedTime();

  int getOriginalSize();

  boolean isTcp();

  boolean isUdp();

  boolean isWebSocket();

  byte[] getFragmentBuffer();

  void setFragmentBuffer(byte[] binary);

  boolean isFragmented();

  Packet clone();
}
