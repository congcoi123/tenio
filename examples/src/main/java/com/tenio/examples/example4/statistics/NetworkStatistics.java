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
package com.tenio.examples.example4.statistics;

import java.util.ArrayList;
import java.util.List;

public final class NetworkStatistics {

	private final List<Long> __latencyRecorder;
	private final List<Integer> __fpsRecorder;
	private final List<Double> __lostPacketRecorder;

	public static NetworkStatistics newInstance() {
		return new NetworkStatistics();
	}

	private NetworkStatistics() {
		__latencyRecorder = new ArrayList<Long>();
		__fpsRecorder = new ArrayList<Integer>();
		__lostPacketRecorder = new ArrayList<Double>();
	}

	public double getLatencyAverage() {
		synchronized (__latencyRecorder) {
			long average = 0;
			int size = __latencyRecorder.size();
			for (int i = 0; i < size; i++) {
				average += __latencyRecorder.get(i).longValue();
			}
			double result = (double) average / (double) size;

			return result;
		}
	}

	public int getLatencySize() {
		synchronized (__latencyRecorder) {
			return __latencyRecorder.size();
		}
	}

	public void addLatency(long latency) {
		synchronized (__latencyRecorder) {
			__latencyRecorder.add(latency);
		}
	}

	public double getFpsAverage() {
		synchronized (__fpsRecorder) {
			int average = 0;
			int size = __fpsRecorder.size();
			for (int i = 0; i < size; i++) {
				average += __fpsRecorder.get(i).intValue();
			}
			double result = (double) average / (double) size;

			return result;
		}
	}

	public int getFpsSize() {
		synchronized (__fpsRecorder) {
			return __fpsRecorder.size();
		}
	}

	public void addFps(int fps) {
		synchronized (__fpsRecorder) {
			__fpsRecorder.add(fps);
		}
	}

	public double getLostPacketsAverage() {
		synchronized (__lostPacketRecorder) {
			double average = 0;
			int size = __lostPacketRecorder.size();
			for (int i = 0; i < size; i++) {
				average += __lostPacketRecorder.get(i).doubleValue();
			}
			double result = (double) average / (double) size;

			return result;
		}
	}

	public int getLostPacketsSize() {
		synchronized (__lostPacketRecorder) {
			return __lostPacketRecorder.size();
		}
	}

	public void addLostPackets(double packets) {
		synchronized (__lostPacketRecorder) {
			__lostPacketRecorder.add(packets);
		}
	}

}
