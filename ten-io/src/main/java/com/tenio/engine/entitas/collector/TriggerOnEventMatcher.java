package com.tenio.engine.entitas.collector;

import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.matcher.IMatcher;
import com.tenio.engine.entitas.group.GroupEvent;

/**
 * @author Rubentxu
 */
public class TriggerOnEventMatcher {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <TEntity extends IEntity> TriggerOnEvent<TEntity> added(IMatcher<TEntity> matcher) {
		return new TriggerOnEvent(matcher, GroupEvent.ADDED);
	}

	public static <TEntity extends IEntity> TriggerOnEvent<TEntity> removed(IMatcher<TEntity> matcher) {
		return new TriggerOnEvent<TEntity>(matcher, GroupEvent.REMOVED);
	}

	public static <TEntity extends IEntity> TriggerOnEvent<TEntity> AddedOrRemoved(IMatcher<TEntity> matcher) {
		return new TriggerOnEvent<TEntity>(matcher, GroupEvent.ADDED_OR_REMOVED);
	}
	
}
