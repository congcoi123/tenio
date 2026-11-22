/*
The MIT License

Copyright (c) 2016-2026 kong <congcoi123@gmail.com>

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

package com.tenio.examples.example12;

import com.tenio.common.data.DataUtility;
import com.tenio.examples.client.TCP;
import com.tenio.examples.server.SharedEventKey;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Run against any of the three profile configurations to compare the welcome message.
 */
public final class TestClientProfiles {

    public static void main(String[] args) throws IOException, InterruptedException {
        var received = new CountDownLatch(1);
        try (var tcp = new TCP("localhost", 8032, 5000)) {
            tcp.send(DataUtility.newZeroMap().putString(SharedEventKey.KEY_PLAYER_LOGIN, "profile-demo"));
            tcp.receive(message -> {
                System.out.println(message);
                received.countDown();
            });
            if (!received.await(5, TimeUnit.SECONDS)) {
                throw new IOException("Profile server did not respond within five seconds");
            }
        }
    }
}
