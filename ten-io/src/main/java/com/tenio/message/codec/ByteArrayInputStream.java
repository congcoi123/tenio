/**   
 * Copyright 2011 The Buzz Media, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tenio.message.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Class used to implement a very efficient and re-usable (see {@link #reset()},
 * {@link #reset(byte[])} and {@link #reset(byte[], int, int)})
 * {@link InputStream} that reads from an underlying, caller-provided
 * <code>byte[]</code> (also accessible via {@link #getArray()} if needed). This
 * class is a compliment to {@link ByteArrayOutputStream}.
 * <p/>
 * This class is meant to be a fast bridge between the stream-based Universal
 * Binary JSON I/O classes and a simple <code>byte[]</code> which can be helpful
 * when working with non-stream-based I/O, like Java NIO.
 * <p/>
 * For example, you can use {@link ByteBuffer#array()},
 * {@link ByteBuffer#position()} and {@link ByteBuffer#limit()} after a
 * {@link ByteBuffer#flip()} to set the source <code>byte[]</code>, offset and
 * length (respectively) to read the raw contents of a <code>ByteBuffer</code>
 * without incurring the cost of an array copy to get the bytes out of it.
 * <h3>Performance</h3> The JDK already provides a
 * {@link java.io.ByteArrayInputStream} implementation, unfortunately it is not
 * re-usable, requiring a new input stream to be created to wrap any input
 * <code>byte[]</code> every single time. By allowing this class to be re-usable
 * (see {@link #reset(byte[], int, int)}) this class can act as a simple, thin
 * and reusable wrapper to any number of <code>byte[]</code> that need to be
 * read in.
 * <p/>
 * Utilizing this class avoids the performance burden caused by unnecessary
 * object creation and GC cleanup which can be significant in a high performance
 * system.
 * <p/>
 * <strong>TIP</strong>: In a very high-performance system where data is being
 * read in as <code>byte[]</code>, you can keep a pool around of streams
 * wrapping this stream type (e.g. {@link UBJInputStream} wrapping
 * {@link ByteArrayInputStream}) and re-use them over and over again without
 * ever needing to create new instances or GC old ones. The performance benefits
 * in a high-frequency system can be significant; especially for long-running
 * applications.
 * <p/>
 * It is because of the re-usability nature of this stream type that allows any
 * wrapping streams to be implicitly (and safely) re-usable as well.
 * <h3>Reuse</h3> The different <code>reset</code> methods provide different
 * approaches to reusing an instance of this class. {@link #reset()} simply
 * resets the current position pointer for the existing underlying
 * <code>byte[]</code> so it can be re-read again if so desired.
 * {@link #reset(byte[])} simply defers to {@link #reset(byte[], int, int)}
 * which replaces the underlying <code>byte[]</code> as well as the current
 * position offset of the stream and the total length of bytes available in the
 * newly set <code>byte[]</code>.
 * <p/>
 * <strong>REMINDER</strong>: {@link #close()} is a no-op operations. You don't
 * need to worry about not being able to re-use instances of this class because
 * a wrapping stream type issued a close operation on it; the state of streams
 * of this type are not effected by that operation.
 * <h3>Usage</h3> This class is designed such that you create an instance of
 * this class, then wrap it with a {@link UBJInputStream} and read any amount of
 * Universal Binary JSON from the underlying <code>byte[]</code> stream.
 * <p/>
 * When that operation is complete and a new set of UBJ bytes are ready to be
 * read, simply call {@link #reset(byte[])} or {@link #reset(byte[], int, int)}
 * to setup the new underlying data to be read in (or {@link #reset()} if you
 * wish to re-read the same contents that were read the first time). At any time
 * you can determine how much data is left to read from the underlying
 * <code>byte[]</code> by calling {@link #available()}.
 * <p/>
 * Usage would look something like this:
 * 
 * <pre>
 * <code>
 * // Get data from somewhere (file, network, etc.)
 * byte[] data = ...
 * 
 * // Create streams individually so we have access to bais.
 * {@link ByteArrayInputStream} bais = new {@link ByteArrayInputStream}(data);
 * {@link UBJInputStream} in = new {@link UBJInputStream}(bais);
 * 
 * // Determine the number of pairs in our object.
 * int count = in.readObjectLength();
 * 
 * // "userID": 22345
 * String fieldName = in.readString();
 * int fieldValue = in.readInt32();
 * 
 * // Hypothetical method that switches on name and sets value. 
 * setFieldData(fieldName, fieldValue);
 * 
 * // "username": "billg64" 
 * fieldName = in.readString();
 * fieldValue = in.readString();
 * 
 * // Hypothetical method that switches on name and sets value. 
 * setFieldData(fieldName, fieldValue);
 * 
 * // Get some more data from our main source (file, network, etc.)
 * data = ...
 * 
 * // Reset the stream to read in new data.
 * bais.reset(data);
 * 
 * ... start process over again reading ...
 * </code>
 * </pre>
 * 
 * Since {@link UBJInputStream} maintains no internal state and simply acts as a
 * translation layer between raw bytes in UBJ format and Java data types,
 * resetting the underlying stream that it wraps is a safe operation.
 * <p/>
 * This provides a very efficient mechanism for working with Universal Binary
 * JSON via the core I/O classes without wasting CPU or memory resources
 * creating/destroying byte arrays or output stream.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 * @see ByteArrayOutputStream
 */
