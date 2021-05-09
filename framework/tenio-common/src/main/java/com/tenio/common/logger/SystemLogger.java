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
package com.tenio.common.logger;

/**
 * The recording logs of a developer is important for every system. This class
 * uses <a href="https://logging.apache.org/log4j/2.x/">Log4j</a> for robust and
 * fast logging. It uses a pool mechanism to increase performance. Every class
 * should be derived from this one so that you can have more chances to
 * investigate bugs in the production environment via daily log files.
 * 
 * @author kong
 * 
 */
public abstract class SystemLogger extends AbstractLogger {

	/**
	 * Only use for debugging EVENTS in the server system. Be careful when using it
	 * yourself. You are warned!
	 * 
	 * @param type the event's type
	 * @param msg  the extra information for "type"
	 */
	public void debug(String type, Object... msg) {
		if (!__logger.isDebugEnabled()) {
			return;
		}

		StringBuilder builder = __stringPool.get();
		builder.append("{").append(type).append("} ");

		for (int i = 0; i < msg.length - 1; i++) {
			builder.append(msg[i]).append(", ");
		}
		if (msg.length > 0) {
			builder.append(msg[msg.length - 1]);
		}

		__logger.debug(builder.toString());
		__stringPool.repay(builder);
	}

	/**
	 * Only use for debugging EVENTS in the server system. Be careful when using it
	 * yourself. You are warned!
	 * 
	 * @param type the event's type
	 * @param msg  the extra information for "type"
	 */
	public void trace(String type, Object... msg) {
		if (!__logger.isTraceEnabled()) {
			return;
		}

		StringBuilder builder = __stringPool.get();
		builder.append("<").append(type).append("> ");

		for (int i = 0; i < msg.length - 1; i++) {
			builder.append(msg[i]).append(", ");
		}
		if (msg.length > 0) {
			builder.append(msg[msg.length - 1]);
		}

		__logger.trace(builder.toString());
		__stringPool.repay(builder);
	}

	/**
	 * Only use for debugging PACKAGE in the server system. Be careful when using it
	 * yourself. You are warned!
	 * 
	 * @param where    where you put this log
	 * @param subWhere the extra information for "where" you put this log
	 * @param tag      the tag type
	 * @param msg      the message content
	 */
	public void trace(String where, Object subWhere, String tag, String msg) {
		if (!__logger.isTraceEnabled()) {
			return;
		}
		StringBuilder builder = __stringPool.get();
		builder.append("<").append(where).append(" ").append(subWhere).append(">").append("[").append(tag).append("] ")
				.append(msg);
		__logger.trace(builder.toString());
		__stringPool.repay(builder);
	}

}
