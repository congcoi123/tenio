package com.tenio.core.entities.data;

import com.tenio.common.data.ZeroObject;
import com.tenio.common.utilities.TimeUtility;

public final class ServerMessage {

	private long __createdTimestamp;
	private ZeroObject __data;

	public static ServerMessage newInstance() {
		return new ServerMessage();
	}

	private ServerMessage() {
		__createdTimestamp = TimeUtility.currentTimeMillis();
	}

	public long getCreatedTimestamp() {
		return __createdTimestamp;
	}

	public ZeroObject getData() {
		return __data;
	}

	public ServerMessage setData(ZeroObject data) {
		__data = data;
		return this;
	}

}
