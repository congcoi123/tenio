package com.tenio.core.network.statistics;

public final class NetworkWriterStatistic {

	public static NetworkWriterStatistic newInstance() {
		return new NetworkWriterStatistic();
	}

	private NetworkWriterStatistic() {

	}

	public void updateWrittenBytes(long numberBytes) {

	}

	public void updateWrittenPackets(long numberPackets) {

	}

	public void updateDroppedPacketsByPolicy(long numberPackets) {

	}

	public void updateDroppedPacketsByFull(long numberPackets) {

	}

}
