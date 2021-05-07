package com.tenio.core.network.entity.packet;

public final class PacketHeader {

	private final boolean __binary;
	private final boolean __compressed;
	private final boolean __bigSized;
	private final boolean __encrypted;

	public static PacketHeader newInstance(boolean binary, boolean compressed, boolean bigSized, boolean encrypted) {
		return new PacketHeader(binary, compressed, bigSized, encrypted);
	}

	private PacketHeader(boolean binary, boolean compressed, boolean bigSized, boolean encrypted) {
		__binary = binary;
		__compressed = compressed;
		__bigSized = bigSized;
		__encrypted = encrypted;
	}

	public boolean isBinary() {
		return __binary;
	}

	public boolean isCompressed() {
		return __compressed;
	}

	public boolean isBigSized() {
		return __bigSized;
	}

	public boolean isEncrypted() {
		return __encrypted;
	}

	@Override
	public String toString() {
		return String.format("{ Normal: %b, Compressed: %b, BigSized: %b, Encrypted: %b }", __binary, __compressed,
				__bigSized, __encrypted);
	}

}
