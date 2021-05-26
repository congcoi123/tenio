package com.tenio.core.network.statistics;

public final class NetworkReaderStatistic {

	private volatile long __readBytes;
	private volatile long __readPackets;
	private volatile long __readDroppedPackets;

	public static NetworkReaderStatistic newInstannce() {
		return new NetworkReaderStatistic();
	}

	private NetworkReaderStatistic() {
		__readBytes = 0;
		__readPackets = 0;
		__readDroppedPackets = 0;
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
