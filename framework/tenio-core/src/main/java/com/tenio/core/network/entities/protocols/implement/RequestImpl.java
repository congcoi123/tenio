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
package com.tenio.core.network.entities.protocols.implement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.network.defines.RequestPriority;
import com.tenio.core.network.entities.protocols.Request;
import com.tenio.core.network.entities.session.Session;

public final class RequestImpl implements Request {

	private static AtomicLong __idCounter = new AtomicLong();

	private long __id;
	private Map<String, Object> __attributes;
	private ServerEvent __event;
	private Session __sender;
	private RequestPriority __priority;
	private long __timestamp;

	public static Request newInstance() {
		return new RequestImpl();
	}

	private RequestImpl() {
		__id = __idCounter.getAndIncrement();
		__priority = RequestPriority.NORMAL;
		__timestamp = TimeUtility.currentTimeMillis();
		__attributes = new ConcurrentHashMap<String, Object>();
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public Object getAttribute(String key) {
		return __attributes.get(key);
	}

	@Override
	public Request setAttribute(String key, Object value) {
		__attributes.put(key, value);
		return this;
	}

	@Override
	public Session getSender() {
		return __sender;
	}

	@Override
	public Request setSender(Session session) {
		__sender = session;

		return this;
	}

	@Override
	public RequestPriority getPriority() {
		return __priority;
	}

	@Override
	public Request setPriority(RequestPriority priority) {
		__priority = priority;

		return this;
	}

	@Override
	public long getTimestamp() {
		return __timestamp;
	}

	@Override
	public ServerEvent getEvent() {
		return __event;
	}

	@Override
	public Request setEvent(ServerEvent event) {
		__event = event;

		return this;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Request)) {
			return false;
		} else {
			var request = (Request) object;
			return getId() == request.getId();
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
	public String toString() {
		return String.format("{ event: %s, sender: %s, priority: %s, timestamp: %d, attributes: %s }",
				__event.toString(), __sender.toString(), __priority.toString(), __timestamp, __attributes.toString());
	}

}
