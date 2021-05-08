package com.tenio.core.network.entity.packet.implement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.tenio.common.utility.TimeUtility;
import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.packet.Packet;
import com.tenio.core.network.entity.session.Session;

public final class PacketImpl implements Packet, Comparable<Packet>, Cloneable {

	private static AtomicLong __idCounter = new AtomicLong();

	private long __id;
	private long __createdTime;
	private byte[] __data;
	private ResponsePriority __priority;
	private boolean __encrypted;
	private Session __sender;
	private TransportType __transportType;
	private int __originalSize;
	private Map<String, Object> __attributes;
	private Collection<Session> __recipients;
	private byte[] __fragmentBuffer;

	public static PacketImpl newInstance() {
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
	public Session getSender() {
		return __sender;
	}

	@Override
	public void setSender(Session session) {
		__sender = session;
	}

	@Override
	public Object getAttribute(String key) {
		if (__attributes != null) {
			return __attributes.get(key);
		}
		return null;
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (__attributes == null) {
			__attributes = new HashMap<String, Object>();
		}
		__attributes.put(key, value);
	}

	@Override
	public void setAttributes(Map<String, Object> attributes) {
		__attributes = attributes;
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

	/**
	 * It is generally necessary to override the <b>hashCode</b> method whenever
	 * equals method is overridden, so as to maintain the general contract for the
	 * hashCode method, which states that equal objects must have equal hash codes.
	 */
	@Override
	public int hashCode() {
		int c = (int) __id + (int) (__createdTime ^ (__createdTime >>> 32)) + __priority.hashCode();

		int hash = 3;
		hash = 89 * hash + c;
		return hash;
	}

	@Override
	public int compareTo(Packet packet2) {
		var packet1 = this;
		return Integer.compare(packet1.getPriority().getValue(), packet2.getPriority().getValue()) != 0
				? Integer.compare(packet1.getPriority().getValue(), packet2.getPriority().getValue())
				: Long.compare(packet1.getId(), packet2.getId());
	}

	@Override
	public String toString() {
		return String.format("{ id: %d, createdTime: %d, transportType: %s, priority: %s, encrypted: %b }", __id,
				__createdTime, __transportType.toString(), __priority.toString(), __encrypted);
	}

	@Override
	public Packet clone() {
		var packet = PacketImpl.newInstance();
		packet.setAttributes(__attributes);
		packet.setData(__data);
		packet.setFragmentBuffer(__fragmentBuffer);
		packet.setPriority(__priority);
		packet.setEncrypted(__encrypted);
		packet.setRecipients(__recipients);
		packet.setSender(__sender);
		packet.setTransportType(__transportType);
		return packet;
	}

}
