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
package com.tenio.core.network.zero.handler;

import java.nio.channels.SocketChannel;

/**
 * UNDER CONSTRUCTION
 * 
 * @author kong
 */
public interface IOHandler {

	void channelActive(SocketChannel socketChannel);

	void channelRead(SocketChannel channel, byte[] binaryData);
	
	void channelWrite(SocketChannel channel, byte[] binaryData);
	
	void channelInactive(SocketChannel socketChannel);
	
    void onDataRead(Session var1, byte[] var2);

    void onDataRead(DatagramChannel var1, SocketAddress var2, byte[] var3);

    void onDataWrite(Packet var1);

    IProtocolCodec getCodec();

    void setCodec(IProtocolCodec var1);

    long getReadPackets();

    long getIncomingDroppedPackets();

}
