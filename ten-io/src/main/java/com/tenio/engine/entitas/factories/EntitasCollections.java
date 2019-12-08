package com.tenio.engine.entitas.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author Rubentxu
 */
public class EntitasCollections {

	public static <T> List<T> createList() {
		return new ArrayList<T>();
	}

	public static <T> Stack<T> createStack() {
		return new Stack<T>();
	}

	public static <T> Set<T> createSet() {
		return new HashSet<T>();
	}

	public static <K, V> Map<K, V> createMap() {
		return new HashMap<K, V>();
	}

}
