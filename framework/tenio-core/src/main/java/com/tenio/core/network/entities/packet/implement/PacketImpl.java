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
package com.tenio.core.network.entities.packet.implement;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.network.defines.ResponsePriority;
import com.tenio.core.network.defines.TransportType;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.session.Session;

public final class PacketImpl implements Packet, Comparable<Packet>, Cloneable {

	private static AtomicLong __idCounter = new AtomicLong();

	private long __id;
	private long __createdTime;
	private byte[] __data;
	private ResponsePriority __priority;
	private boolean __encrypted;
	private TransportType __transportType;
	private int __originalSize;
	private Collection<Session> __recipients;
	private byte[] __fragmentBuffer;

	public static Packet newInstance() {
		return new PacketImpl();
	}

	private PacketImpl() {
		__id = __idCounter.getAndIncrement();
		__createdTime = TimeUtility.currentTimeMillis();
		__transportType = TransportType.UNKNOWN;
		__priority = ResponsePriority.NORMAL;
		__encrypted = false;
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public byte[] getData() {
		return __data;
	}

	@Override
	public void setData(byte[] binary) {
		__data = binary;
		__originalSize = binary.length;
	}

	@Override
	public TransportType getTransportType() {
		return __transportType;
	}

	@Override
	public void setTransportType(TransportType type) {
		__transportType = type;
	}

	@Override
	public ResponsePriority getPriority() {
		return __priority;
	}

	@Override
	public void setPriority(ResponsePriority priority) {
		__priority = priority;
	}

	@Override
	public boolean isEncrypted() {
		return __encrypted;
	}

	@Override
	public void setEncrypted(boolean encrypted) {
		__encrypted = encrypted;
	}

	@Override
	public Collection<Session> getRecipients() {
		return __recipients;
	}

	@Override
	public void setRecipients(Collection<Session> recipients) {
		__recipients = recipients;
	}

	@Override
	public long getCreatedTime() {
		return __createdTime;
	}

	@Override
	public int getOriginalSize() {
		return __originalSize;
	}

	@Override
	public boolean isTcp() {
		return __transportType == TransportType.TCP;
	}

	@Override
	public boolean isUdp() {
		return __transportType == TransportType.UDP;
	}

	@Override
	public boolean isWebSocket() {
		return __transportType == TransportType.WEB_SOCKET;
	}

	@Override
	public byte[] getFragmentBuffer() {
		return __fragmentBuffer;
	}

	@Override
	public void setFragmentBuffer(byte[] binary) {
		__fragmentBuffer = binary;
	}

	@Override
	public boolean isFragmented() {
		return __fragmentBuffer != null;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Packet)) {
			return false;
		} else {
			var packet = (Packet) object;
			return getId() == packet.getId();
		}
	}

	/**
	 * It is generally necessary to override the <b>hashCode</b> method whenever
	 * equals method is overridden, so as to maintain the general contract for the
	 * hashCode method, which states that equal objects must have equal hash codes.
	 * 
	 * @see <a href="https://imgur.com/x6rEAZE">Formula</a>
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (__id ^ (__id >>> 32));
		return result;
	}

	@Override
	public int compareTo(Packet packet2) {
		var packet1 = this;
		return Integer.compare(packet1.getPriority().getValue(), packet2.getPriority().getValue()) != 0
				? Integer.compare(packet1.getPriority().getValue(), packet2.getPriority().getValue())
				: Long.compare(packet2.getId(), packet1.getId());
	}

	@Override
	public String toString() {
		return String.format("{ id: %d, createdTime: %d, transportType: %s, priority: %s, encrypted: %b }", __id,
				__createdTime, __transportType.toString(), __priority.toString(), __encrypted);
	}

	@Override
	public Packet clone() {
		var packet = PacketImpl.newInstance();
		packet.setData(__data);
		packet.setFragmentBuffer(__fragmentBuffer);
		packet.setPriority(__priority);
		packet.setEncrypted(__encrypted);
		packet.setRecipients(__recipients);
		packet.setTransportType(__transportType);
		return packet;
	}

}
