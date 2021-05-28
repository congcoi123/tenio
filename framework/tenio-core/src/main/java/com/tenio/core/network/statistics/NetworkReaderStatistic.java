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
package com.tenio.core.network.statistics;

public final class NetworkReaderStatistic {

	private volatile long __readBytes;
	private volatile long __readPackets;
	private volatile long __readDroppedPackets;

	public static NetworkReaderStatistic newInstannce() {
		return new NetworkReaderStatistic();
	}

	private NetworkReaderStatistic() {
		__readBytes = 0L;
		__readPackets = 0L;
		__readDroppedPackets = 0L;
	}

	public void updateReadBytes(long numberBytes) {
		__readBytes += numberBytes;
	}

	public void updateReadPackets(long numberPackets) {
		__readPackets += numberPackets;
	}

	public void updateReadDroppedPackets(long numberPackets) {
		__readDroppedPackets += numberPackets;
	}

	public long getReadBytes() {
		return __readBytes;
	}

	public long getReadPackets() {
		return __readPackets;
	}

	public long getReadDroppedPackets() {
		return __readDroppedPackets;
	}

}
