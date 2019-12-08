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
package com.tenio.net.mina.codec.socket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.tenio.configuration.constant.Constants;

/**
 * This class support Encoding and Decoding an array of bytes.
 * 
 * @author kong
 * 
 */
public class ByteArrayCodecFactory implements ProtocolCodecFactory {

	/**
	 * @see {@link ProtocolEncoder}
	 */
	private ProtocolEncoder __encoder;
	/**
	 * @see {@link ProtocolDecoder}
	 */
	private ProtocolDecoder __decoder;

	public ByteArrayCodecFactory() {
		__encoder = new ByteArrayEncoder();
		__decoder = new ByteArrayDecoder();
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return __decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return __encoder;
	}

	/**
	 * Decoding an array of bytes.
	 */
	private final class ByteArrayDecoder extends CumulativeProtocolDecoder {

		@Override
		protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

			if (in.prefixedDataAvailable(Constants.HEADER_BYTES)) {
				int length = in.getShort();
				byte[] bytes = new byte[length];
				in.get(bytes);
				out.write(bytes);
				return true;
			}
			return false;
		}

	}

	/**
	 * Encoding an array of bytes.
	 */
	private final class ByteArrayEncoder extends ProtocolEncoderAdapter {

		@Override
		public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
			byte[] data = (byte[]) message;
			int capacity = Constants.HEADER_BYTES + data.length;
			IoBuffer buffer = IoBuffer.allocate(capacity, false);
			buffer.setAutoExpand(true);
			// add header
			buffer.putShort((short) data.length);
			buffer.put(data);
			buffer.flip();
			out.write(buffer);
		}

	}

}
