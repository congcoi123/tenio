package com.tenio.engine.entitas.api.system;

import java.util.List;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.collector.TriggerOnEvent;

/**
 * @author Rubentxu
 */
@SuppressWarnings("rawtypes")
public interface IMultiReactiveSystem<E extends Entity> extends ISystem {
	
	void execute(List<Entity> entities);
	
	TriggerOnEvent[] getTriggers();
	
}
