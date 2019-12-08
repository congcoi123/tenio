package com.tenio.engine.entitas.systems;

import java.util.List;

import com.tenio.engine.entitas.api.system.ICleanupSystem;
import com.tenio.engine.entitas.api.system.IExecuteSystem;
import com.tenio.engine.entitas.api.system.IInitializeSystem;
import com.tenio.engine.entitas.api.system.IRenderSystem;
import com.tenio.engine.entitas.api.system.ISystem;
import com.tenio.engine.entitas.api.system.ITearDownSystem;
import com.tenio.engine.entitas.factories.EntitasCollections;
import com.tenio.engine.physic.graphic.Paint;

/**
 * <p>
 * Systems provide a convenient way to group systems. You can add
 * <br>
 * <tt>IInitializeSystem, IExecuteSystem, ICleanupSystem, ITearDownSystem,
 * <br>
 * ReactiveSystem</tt> and other nested Systems instances. All systems will be
 * <br>
 * initialized and executed based on the order you added them.
 * </p>
 * 
 * @author Rubentxu
 */
public final class Systems implements IInitializeSystem, IExecuteSystem, IRenderSystem, ICleanupSystem, ITearDownSystem {

	private List<IInitializeSystem> __initializeSystems;
	private List<IExecuteSystem> __executeSystems;
	private List<IRenderSystem> __renderSystems;
	private List<ICleanupSystem> __cleanupSystems;
	private List<ITearDownSystem> __tearDownSystems;

	/**
	 * Creates a new Systems instance.
	 */
	public Systems() {
		__initializeSystems = EntitasCollections.createList();
		__executeSystems = EntitasCollections.createList();
		__renderSystems = EntitasCollections.createList();
		__cleanupSystems = EntitasCollections.createList();
		__tearDownSystems = EntitasCollections.createList();
	}

	/**
	 * Adds the system instance to the systems list.
	 * 
	 * @param system
	 * @return Systems
	 */
	public Systems add(ISystem system) {
		if (system != null) {
			if (system instanceof IInitializeSystem)
				__initializeSystems.add((IInitializeSystem) system);
			if (system instanceof IExecuteSystem)
				__executeSystems.add((IExecuteSystem) system);
			if (system instanceof IRenderSystem)
				__renderSystems.add((IRenderSystem) system);
			if (system instanceof ICleanupSystem)
				__cleanupSystems.add((ICleanupSystem) system);
			if (system instanceof ITearDownSystem)
				__tearDownSystems.add((ITearDownSystem) system);
		}
		return this;
	}

	/**
	 * Calls initialize() on all IInitializeSystem and other nested Systems
	 * instances in the order you added them.
	 */
	public void initialize() {
		for (IInitializeSystem iSystem : __initializeSystems) {
			iSystem.initialize();
		}
	}

	/**
	 * Calls execute() on all IExecuteSystem and other nested Systems instances in
	 * the order you added them.
	 * 
	 * @param deltaTime
	 */
	public void execute(float deltaTime) {
		for (IExecuteSystem eSystem : __executeSystems) {
			eSystem.execute(deltaTime);
		}
	}

	/**
	 * Calls render() on all IRenderSystem and other nested Systems instances in the
	 * order you added them.
	 */
	@Override
	public void render(Paint paint) {
		for (IRenderSystem eSystem : __renderSystems) {
			eSystem.render(paint);
		}
	}

	/**
	 * Calls cleanup() on all ICleanupSystem and other nested Systems instances in
	 * the order you added them.
	 */
	public void cleanup() {
		for (ICleanupSystem clSystem : __cleanupSystems) {
			clSystem.cleanup();
		}
	}

	/**
	 * Calls tearDown() on all ITearDownSystem and other nested Systems instances in
	 * the order you added them.
	 */
	public void tearDown() {
		for (ITearDownSystem tSystem : __tearDownSystems) {
			tSystem.tearDown();
		}
	}

	/**
	 * Activates all ReactiveSystems in the systems list.
	 */
	@SuppressWarnings("rawtypes")
	public void activateReactiveSystems() {
		for (int i = 0; i < __executeSystems.size(); i++) {
			ReactiveSystem reactiveSystem = (ReactiveSystem) ((__executeSystems.get(i) instanceof ReactiveSystem)
					? __executeSystems.get(i)
					: null);
			if (reactiveSystem != null) {
				reactiveSystem.activate();
			}

			Systems nestedSystems = (Systems) ((__executeSystems.get(i) instanceof Systems) ? __executeSystems.get(i)
					: null);
			if (nestedSystems != null) {
				nestedSystems.activateReactiveSystems();
			}
		}
	}

	/**
	 * Deactivates all ReactiveSystems in the systems list. This will also clear all
	 * ReactiveSystems. This is useful when you want to soft-restart your
	 * application and want to reuse your existing system instances.
	 */
	@SuppressWarnings("rawtypes")
	public void deactivateReactiveSystems() {
		for (int i = 0; i < __executeSystems.size(); i++) {
			ReactiveSystem reactiveSystem = (ReactiveSystem) ((__executeSystems.get(i) instanceof ReactiveSystem)
					? __executeSystems.get(i)
					: null);
			if (reactiveSystem != null) {
				reactiveSystem.deactivate();
			}

			Systems nestedSystems = (Systems) ((__executeSystems.get(i) instanceof Systems) ? __executeSystems.get(i)
					: null);
			if (nestedSystems != null) {
				nestedSystems.deactivateReactiveSystems();
			}
		}
	}

	/**
	 * Clears all ReactiveSystems in the systems list.
	 */
	@SuppressWarnings("rawtypes")
	public void clearReactiveSystems() {
		for (int i = 0; i < __executeSystems.size(); i++) {
			ReactiveSystem reactiveSystem = (ReactiveSystem) ((__executeSystems.get(i) instanceof ReactiveSystem)
					? __executeSystems.get(i)
					: null);
			if (reactiveSystem != null) {
				reactiveSystem.clear();
			}

			Systems nestedSystems = (Systems) ((__executeSystems.get(i) instanceof Systems) ? __executeSystems.get(i)
					: null);
			if (nestedSystems != null) {
				nestedSystems.clearReactiveSystems();
			}
		}
	}

	public void clearSystems() {
		__initializeSystems.clear();
		__executeSystems.clear();
		__renderSystems.clear();
		__cleanupSystems.clear();
		__tearDownSystems.clear();
	}

	@Override
	public String toString() {
		return "Systems{" + "initializeSystems=" + __initializeSystems + ", executeSystems=" + __executeSystems
				+ ", renderSystems=" + __renderSystems + ", cleanupSystems=" + __cleanupSystems + ", tearDownSystems="
				+ __tearDownSystems + '}';
	}

}