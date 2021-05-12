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
package com.tenio.core.network.zero.engines.implement;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.tenio.core.exceptions.PacketQueueFullException;
import com.tenio.core.exceptions.PacketQueuePolicyViolationException;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.PacketQueue;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.statistics.NetworkWriterStatistic;
import com.tenio.core.network.zero.engines.ZeroWriter;
import com.tenio.core.network.zero.engines.handler.writer.DatagramWriterHandler;
import com.tenio.core.network.zero.engines.handler.writer.SocketWriterHandler;
import com.tenio.core.network.zero.engines.handler.writer.WriterHandler;
import com.tenio.core.network.zero.engines.listener.ZeroWriterListener;

/**
 * @author kong
 */
// TODO: Add description
public final class ZeroWriterImpl extends AbstractZeroEngine implements ZeroWriter, ZeroWriterListener {

	private BlockingQueue<Session> __sessionTicketsQueue;
	private WriterHandler __socketWriterHandler;
	private WriterHandler __datagramWriterHandler;
	private NetworkWriterStatistic __networkWriterStatistic;

	public ZeroWriterImpl() {
		super();
		__sessionTicketsQueue = new LinkedBlockingQueue<Session>();
		setName("writer");
	}

	private void __initializeSocketWriterHandler() {
		__socketWriterHandler = new SocketWriterHandler();
		__socketWriterHandler.setNetworkWriterStatistic(__networkWriterStatistic);
		__socketWriterHandler.setSessionTicketsQueue(__sessionTicketsQueue);
		__socketWriterHandler.allocateBuffer(getMaxBufferSize());
	}

	private void __initializeDatagramWriterHandler() {
		__datagramWriterHandler = new DatagramWriterHandler();
		__datagramWriterHandler.setNetworkWriterStatistic(__networkWriterStatistic);
		__datagramWriterHandler.setSessionTicketsQueue(__sessionTicketsQueue);
		__datagramWriterHandler.allocateBuffer(getMaxBufferSize());
	}

	private void __writableLoop() {
		try {
			Session session = __sessionTicketsQueue.take();
			__processSessionQueue(session);
			// FIXME: Need to handle these exceptions
		} catch (InterruptedException e) {
			error(e);
		}
	}

	private void __processSessionQueue(Session session) {

		// ignore the null session
		if (session == null) {
			return;
		}

		try {
			// now we can iterate packets from queue to proceed
			PacketQueue packetQueue = session.getPacketQueue();
			synchronized (packetQueue) {
				// ignore the empty queue
				if (packetQueue.isEmpty()) {
					return;
				}

				// when the session is in-activated, just ignore its packets
				if (!session.isActivated()) {
					packetQueue.take();
					return;
				}

				Packet packet = packetQueue.peek();
				// ignore the null packet and remove it from queue
				if (packet == null) {
					if (!packetQueue.isEmpty()) {
						packetQueue.take();
					}

					return;
				}

				if (packet.isTcp()) {
					__socketWriterHandler.send(packetQueue, session, packet);
				} else if (packet.isUdp()) {
					__datagramWriterHandler.send(packetQueue, session, packet);
				}
			}
			// FIXME: Need to handle these exceptions
		} catch (ClosedChannelException e) {
			error(e);
		} catch (IOException e) {
			error(e);
		} catch (Exception e) {
			error(e);
		}
	}

	@Override
	public void enqueuePacket(Packet packet) {
		// retrieve all recipient sessions from the packet
		Collection<Session> recipients = packet.getRecipients();
		if (recipients == null) {
			return;
		}

		// when there is only one recipient, no need to create clone packets
		if (recipients.size() == 1) {
			__enqueuePacket(recipients.iterator().next(), packet);
		} else {
			Iterator<Session> sessionIterator = recipients.iterator();

			// one session needs one packet in its queue, need to clone the packet
			while (sessionIterator.hasNext()) {
				Session session = sessionIterator.next();
				__enqueuePacket(session, packet.clone());
			}
		}

	}

	private void __enqueuePacket(Session session, Packet packet) {
		// check the session state once more time
		if (!session.isActivated()) {
			return;
		}

		// loops through the packet queue and handles its packets
		PacketQueue packetQueue = session.getPacketQueue();
		if (packetQueue != null) {
			synchronized (packetQueue) {
				try {
					// get the current state first
					boolean isEmpty = packetQueue.isEmpty();
					// now can put new item into the queue
					packetQueue.put(packet);

					// only need when the packet queue is empty or the session was not in the
					// tickets queue
					if (isEmpty || !__sessionTicketsQueue.contains(session)) {
						__sessionTicketsQueue.add(session);
					}

					packet.setRecipients(null);
				} catch (PacketQueuePolicyViolationException e) {
					session.addDroppedPackets(1);
					__networkWriterStatistic.updateDroppedPacketsByPolicy(1);
				} catch (PacketQueueFullException e) {
					session.addDroppedPackets(1);
					__networkWriterStatistic.updateDroppedPacketsByFull(1);
				}
			}
		}

	}

	@Override
	public void continueWriteInterestOp(Session session) {
		if (session != null) {
			__sessionTicketsQueue.add(session);
		}
	}

	@Override
	public NetworkWriterStatistic getNetworkWriterStatistic() {
		return __networkWriterStatistic;
	}

	@Override
	public void setNetworkWriterStatistic(NetworkWriterStatistic networkWriterStatistic) {
		__networkWriterStatistic = networkWriterStatistic;
	}

	@Override
	public void onInitialized() throws Exception {
		__initializeSocketWriterHandler();
		__initializeDatagramWriterHandler();
	}

	@Override
	public void onStarted() throws Exception {
		// do nothing
	}

	@Override
	public void onResumed() {
		// do nothing
	}

	@Override
	public void onRunning() {
		__writableLoop();
	}

	@Override
	public void onPaused() {
		// do nothing
	}

	@Override
	public void onStopped() throws Exception {
		__sessionTicketsQueue.clear();
	}

	@Override
	public void onDestroyed() {
		__sessionTicketsQueue = null;
		__socketWriterHandler = null;
		__datagramWriterHandler = null;
	}

}
