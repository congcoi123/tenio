/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

import com.tenio.engine.fsm.MessageDispatcher;

/**
 * Check out the <a href=
 * "https://en.wikipedia.org/wiki/Finite-state_machine#State/Event_table">State</a>
 * for more details.
 *
 * @param <T> the entity template
 */
public abstract class State<T> {

  /**
   * This will execute when the state is entered.
   *
   * @param entity the current entity
   */
  public abstract void enter(T entity);

  /**
   * This is the state's normal update function.
   *
   * @param entity the current entity
   */
  public abstract void execute(T entity);

  /**
   * This will execute when the state is exited.
   *
   * @param entity the current entity
   */
  public abstract void exit(T entity);

  /**
   * This executes if the agent receives a message from the message dispatcher,
   * see {@link MessageDispatcher}.
   *
   * @param entity the current entity
   * @param msg    the message that sent to this current entity
   * @return <b>true</b> if the message was sent successful, <b>false</b> otherwise
   */
  public abstract boolean onMessage(T entity, Telegram msg);
}
