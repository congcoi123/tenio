/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.message.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.tenio.configuration.constant.Constants;

/**
 * When sending a message through the Internet, it's necessary to convert one
 * object data to its corresponding serialize data. Because TCP transfers data
 * by a stream, so it needs to determine the one package range by adding a value
 * called header length to each package's header.
 * 
 * @author kong
 * 
 */
public final class MessagePacker {

	/**
	 * Add the header-length value to the package's header
	 * 
	 * @param data your sending data in an array of bytes
	 * @return Returns a new array of bytes data with a header-length value at the
	 *         package's header
	 */
	public static byte[] pack(byte[] data) {
		// Make bytes
		byte[] length = shortToByte((short) data.length);
		// HEADER_BYTES bytes for hold data's length
		byte[] all = ByteBuffer.allocate(Constants.HEADER_BYTES + data.length).array();
		// Done by native codes = "exceptional fast"
		System.arraycopy(length, 0, all, 0, Constants.HEADER_BYTES);
		System.arraycopy(data, 0, all, Constants.HEADER_BYTES, data.length);

		return all;
	}

	/**
	 * Convert a value in short type to its corresponding array of bytes. This
	 * convert by <code>BIG_EDIAN</code> byte's order
	 * 
	 * @param value the value in short type
	 * @return Returns an array of bytes
	 */
	public static byte[] shortToByte(short value) {
		return ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(value).array();
	}

	/**
	 * Convert an array of bytes to its corresponding value in short type. This
	 * convert by <code>BIG_EDIAN</code> byte's order
	 * 
	 * @param value the array of bytes
	 * @return Returns a value in short type
	 */
	public static short byteToShort(byte[] value) {
		return ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN).getShort();
	}

}
