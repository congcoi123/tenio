package com.tenio.engine.entitas.api.entitas;

import java.util.Stack;

import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.IComponent;

/**
 * @author Rubentxu
 */
public interface IEntity extends IAERC {

	int getTotalComponents();

	int getCreationIndex();

	boolean isEnabled();

	Stack<IComponent>[] getComponentPools();

	ContextInfo getContextInfo();

	IAERC getAERC();

	void initialize(int creationIndex, int totalComponents, Stack<IComponent>[] componentPools, ContextInfo contextInfo,
			IAERC aerc);

	void reactivate(int creationIndex);

	void addComponent(int index, IComponent component);

	void removeComponent(int index);

	void replaceComponent(int index, IComponent component);

	IComponent getComponent(int index);

	IComponent[] getComponents();

	int[] getComponentIndices();

	boolean hasComponent(int index);

	boolean hasComponents(int... indices);

	boolean hasAnyComponent(int... indices);
	
	IComponent recoverComponent(int index);

	void removeAllComponents();

	Stack<IComponent> getComponentPool(int index);

	IComponent createComponent(int index, @SuppressWarnings("rawtypes") Class clazz);

	<T> T createComponent(int index);

	void destroy();

	void internalDestroy();
	
	void clearEventsListener();

	void removeAllOnEntityReleasedHandlers();

}
