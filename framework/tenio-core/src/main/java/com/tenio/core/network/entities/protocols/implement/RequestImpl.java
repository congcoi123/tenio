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

import com.tenio.common.utilities.TimeUtility;
import com.tenio.core.configuration.defines.ServerEvent;
import com.tenio.core.network.defines.RequestPriority;
import com.tenio.core.network.entities.protocols.Request;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
public final class RequestImpl extends AbstractMessage implements Request {

	private ServerEvent __event;
	private Session __sender;
	private RequestPriority __priority;
	private long __timestamp;

	public static Request newInstance() {
		return new RequestImpl();
	}

	private RequestImpl() {
		super();

		__priority = RequestPriority.NORMAL;
		__timestamp = TimeUtility.currentTimeMillis();
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

}
