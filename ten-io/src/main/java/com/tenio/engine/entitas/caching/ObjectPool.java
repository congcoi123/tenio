package com.tenio.engine.entitas.caching;

import java.util.Stack;
import java.util.function.Consumer;

/**
 * @author Rubentxu
 */
public class ObjectPool<T> {
	private Factory<T> __factoryMethod;
	private Consumer<T> __resetMethod;
	private Stack<T> __pool;

	public ObjectPool(Factory<T> factoryMethod, Consumer<T> resetMethod) {
		__factoryMethod = factoryMethod;
		__resetMethod = resetMethod;
		__pool = new Stack<T>();
	}

	public T get() {
		return __pool.size() == 0 ? __factoryMethod.create() : __pool.pop();
	}

	public void push(T obj) {
		if (__resetMethod != null) {
			__resetMethod.accept(obj);
		}
		__pool.push(obj);
	}

	public void reset() {
		__pool.clear();
	}

}
