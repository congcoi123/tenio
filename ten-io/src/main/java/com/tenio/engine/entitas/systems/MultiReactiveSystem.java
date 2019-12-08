package com.tenio.engine.entitas.systems;

import java.util.List;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.IContexts;
import com.tenio.engine.entitas.api.system.IReactiveSystem;
import com.tenio.engine.entitas.collector.ICollector;
import com.tenio.engine.entitas.factories.EntitasCollections;

/**
 * <p>
 * A ReactiveSystem calls Execute(entities) if there were changes based on the
 * <br>
 * specified Collector and will only pass in changed entities. A common use-case
 * <br>
 * is to react to changes, e.g. a change of the position of an entity to update
 * <br>
 * the <tt>gameObject.transform.position</tt> of the related gameObject.
 * <p>
 * 
 * @author Rubentxu
 */
public abstract class MultiReactiveSystem<TEntity extends Entity, TContexts extends IContexts>
		implements IReactiveSystem {

	private ICollector<TEntity>[] __collectors;
	private List<TEntity> __buffer;

	@SuppressWarnings("unchecked")
	public MultiReactiveSystem(TContexts context) {
		__collectors = _getTrigger(context);
		__buffer = EntitasCollections.createList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MultiReactiveSystem(ICollector[] collector) {
		__collectors = collector;
		__buffer = EntitasCollections.createList();
	}

	/**
	 * Specify the collector that will trigger the ReactiveSystem.
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected abstract ICollector[] _getTrigger(TContexts context);

	/**
	 * This will exclude all entities which don't pass the filter.
	 * 
	 * @param entity
	 * @return
	 */
	protected abstract boolean _filter(TEntity entity);

	protected abstract void _execute(List<TEntity> entities, float delta);

	/**
	 * Activates the ReactiveSystem and starts observing changes based on the
	 * specified Collector. ReactiveSystem are activated by default.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void activate() {
		for (ICollector collector : __collectors) {
			collector.activate();
		}
	}

	/**
	 * Deactivates the ReactiveSystem. No changes will be tracked while deactivated.
	 * This will also clear the ReactiveSystem. ReactiveSystem are activated by
	 * default.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void deactivate() {
		for (ICollector collector : __collectors) {
			collector.deactivate();
		}
	}

	/**
	 * Clears all accumulated changes.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void clear() {
		for (ICollector collector : __collectors) {
			collector.clearCollectedEntities();
		}
	}

	/**
	 * Will call Execute(entities) with changed entities if there are any. Otherwise
	 * it will not call Execute(entities).
	 * 
	 * @param delta
	 */
	@Override
	public void execute(float delta) {
		for (ICollector<TEntity> collector : __collectors) {
			if (collector.getCount() != 0) {
				for (TEntity e : collector.getCollectedEntities()) {
					if (_filter(e)) {
						e.retain(this);
						__buffer.add(e);
					}
				}
				collector.clearCollectedEntities();
			}
		}

		if (__buffer.size() != 0) {
			_execute(__buffer, delta);
			for (int i = 0; i < __buffer.size(); i++) {
				__buffer.get(i).release(this);
			}
			__buffer.clear();
		}

	}

	@Override
	public String toString() {
		return "MultiReactiveSystem{" + "collectors=" + __collectors + ", buffer=" + __buffer + "}";
	}

}
