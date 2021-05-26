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
		__writtenBytes = 0;
		__writtenPackets = 0;
		__writtenDroppedPacketsByPolicy = 0;
		__writtenDroppedPacketsByFull = 0;
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
