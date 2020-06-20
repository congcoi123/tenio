/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.configuration.constant.Constants;
import com.tenio.entity.element.TObject;
import com.tenio.message.codec.MessagePacker;
import com.tenio.message.codec.MsgPackConverter;

/**
 * @author kong
 */
public final class MessagePackingTest {

	private TObject __message;

	@BeforeEach
	public void initialize() {
		__message = TObject.newInstance();
		__message.put("string", "String");
		__message.put("integer", 1993);
		__message.put("float", 1.0);
		__message.put("boolean", true);
	}

	@Test
	public void messageSerializeAndUnserializeShouldReturnTrue() {
		// Convert TObject message to byte array
		var bytes = MsgPackConverter.serialize(__message);
		
		// Revert the byte array to TObject message
		assertEquals(__message, MsgPackConverter.unserialize(bytes));
	}

	@Test
	public void messagePackAndUnpackShouldReturnTrue() {
		// Convert TObject message to byte array
		var bytes = MsgPackConverter.serialize(__message);
		// Pack message with header-length value
		var packWithHeader = MessagePacker.pack(bytes);
		// Slide message, keep only the message content
		var packWithContent = Arrays.copyOfRange(packWithHeader, Constants.HEADER_BYTES, packWithHeader.length);
		
		// Revert the byte array to TObject message
		assertEquals(__message, MsgPackConverter.unserialize(packWithContent));
	}

}
