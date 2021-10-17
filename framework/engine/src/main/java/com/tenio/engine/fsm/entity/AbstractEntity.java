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

package com.tenio.engine.fsm.entity;

import com.tenio.common.logger.AbstractLogger;
import com.tenio.engine.fsm.MessageDispatcher;
import java.util.UUID;

/**
 * An entity is an element of one game, such as a hero, a dragon, a wall, ...
 */
public abstract class AbstractEntity extends AbstractLogger {

  /**
   * Every entity must have a unique identifying number.
   */
  private String id;

  public AbstractEntity() {
    id = UUID.randomUUID().toString();
  }

  /**
   * Create own entity. It can be caused a duplicate creating.
   *
   * @param id the unique id
   */
  public AbstractEntity(String id) {
    setId(id);
  }

  /**
   * Retrieves the entity id.
   *
   * @return entity id
   */
  public String getId() {
    return id;
  }

  /**
   * Create own entity. It can be caused a duplicate creating.
   *
   * @param id the unique id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * All entities must implement an update function.
   *
   * @param deltaTime the time between two frames
   */
  public abstract void update(float deltaTime);

  /**
   * All entities can communicate using messages. They are sent by using the
   * {@link MessageDispatcher} class
   *
   * @param msg see {@link Telegram}
   * @return <b>true</b> if the message was sent successful, <b>false</b> otherwise
   */
  public abstract boolean handleMessage(final Telegram msg);
}
