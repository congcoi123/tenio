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
package com.tenio.core.network.zero.codec.packet;

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
