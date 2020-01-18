package com.tenio.entitas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.events.GroupChanged;
import com.tenio.engine.entitas.group.Group;
import com.tenio.entitas.components.Position;
import com.tenio.entitas.components.View;
import com.tenio.entitas.utils.TestComponentIds;
import com.tenio.entitas.utils.TestEntity;
import com.tenio.entitas.utils.TestMatcher;

public class GroupTest {

	private Group<TestEntity> group;
	private TestEntity entity;
	@SuppressWarnings({ "rawtypes", "unused" })
	private Group group2;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {

		entity = new TestEntity();
		entity.initialize(0, 10, new Stack[10],
				new ContextInfo("Test", TestComponentIds.componentNames(), TestComponentIds.componentTypes()), null);
		entity.clearEventsListener();
		entity.reactivate(0);
		entity.addComponent(TestComponentIds.Position, new Position(100, 100));
		entity.addComponent(TestComponentIds.View, new View(1));

		group = new Group<TestEntity>(TestMatcher.Position(), TestEntity.class);
		group2 = new Group(TestMatcher.Interactive(), Entity.class);

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void handleEntityTest() {
		GroupChanged<TestEntity> lambda = (group, e, index, component) -> {
			entityEquals(entity, e);
		};
		group.onEntityAdded(lambda);
		Set<GroupChanged> changed = group.handleEntity(entity);
		assertTrue(changed.contains(lambda));
	}

	private void entityEquals(TestEntity entity, Object entity2) {
		assertEquals(entity, entity2);
	}

	@Test
	public void handleEntityOnEntityRemovedTest() {
		GroupChanged<TestEntity> lambda = (group, e, idx, component) -> assertEquals(entity, e);
		group.onEntityRemoved(lambda);
		var changed = group.handleEntity(entity);
		assertEquals(0, changed.size());

		entity.removeComponent(TestComponentIds.Position);
		changed = group.handleEntity(entity);
		assertTrue(changed.contains(lambda));
	}

	@Test
	public void handleEntitySilentlyTest() {
		group.handleEntitySilently(entity);
		assertEquals(1, group.getEntities().length);
	}

	@Test
	public void handleEntitySilentlyOnEntityRemovedTest() {
		group.handleEntitySilently(entity);
		assertEquals(1, group.getEntities().length);

		entity.removeComponent(TestComponentIds.Position);
		group.handleEntitySilently(entity);
		assertFalse(group.containsEntity(entity));
	}

	@Test
	public void getSingleEntityTest() {
		assertNull(group.getSingleEntity());
		group.handleEntitySilently(entity);

		assertEquals(entity, group.getSingleEntity());
	}

}
