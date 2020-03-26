package com.tenio.entitas.utils;

import com.tenio.engine.entitas.Context;
import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.entitas.EntityBaseFactory;

/**
 * ---------------------------------------------------------------------------
 * '<auto-generated>' This code was generated by CodeGeneratorApp.
 * ---------------------------------------------------------------------------
 */
public class TestContext extends Context<TestEntity> {

	public TestContext(int totalComponents, int startCreationIndex, ContextInfo contextInfo,
			EntityBaseFactory<TestEntity> factoryMethod) {
		super(totalComponents, startCreationIndex, contextInfo, factoryMethod, null);
	}

	public TestEntity getPlayerEntity() {
		return getGroup(TestMatcher.Player()).getSingleEntity();
	}

	public boolean isPlayer() {
		return getPlayerEntity() != null;
	}

	public TestContext setPlayer(boolean value) {
		var entity = getPlayerEntity();
		if (value != (entity != null)) {
			if (value) {
				entity.setPlayer(true);
			} else {
				entity.destroy();
			}
		}
		return this;
	}
}