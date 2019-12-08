/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.engine.fsm.entities;

import java.util.concurrent.atomic.AtomicInteger;

import com.tenio.engine.fsm.MessageDispatcher;
import com.tenio.logger.AbstractLogger;

/**
 * An entity is an element of one game, such as a hero, a dragon, a wall, ...
 * 
 * @author kong
 * 
 */
public abstract class AbstractEntity extends AbstractLogger {

	/**
	 * Every entity must have a unique identifying number
	 */
	private int __id;

	/**
	 * This is the next valid ID. Each time a BaseGameEntity is instantiated this
	 * value is updated
	 */
	private static AtomicInteger __nextId = new AtomicInteger();

	public AbstractEntity() {
		__id = __nextId.incrementAndGet();
	}

	/**
	 * Create own entity. It can be caused a duplicate creating
	 * 
	 * @param id the unique id
	 */
	public AbstractEntity(int id) {
		__id = id;
	}

	/**
	 * @return Returns its id
	 */
	public int getId() {
		return __id;
	}

	/**
	 * Use with recreating id counter
	 */
	public static void resetValidID() {
		__nextId = new AtomicInteger();
	}

	/**
	 * All entities must implement an update function
	 * 
	 * @param delta the time between two frames
	 */
	public abstract void update(float delta);

	/**
	 * All entities can communicate using messages. They are sent by using the
	 * {@link MessageDispatcher} singleton class
	 * 
	 * @param msg @see {@link Telegram}
	 * @return Returns <code>true</code> if the message was sent successful
	 */
	public abstract boolean handleMessage(Telegram msg);

}
