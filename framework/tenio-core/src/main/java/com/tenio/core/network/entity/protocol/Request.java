package com.tenio.core.network.entity.protocol;

import com.tenio.core.network.define.RequestPriority;
import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.Session;

public interface Request extends Message {
	TransportType getTransportType();

	void setTransportType(TransportType var1);

	Session getSender();

	void setSender(Session var1);

	RequestPriority getPriority();

	void setPriority(ResponsePriority var1);

	long getTimeStamp();

	void setTimeStamp(long var1);

	boolean isTcp();

	boolean isUdp();

	boolean isWebsocket();
}
