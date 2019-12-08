package com.tenio.engine.entitas.systems;

import java.util.List;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.IContext;
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
 * </p>
 * 
 * @author Rubentxu
 */
public abstract class ReactiveSystem<TEntity extends Entity> implements IReactiveSystem {

	private ICollector<TEntity> __collector;
	private List<TEntity> __buffer;

	public ReactiveSystem(IContext<TEntity> context) {
		__collector = _getTrigger(context);
		__buffer = EntitasCollections.createList();
	}

	public ReactiveSystem(ICollector<TEntity> collector) {
		__collector = collector;
		__buffer = EntitasCollections.createList();
	}

	/**
	 * Specify the collector that will trigger the ReactiveSystem.
	 * 
	 * @param context
	 * @return ICollector<TEntity>
	 */
	protected abstract ICollector<TEntity> _getTrigger(IContext<TEntity> context);

	/**
	 * This will exclude all entities which don't pass the filter.
	 * 
	 * @param entity
	 * @return boolean
	 */
	protected abstract boolean _filter(TEntity entity);

	protected abstract void _execute(List<TEntity> entities, float delta);

	/**
	 * Activates the ReactiveSystem and starts observing changes based on the
	 * specified Collector. ReactiveSystem are activated by default.
	 */
	@Override
	public void activate() {
		__collector.activate();
	}

	/**
	 * Deactivates the ReactiveSystem. No changes will be tracked while deactivated.
	 * This will also clear the ReactiveSystem. ReactiveSystem are activated by
	 * default.
	 */
	@Override
	public void deactivate() {
		__collector.deactivate();
	}

	/**
	 * Clears all accumulated changes.
	 */
	@Override
	public void clear() {
		__collector.clearCollectedEntities();
	}

	/**
	 * Will call execute(entities) with changed entities if there are any. Otherwise
	 * it will not call Execute(entities).
	 * 
	 * @param delta
	 */
	@Override
	public void execute(float delta) {
		if (__collector.getCount() != 0) {
			for (TEntity e : __collector.getCollectedEntities()) {
				if (_filter(e)) {
					e.retain(this);
					__buffer.add(e);
				}
			}
			__collector.clearCollectedEntities();

			if (__buffer.size() != 0) {
				_execute(__buffer, delta);
				for (int i = 0; i < __buffer.size(); i++) {
					__buffer.get(i).release(this);
				}
				__buffer.clear();
			}
		}
	}

	@Override
	public String toString() {
		return "ReactiveSystem{" + "collectors=" + __collector + ", buffer=" + __buffer + "}";
	}

}
