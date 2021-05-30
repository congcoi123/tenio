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
package com.tenio.examples.example4;

import com.tenio.common.configuration.Configuration;
import com.tenio.core.AbstractApp;
import com.tenio.core.bootstrap.annotations.Bootstrap;
import com.tenio.examples.server.TestConfiguration;

/**
 * This class makes a simple simulator for the physic 2d movement.
 */
@Bootstrap
public final class TestServerMovement extends AbstractApp {

	public static void main(String[] params) {
		var game = new TestServerMovement();
		game.start(TestServerMovement.class);
	}

	@Override
	public TestConfiguration getConfiguration() {
		return new TestConfiguration("TenIOConfig.example4.xml");
	}

	@Override
	public void onStarted(Configuration configuration) {

	}

	@Override
	public void onShutdown() {

	}

}
