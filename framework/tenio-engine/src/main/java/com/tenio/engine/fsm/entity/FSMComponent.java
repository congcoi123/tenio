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

import com.tenio.engine.fsm.Component;
import com.tenio.engine.fsm.MessageDispatcher;

/**
 * Check out the
 * <a href="https://en.wikipedia.org/wiki/Finite-state_machine#State/">FSM</a>
 * for more details.
 * 
 * @param <T> the state template
 */
public final class FSMComponent<T> extends Component<T> {

	/**
	 * The current state, see {@link State}
	 */
	private State<T> __currentState;
	/**
	 * A record of the last state the agent was in, see {@link State}
	 */
	private State<T> __previousState;
	/**
	 * This is called every time the FSM is updated, see {@link State}
	 */
	private State<T> __globalState;
	/**
	 * @see MessageDispatcher
	 */
	private final MessageDispatcher __dispatcher;

	public FSMComponent(final MessageDispatcher dispatcher, T entity) {
		super(entity);
		__dispatcher = dispatcher;
		__currentState = null;
		__previousState = null;
		__globalState = null;
	}

	/**
	 * Use these methods to initialize the FSM
	 * 
	 * @param state the first state
	 */
	public void setCurrentState(State<T> state) {
		__currentState = state;
	}

	/**
	 * The global state is called in every processing
	 * 
	 * @param state the corresponding state
	 */
	public void setGlobalState(State<T> state) {
		__globalState = state;
	}

	/**
	 * Set the previous state
	 * 
	 * @param state the corresponding state
	 */
	public void setPreviousState(State<T> state) {
		__previousState = state;
	}

	public MessageDispatcher getDispatcher() {
		return __dispatcher;
	}

	/**
	 * Call this to update the FSM
	 */
	@Override
	public void update(double delta) {
		// if a global state exists, call its execute method, else do nothing
		if (__globalState != null) {
			__globalState.execute(__entity);
		}

		// same for the current state
		if (__currentState != null) {
			__currentState.execute(__entity);
		}
	}

	public boolean handleMessage(Telegram msg) {
		// First see if the current state is valid and that it can handle
		// the message
		if (__currentState != null && __currentState.onMessage(__entity, msg)) {
			return true;
		}

		// If not, and if a global state has been implemented, send
		// the message to the global state
		if (__globalState != null && __globalState.onMessage(__entity, msg)) {
			return true;
		}

		return false;
	}

	public void changeState(State<T> state) {
		// keep a record of the previous state
		__previousState = __currentState;

		// call the exit method of the existing state
		__currentState.exit(__entity);

		// change state to the new state
		__currentState = state;

		// call the entry method of the new state
		__currentState.enter(__entity);
	}

	/**
	 * Change state back to the previous state
	 */
	public void revertToPreviousState() {
		changeState(__previousState);
	}

	/**
	 * @param state the confirming state
	 * @return <b>true</b> if the current state's type is equal to the type of the
	 *         class passed as a parameter
	 */
	public boolean isInState(State<T> state) {
		return __currentState.getClass() == state.getClass();
	}

	public State<T> getCurrentState() {
		return __currentState;
	}

	public State<T> getGlobalState() {
		return __globalState;
	}

	public State<T> getPreviousState() {
		return __previousState;
	}

	/**
	 * Only be used during debugging to grab the name of the current state
	 * 
	 * @return the name of current state
	 */
	public String getNameOfCurrentState() {
		return __currentState.getClass().getName();
	}

}
