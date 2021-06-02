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
package com.tenio.examples.example4.constant;

public final class Example4Constant {

	public static final int DESIGN_WIDTH = 500;
	public static final int DESIGN_HEIGHT = 500;

	public static final int SOCKET_PORT = 8032;
	public static final int DATAGRAM_PORT = 8034;

	public static final float DELAY_CREATION = 0.1f;
	// time in minutes
	public static final int AVERAGE_LATENCY_MEASUREMENT_INTERVAL = 1;
	// time in seconds
	public static final int SEND_MEASUREMENT_REQUEST_INTERVAL = 20;

	public static final int NUMBER_OF_PLAYERS = 100;

	public static final int ONE_SECOND_EXPECT_RECEIVE_PACKETS = 10;

	public static final int ONE_MINUTE_EXPECT_RECEIVE_PACKETS = ONE_SECOND_EXPECT_RECEIVE_PACKETS * 60 * 100;

	private Example4Constant() {

	}

}
