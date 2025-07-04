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

package com.tenio.examples.example4.statistic;

import java.util.ArrayList;
import java.util.List;

public final class NetworkStatistic {

  private final List<Long> latencyRecorder;
  private final List<Integer> fpsRecorder;
  private final List<Double> lostPacketRecorder;

  private NetworkStatistic() {
    latencyRecorder = new ArrayList<>();
    fpsRecorder = new ArrayList<>();
    lostPacketRecorder = new ArrayList<>();
  }

  public static NetworkStatistic newInstance() {
    return new NetworkStatistic();
  }

  public double getLatencyAverage() {
    synchronized (latencyRecorder) {
      long average = 0;
      int size = latencyRecorder.size();
      for (Long aLong : latencyRecorder) {
        average += aLong;
      }

      return (double) average / (double) size;
    }
  }

  public int getLatencySize() {
    synchronized (latencyRecorder) {
      return latencyRecorder.size();
    }
  }

  public void addLatency(long latency) {
    synchronized (latencyRecorder) {
      latencyRecorder.add(latency);
    }
  }

  public double getFpsAverage() {
    synchronized (fpsRecorder) {
      int average = 0;
      int size = fpsRecorder.size();
      for (Integer integer : fpsRecorder) {
        average += integer;
      }

      return (double) average / (double) size;
    }
  }

  public int getFpsSize() {
    synchronized (fpsRecorder) {
      return fpsRecorder.size();
    }
  }

  public void addFps(int fps) {
    synchronized (fpsRecorder) {
      fpsRecorder.add(fps);
    }
  }

  public double getLostPacketsAverage() {
    synchronized (lostPacketRecorder) {
      double average = 0;
      int size = lostPacketRecorder.size();
      for (Double aDouble : lostPacketRecorder) {
        average += aDouble;
      }

      return average / (double) size;
    }
  }

  public int getLostPacketsSize() {
    synchronized (lostPacketRecorder) {
      return lostPacketRecorder.size();
    }
  }

  public void addLostPackets(double packets) {
    synchronized (lostPacketRecorder) {
      lostPacketRecorder.add(packets);
    }
  }
}
