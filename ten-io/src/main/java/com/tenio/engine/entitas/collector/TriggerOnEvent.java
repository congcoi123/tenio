package com.tenio.engine.entitas.collector;

import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.api.matcher.IMatcher;
import com.tenio.engine.entitas.group.GroupEvent;

/**
 * @author Rubentxu
 */
public final class TriggerOnEvent<TEntity extends IEntity> {
	public IMatcher<TEntity> matcher;
	public GroupEvent groupEvent;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TriggerOnEvent(IMatcher matcher, GroupEvent groupEvent) {
		this.matcher = matcher;
		this.groupEvent = groupEvent;
	}

}
