/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

/**
 * Lifetime aggregates use constant space and return zero before the first sample.
 */
public final class NetworkStatistic {

    private double latencySum;
    private double fpsSum;
    private double lossSum;
    private int latencyCount;
    private int fpsCount;
    private int lossCount;

    private NetworkStatistic() {
    }

    public static NetworkStatistic newInstance() {
        return new NetworkStatistic();
    }

    public synchronized void addLatency(long value) {
        latencySum += value;
        latencyCount++;
    }

    public synchronized void addFps(int value) {
        fpsSum += value;
        fpsCount++;
    }

    public synchronized void addLostPackets(double value) {
        lossSum += value;
        lossCount++;
    }

    public synchronized int getLatencySize() {
        return latencyCount;
    }

    public synchronized int getFpsSize() {
        return fpsCount;
    }

    public synchronized int getLostPacketsSize() {
        return lossCount;
    }

    public synchronized double getLatencyAverage() {
        return latencyCount == 0 ? 0 : latencySum / latencyCount;
    }

    public synchronized double getFpsAverage() {
        return fpsCount == 0 ? 0 : fpsSum / fpsCount;
    }

    public synchronized double getLostPacketsAverage() {
        return lossCount == 0 ? 0 : lossSum / lossCount;
    }
}
