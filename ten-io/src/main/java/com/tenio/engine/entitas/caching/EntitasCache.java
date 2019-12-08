package com.tenio.engine.entitas.caching;

import java.util.List;
import java.util.Set;

import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * @author Rubentxu
 */
public class EntitasCache {

	private static ObjectPool<List<IComponent>> __componentArray = new ObjectPool<List<IComponent>>(() -> {
		return EntitasCollections.createList();
	}, null);

	private static ObjectPool<List<Integer>> __integerArray = new ObjectPool<List<Integer>>(() -> {
		return EntitasCollections.createList();
	}, null);

	private static ObjectPool<Set<Integer>> __integerSet = new ObjectPool<Set<Integer>>(() -> {
		return EntitasCollections.createSet();
	}, null);

	@SuppressWarnings("rawtypes")
	private static ObjectPool<List<Set<GroupChanged>>> __groupChangedArray = new ObjectPool<List<Set<GroupChanged>>>(() -> {
		return EntitasCollections.<Set<GroupChanged>>createList();
	}, null);

	public static List<IComponent> getIComponentList() {
		return __componentArray.get();
	}

	public static void pushIComponentList(List<IComponent> list) {
		list.clear();
		__componentArray.push(list);
	}

	public static List<Integer> getIntArray() {
		return __integerArray.get();
	}

	public static void pushIntArray(List<Integer> list) {
		list.clear();
		__integerArray.push(list);
	}

	public static Set<Integer> getIntHashSet() {
		return __integerSet.get();
	}

	public static void pushIntHashSet(Set<Integer> hashSet) {
		hashSet.clear();
		__integerSet.push(hashSet);
	}

	@SuppressWarnings("rawtypes")
	public static List<Set<GroupChanged>> getGroupChangedList() {
		return __groupChangedArray.get();
	}

	@SuppressWarnings("rawtypes")
	public static void pushGroupChangedList(List<Set<GroupChanged>> list) {
		list.clear();
		__groupChangedArray.push(list);
	}

	public static void reset() {
		__componentArray.reset();
		__integerArray.reset();
		__integerSet.reset();
		__groupChangedArray.reset();
	}

}
