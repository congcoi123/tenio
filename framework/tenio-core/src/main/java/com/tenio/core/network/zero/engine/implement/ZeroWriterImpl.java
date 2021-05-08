package com.tenio.core.network.zero.engine.implement;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.tenio.core.exception.PacketQueueFullException;
import com.tenio.core.exception.PacketQueuePolicyViolationException;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.packet.PacketQueue;
import com.tenio.core.network.entity.session.Session;
import com.tenio.core.network.statistic.NetworkWriterStatistic;
import com.tenio.core.network.zero.engine.ZeroWriter;
import com.tenio.core.network.zero.engine.handler.writer.WriterHandler;
import com.tenio.core.network.zero.engine.listener.ZeroWriterListener;
import com.tenio.core.server.Service;

public final class ZeroWriterImpl extends AbstractZeroEngine implements ZeroWriter, ZeroWriterListener {

	private final BlockingQueue<Session> sessionTicketsQueue;
	private WriterHandler socketWriter;
	private WriterHandler datagramWriter;
	private NetworkWriterStatistic statistic;

	public ZeroWriterImpl(int numberWorkers) {
		super(numberWorkers);
		sessionTicketsQueue = new LinkedBlockingQueue();
	}

	private void __writeLoop() {
		try {
			Session session = this.sessionTicketsQueue.take();
			this.processSessionQueue(session);
		} catch (InterruptedException var3) {
			// logger.warn("EngineWriter thread interruped: " + Thread.currentThread());
		} catch (Throwable var4) {
//			this.logger.warn("Problems in EngineWriter main loop, Thread: " + Thread.currentThread());
		}
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
			// this.logger.warn("Socket closed during write operation for session: " +
			// session);
		} catch (IOException var9) {
			// this.logger.warn("Error during write. Session: " + session);
		} catch (Exception var10) {
			// this.logger.warn("Error during write. Session: " + session);
		}
	}

	@Override
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

	@Override
	public void continueWriteInterestOp(Session session) {
		if (session != null) {
			this.sessionTicketsQueue.add(session);
		}
	}

	@Override
	public NetworkWriterStatistic getNetworkWriterStatistic() {
		return statistic;
	}

	@Override
	public void onSetup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRun() {
		__writeLoop();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getEngineName() {
		return "writer";
	}

}
