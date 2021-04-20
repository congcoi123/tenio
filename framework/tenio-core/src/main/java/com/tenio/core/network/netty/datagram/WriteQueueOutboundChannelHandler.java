package com.tenio.core.network.netty.datagram;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class WriteQueueOutboundChannelHandler extends ChannelOutboundHandlerAdapter {
	private static final int QUEUE_SIZE_WARNING = 5000;
	private ChannelHandlerContext ctx;
	private final Queue<Object> messageQueue = new ConcurrentLinkedQueue<>();
	private int qSize = 0; // should make access synchronized
	private boolean isWriting;

	private final ChannelFutureListener sendListener = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) {
			if (future.isSuccess()) {
				ctx.fireUserEventTriggered("WRITE_MESSAGE_COMPLETE"); // might want to do a message counter in another
																		// handler
				// System.err.println("succeed");
				poll();
			} else {
				System.err.println("failed");
				
				future.channel().close();
				messageQueue.clear();
			}
		}
	};

	private void poll() {
		isWriting = true;
		if (!messageQueue.isEmpty()) {
			this.ctx.writeAndFlush(messageQueue.poll()).addListener(sendListener);
			qSize--;
		} else {
			isWriting = false;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		this.ctx = ctx;
		int size = qSize;
		if (size > QUEUE_SIZE_WARNING) {
			System.out.println("Queue size: " + size); // should handle somehow
		}
		messageQueue.offer(msg);
		qSize++;
		if (!isWriting) {
			poll();
		}
	}
}
