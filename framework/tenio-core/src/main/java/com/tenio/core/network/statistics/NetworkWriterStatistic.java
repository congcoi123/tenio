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

public final class NetworkWriterStatistic {

	private volatile long __writtenBytes;
	private volatile long __writtenPackets;
	private volatile long __writtenDroppedPacketsByPolicy;
	private volatile long __writtenDroppedPacketsByFull;

	public static NetworkWriterStatistic newInstance() {
		return new NetworkWriterStatistic();
	}

	private NetworkWriterStatistic() {
		__writtenBytes = 0L;
		__writtenPackets = 0L;
		__writtenDroppedPacketsByPolicy = 0L;
		__writtenDroppedPacketsByFull = 0L;
	}

	public void updateWrittenBytes(long numberBytes) {
		__writtenBytes += numberBytes;
	}

	public void updateWrittenPackets(long numberPackets) {
		__writtenPackets += numberPackets;
	}

	public void updateWrittenDroppedPacketsByPolicy(long numberPackets) {
		__writtenDroppedPacketsByPolicy += numberPackets;
	}

	public void updateWrittenDroppedPacketsByFull(long numberPackets) {
		__writtenDroppedPacketsByFull += numberPackets;
	}

	public long getWrittenBytes() {
		return __writtenBytes;
	}

	public long getWrittenPackets() {
		return __writtenPackets;
	}

	public long getWrittenDroppedPacketsByPolicy() {
		return __writtenDroppedPacketsByPolicy;
	}

	public long getWrittenDroppedPacketsByFull() {
		return __writtenDroppedPacketsByFull;
	}

	public long getWrittenDroppedPackets() {
		return __writtenDroppedPacketsByPolicy + __writtenDroppedPacketsByFull;
	}

}