public class ByteArrayInputStream extends InputStream {

	private int __offset;
	private int __length;
	private byte[] __buffer;

	private ByteArrayInputStream() {
	}

	public static ByteArrayInputStream newInstance() {
		return new ByteArrayInputStream();
	}

	public static ByteArrayInputStream valueOf(byte[] data) throws IllegalArgumentException {
		var array = new ByteArrayInputStream();
		array.reset(data);
		return array;
	}

	public static ByteArrayInputStream valueOf(byte[] data, int offset, int length) throws IllegalArgumentException {
		var array = new ByteArrayInputStream();
		array.reset(data, offset, length);
		return array;
	}

	@Override
	public int available() throws IOException {
		return (__length - __offset);
	}

	@Override
	public long skip(long number) throws IllegalArgumentException, IOException {
		if (number < 0) {
			throw new IllegalArgumentException("n [" + number + "] must be >= 0");
		}

		// Calculate remaining skip-able bytes.
		int range = (__length - __offset);

		// Trim to the smaller of the two values for our skip amount.
		number = (number < range ? number : range);

		// Skip the bytes
		__offset += number;

		return number;
	}

	@Override
	public int read() throws IOException {
		return (__offset < __length ? __buffer[__offset++] & 0xFF : -1);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IllegalArgumentException, IOException {
		if (buffer == null) {
			throw new IllegalArgumentException("buffer cannot be null");
		}
		if (offset < 0 || length < 0 || (offset + length) > buffer.length) {
			throw new IllegalArgumentException(
					"offset [" + offset + "] and length [" + length + "] must be >= 0 and (offset + length)["
							+ (offset + length) + "] must be <= buffer.length [" + buffer.length + "]");
		}

		// Calculate bytes remaining in the stream.
		int r = (__length - __offset);

		/*
		 * If no bytes are remaining, update the length we return to -1, otherwise begin
		 * the copy operation on the remaining bytes.
		 */
		if (r < 1) {
			length = -1;
		} else {
			/*
			 * Trim the copy length to the smaller of the two values: how many bytes were
			 * requested or how many are left.
			 */
			length = (length < r ? length : r);

			// Copy data into buffer.
			System.arraycopy(__buffer, __offset, buffer, offset, length);
			__offset += length;
		}

		return length;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int readlimit) {
		// do nothing
	}

	@Override
	public void reset() throws IOException {
		__offset = 0;
		__length = 0;
		__buffer = null;
	}

	public void reset(int offset) throws IllegalArgumentException {
		if (offset < 0 || (offset + __length) > __buffer.length) {
			throw new IllegalArgumentException(
					"offset [" + offset + "] must be >= 0 and (offset + getLength()) must be <= getArray().length ["
							+ __buffer.length + "]");
		}

		__offset = offset;
	}

	public void reset(byte[] data) throws IllegalArgumentException {
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}

		reset(data, 0, data.length);
	}

	public void reset(byte[] data, int offset, int length) throws IllegalArgumentException {
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null");
		}
		if (offset < 0 || length < 0 || (offset + length) > data.length) {
			throw new IllegalArgumentException(
					"offset [" + offset + "] and length [" + length + "] must be >= 0 and (offset + length)["
							+ (offset + length) + "] must be <= data.length [" + data.length + "]");
		}

		__offset = 0;
		__length = length;
		__buffer = data;
	}

	public byte[] getArray() {
		return __buffer;
	}

	public int getOffset() {
		return __offset;
	}

	public int getLength() {
		return __length;
	}

}
