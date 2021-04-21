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
package com.tenio.core.network.netty.option;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author kong
 */
public final class WriteQueueOutboundChannelHandler extends ChannelOutboundHandlerAdapter {

	private static final int QUEUE_SIZE_WARNING = 5000;

	private ChannelHandlerContext __ctx;
	private final Queue<Object> __messageQueue;
	private int __queueSize = 0;
	private boolean __isWriting;

	public WriteQueueOutboundChannelHandler() {
		__messageQueue = new ConcurrentLinkedQueue<Object>();
		__queueSize = 0;
		__isWriting = false;
	}

	private final ChannelFutureListener __sendListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) {
			if (future.isSuccess()) {
				// __ctx.fireUserEventTriggered("WRITE_MESSAGE_COMPLETE");
				__poll();
			} else {
				System.err.println("failed");

				future.channel().close();
				__messageQueue.clear();
			}
		}
	};

	private void __poll() {
		__isWriting = true;
		if (!__messageQueue.isEmpty()) {
			__ctx.writeAndFlush(__messageQueue.poll()).addListener(__sendListener);
			__queueSize--;
			
//			int size = __queueSize;
//			System.out.println(String.format("Queue Size: %d -> Thread: %ld", size, Thread.currentThread().getId()));
//			System.out.println("Queue Size: " + size + ", Thread: " + Thread.currentThread().getId());
		} else {
			__isWriting = false;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		__ctx = ctx;
		int size = __queueSize;

		if (size > QUEUE_SIZE_WARNING) {
			// Thread.sleep(100);
			// __ctx.flush();
		}

		__messageQueue.offer(msg);
		__queueSize++;

		if (!__isWriting) {
			__poll();
		}
	}
}
