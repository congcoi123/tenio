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

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tMap;

import java.io.IOException;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import com.tenio.entities.element.TArray;
import com.tenio.entities.element.TObject;
import com.tenio.pool.ByteArrayInputStreamPool;
import com.tenio.pool.IElementPool;

/**
 * <a href="https://msgpack.org/index.html">MessagePack</a> is an efficient
 * binary serialization format. It lets you exchange data among multiple
 * languages like JSON. But it's faster and smaller. Small integers are encoded
 * into a single byte, and typical short strings require only one extra byte in
 * addition to the strings themselves. This class helps you convert one system
 * object @see {@link TObject} to MsgPack data and vice versa.
 * 
 * @author kong
 * 
 */
public final class MsgPackConverter {

	/**
	 * Serialize an object to an array of bytes data
	 * 
	 * @param object a {@link Map} type object
	 * @return Returns an array of bytes data
	 */
	public static byte[] serialize(Map<String, Object> object) {
		return MsgPackUtil.pack(object);
	}

	/**
	 * Unserialize an array of bytes data to a {@link TObject}
	 * 
	 * @param msg an array of bytes data
	 * @return Returns an object in <code>TObject</code> type
	 */
	public static TObject unserialize(byte[] msg) {
		TObject object = new TObject();
		Map<String, Value> dstMap = MsgPackUtil.unpack(msg);
		if (dstMap == null || dstMap.isEmpty()) {
			return null;
		}
		dstMap.forEach((key, value) -> {
			object.put(key, MsgPackUtil.valueToObject(value));
		});
		return object;
	}

	private final static class MsgPackUtil {

		/**
		 * A MsgPack instance
		 */
		private static final MessagePack __packer = new MessagePack();
		/**
		 * An object creation pool instance, which is used for re-use byte objects @see
		 * {@link IElementPool}
		 */
		private static final ByteArrayInputStreamPool __bytesPool = new ByteArrayInputStreamPool();

		/**
		 * Converting an object @see {@link TObject} to array of bytes data
		 * 
		 * @param map an object in {@link Map} type
		 * @return Returns an array of bytes data
		 */
		public static byte[] pack(Map<String, Object> map) {
			try {
				return __packer.write(map);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * Converting an array of bytes data to a {@link Map} object
		 * 
		 * @param msg an array of bytes
		 * @return Returns a object in map type
		 */
		public static Map<String, Value> unpack(byte[] msg) {
			Template<Map<String, Value>> mapTmpl = tMap(TString, TValue);
			ByteArrayInputStream in = __bytesPool.get();
			try {
				in.reset(msg);
				Unpacker unpacker = __packer.createUnpacker(in);
				return unpacker.read(mapTmpl);
			} catch (IOException | IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} finally {
				// anyway --> repay
				__bytesPool.repay(in);
			}
		}

		/**
		 * Converting value in MsgPack type to its corresponding in Java type
		 * 
		 * @param value the value in {@link Value} type
		 * @return Returns an object in Java type
		 */
		public static Object valueToObject(Value value) {
			if (value.isNilValue()) {
				value.asNilValue();
				return null;
			} else if (value.isRawValue()) {
				// String only
				return value.asRawValue().getString();
			} else if (value.isBooleanValue()) {
				return value.asBooleanValue().getBoolean();
			} else if (value.isFloatValue()) {
				// Double only (8 bytes)
				return value.asFloatValue().getDouble();
			} else if (value.isIntegerValue()) {
				// Integer only (4 bytes)
				return value.asIntegerValue().getInt();
			} else if (value.isArrayValue()) {
				// Convert value to list of objects (TArray)
				ArrayValue arr = value.asArrayValue();
				TArray array = new TArray();
				for (Value element : arr) {
					array.add(valueToObject(element));
				}
				return array;
			} else if (value.isMapValue()) {
				// FIXME: Temporary not support
				throw new UnsupportedOperationException();
			} else {
				throw new UnsupportedOperationException();
			}
		}

	}

}
