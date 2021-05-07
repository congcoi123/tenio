package com.tenio.core.network.zero.engine.implement;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.tenio.common.logger.SystemLogger;
import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.connection.Session;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.zero.engine.handler.writer.WriterHandler;
import com.tenio.core.network.zero.engine.statistic.WriterStatistic;

public final class EngineWriterImpl extends SystemLogger implements Runnable {

	private final ExecutorService threadPool;
	private final BlockingQueue<Session> sessionTicketsQueue = new LinkedBlockingQueue();
	private volatile int threadId = 1;
	private volatile boolean isActive = false;
	private int threadPoolSize;
	private WriterHandler socketWriter;
	private WriterHandler datagramWriter;
	private WriterStatistic statistic;

	public EngineWriterImpl(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}

	public void init(Object o) {
		// super.init(o);
		if (this.isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		} else if (this.threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + this.threadPoolSize);
		} else {
			this.isActive = true;
			this.initThreadPool();
			// this.bootLogger.info("Socket Writer started (pool size:" + this.threadPoolSize + ")");
		}
	}

	public void destroy(Object o) {
		// super.destroy(o);
		this.isActive = false;
		List leftOvers = this.threadPool.shutdownNow();
		// this.bootLogger.info("EngineWriter stopped. Unprocessed tasks: " + leftOvers.size());
	}

	public int getQueueSize() {
		return this.sessionTicketsQueue.size();
	}

	public int getThreadPoolSize() {
		return this.threadPoolSize;
	}

	// set highest priority for the session when channel can write
	public void continueWriteOp(Session session) {
		if (session != null) {
			this.sessionTicketsQueue.add(session);
		}

	}

	private void initThreadPool() {
		for (int j = 0; j < this.threadPoolSize; ++j) {
			this.threadPool.execute(this);
		}

	}

	public void run() {
		Thread.currentThread().setName("EngineWriter-" + this.threadId++);
		
		while (this.isActive) {
			try {
				Session session = this.sessionTicketsQueue.take();
				this.processSessionQueue(session);
			} catch (InterruptedException var3) {
				// logger.warn("EngineWriter thread interruped: " + Thread.currentThread());
				this.isActive = false;
			} catch (Throwable var4) {
//				this.logger.warn("Problems in EngineWriter main loop, Thread: " + Thread.currentThread());
			}
		}

//		this.bootLogger.info("EngineWriter threadpool shutting down.");
	}

	private void processSessionQueue(Session session) {
		if (session == null) {
			return;
		}

		try {
			PacketQueue sessionQ = session.getPacketQueue();
			synchronized (sessionQ) {
				if (sessionQ.isEmpty()) {
					return;
				}

				if (!session.isActivated()) {
					sessionQ.take();
					return;
				}

				var packet = sessionQ.peek();
				if (packet == null) {
					if (!sessionQ.isEmpty()) {
						sessionQ.take();
					}

					return;
				}

				if (session.isTcp()) {
					socketWriter.send(sessionQ, session, null);
				} else if (session.isUdp()) {
					datagramWriter.send(sessionQ, session, null);
				}
			}
		} catch (ClosedChannelException var8) {
			// this.logger.warn("Socket closed during write operation for session: " + session);
		} catch (IOException var9) {
			// this.logger.warn("Error during write. Session: " + session);
		} catch (Exception var10) {
			// this.logger.warn("Error during write. Session: " + session);
		}
	}

	public void enqueuePacket(Packet packet) {
		Collection<Session> recipients = packet.getRecipients();
		if (recipients == null) {
			return;
		}

		if (recipients.size() == 1) {
			this.enqueueLocalPacket(recipients.iterator().next(), packet);
		} else {
			Iterator<Session> iterator = recipients.iterator();

			while (iterator.hasNext()) {
				Session session = iterator.next();
				this.enqueueLocalPacket(session, packet.clone());
			}
		}

	}

	private void enqueueLocalPacket(Session session, Packet packet) {
		if (!session.isActivated()) {
			return;
		}

		PacketQueue sessionQ = session.getPacketQueue();
		if (sessionQ != null) {
			synchronized (sessionQ) {
				int userId = 0;

				try {
					boolean wasEmpty = sessionQ.isEmpty();
					sessionQ.put(packet);

					// only need when packet queue is empty or the session was not in the tickets
					// queue
					if (wasEmpty || !this.sessionTicketsQueue.contains(session)) {
						this.sessionTicketsQueue.add(session);
					}

					packet.setRecipients((Collection) null);
				} catch (PacketQueuePolicyViolationException var10) {
					session.addDroppedPackets(1);
					statistic.updateDroppedPacketsByPolicy(1);
				} catch (PacketQueueFullException var11) {
					session.addDroppedPackets(1);
					statistic.updateDroppedPacketsByFull(1);
				}
			}
		}

	}

}
