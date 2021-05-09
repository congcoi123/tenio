package com.tenio.common.utility;

public final class ByteUtility {

	public static byte[] intToBytes(int value) {
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	public static int bytesToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8)
				| ((bytes[3] & 0xFF) << 0);
	}

	public static byte[] shortToBytes(short value) {
		return new byte[] { (byte) (value >> 8), (byte) value };
	}

	public static short bytesToShort(byte[] bytes) {
		return (short) (((bytes[0] & 0xFF) << 8) | ((bytes[1] & 0xFF) << 0));
	}

	public static byte[] resizeBytesArray(byte[] source, int position, int size) {
		byte[] binary = new byte[size];
		System.arraycopy(source, position, binary, 0, size);
		return binary;
	}

}
