package com.tenio.entitas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Stack;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.IComponent;
import com.tenio.engine.entitas.api.IContext;
import com.tenio.engine.entitas.api.IGroup;
import com.tenio.engine.entitas.api.entitas.EntityBaseFactory;
import com.tenio.engine.entitas.api.entitas.IEntity;
import com.tenio.engine.entitas.exceptions.ContextDoesNotContainEntityException;
import com.tenio.engine.entitas.exceptions.ContextEntityIndexDoesAlreadyExistException;
import com.tenio.engine.entitas.exceptions.ContextStillHasRetainedEntitiesException;
import com.tenio.engine.entitas.index.PrimaryEntityIndex;
import com.tenio.entitas.components.Position;
import com.tenio.entitas.utils.TestComponentIds;
import com.tenio.entitas.utils.TestContext;
import com.tenio.entitas.utils.TestEntity;
import com.tenio.entitas.utils.TestMatcher;

public class ContextTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private TestContext context;
	private TestEntity entity;

	public EntityBaseFactory<TestEntity> factoryEntity() {
		return () -> {
			return new TestEntity();
		};
	}

	public TestContext createTestPool() {
		return new TestContext(TestComponentIds.totalComponents, 0,
				new ContextInfo("Test", TestComponentIds.componentNames(), TestComponentIds.componentTypes()),
				factoryEntity());
	}

	@Before
	public void setUp() throws Exception {
		context = createTestPool();
		entity = context.createEntity();
		entity.clearEventsListener();
		context.clearEventsListener();

	}

	@Test
	public void OnEntityCreatedTest() {
		context.onEntityCreated((context, e) -> assertTrue(e.isEnabled()));
		entity = context.createEntity();
	}

	@Test
	public void hasEntityTest() {
		assertTrue(context.hasEntity(entity));

	}

	@Test
	public void getCountTest() {
		assertEquals(1, context.getEntitesCount());

	}

	@Test
	public void getEntitiesTest() {
		assertEquals(1, context.getEntities().length);

	}

	@Test
	public void destroyEntityTest() {
		entity.destroy();
		assertEquals(0, context.getEntities().length);

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void OnEntityDestroyedTest() {
		context.onEntityWillBeDestroyed((IContext pool, IEntity e) -> assertTrue(e.isEnabled()));
		context.onEntityDestroyed((pool, e) -> assertFalse(e.isEnabled()));
		context.destroyAllEntities();
		assertEquals(0, context.getEntitesCount());

	}

	@Test
	public void getReusableEntitiesCountTest() {
		var entity2 = context.createEntity();
		assertEquals(0, context.getReusableEntitiesCount());
		entity2.destroy();
		assertEquals(2, context.getReusableEntitiesCount());

	}

	@Test
	public void getRetainedEntitiesCountTest() {
		entity = context.createEntity();
		var entity2 = context.createEntity();
		entity.retain(new Object());
		entity.destroy();
		assertEquals(1, context.getRetainedEntitiesCount());
		entity2.destroy();
		assertEquals(1, context.getRetainedEntitiesCount());

	}

	@Test(expected = ContextStillHasRetainedEntitiesException.class)
	public void PoolStillHasRetainedEntitiesExceptionTest() {
		entity = context.createEntity();
		entity.retain(new Object());
		entity.destroy();
		context.destroyAllEntities();
	}

	@Test(expected = ContextDoesNotContainEntityException.class)
	public void entityIsNotRetainedByOwnerExceptionTest() {
		var entity2 = new TestEntity();
		entity2.initialize(0, 100, null, null, null);
		entity2.destroy();

	}

	@Test
	public void onEntityReleasedTest() {
		entity = context.createEntity();
		entity.destroy();
		assertEquals(2, context.getReusableEntitiesCount());
	}

	@Test
	public void getGroupTest() {
		entity.addComponent(TestComponentIds.Position, new Position());
		var group = context.getGroup(TestMatcher.Position());
		assertEquals(1, group.getCountEntities());
		group = context.getGroup(TestMatcher.Position());
		assertEquals(1, group.getCountEntities());
	}

	@Test
	public void getGroupEntitiesTest() {
		entity.addComponent(TestComponentIds.Position, new Position());
		var group = context.getGroup(TestMatcher.Position());
		assertEquals(1, group.getEntities().length);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void entityIndexTest() {
		entity.addComponent(TestComponentIds.Position, new Position());
		var group = context.getGroup(TestMatcher.Position());
		var index = new PrimaryEntityIndex("positions", (e, c) -> "positionEntities", group);
		context.addEntityIndex(index);
		index = (PrimaryEntityIndex<Entity, String>) context.getEntityIndex("positions");
		assertNotNull(index);
		assertNotNull(index.getEntity("positionEntities"));

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ContextEntityIndexDoesAlreadyExistException.class)
	public void duplicateEntityIndexTest() {
		entity.addComponent(TestComponentIds.Position, new Position());
		var group = context.getGroup(TestMatcher.Position());
		var index = new PrimaryEntityIndex("duplicate", group, (e, c) -> new String[] { "positionEntities" });
		context.addEntityIndex(index);
		context.addEntityIndex(index);

	}

	@Test
	public void clearComponentPoolTest() {
		var cpool = context.getComponentPools();
		cpool[0] = new Stack<IComponent>();
		cpool[0].push(new Position());

		assertEquals(1, cpool[0].size());
		context.clearComponentPool(0);

		assertTrue(cpool[0].empty());

	}

	@Test
	public void clearComponentPoolsTest() {
		var cpool = context.getComponentPools();
		cpool[0] = new Stack<IComponent>();
		cpool[0].push(new Position());

		assertEquals(1, cpool[0].size());
		context.clearComponentPools();

		assertTrue(cpool[0].empty());

	}

	@Test
	public void resetTest() {
		context.onEntityCreated((pool, entity) -> {
		});
		assertEquals(1, context.getOnEntityCreateds().size());
		context.reset();
		assertEquals(0, context.getOnEntityCreateds().size());

	}

	@Test
	public void updateGroupsComponentAddedOrRemovedTest() {
		var position = new Position();
		var group = context.getGroup(TestMatcher.Position());
		group.onEntityAdded((g, e, idx, pc) -> assertEquals(TestComponentIds.Position, idx));

		entity.addComponent(TestComponentIds.Position, position);
		context.__updateGroupsComponentAddedOrRemoved(entity, TestComponentIds.Position, position,
				context.getGroupsForIndex());
		context.__updateGroupsComponentAddedOrRemoved(entity, TestComponentIds.Position, position,
				context.getGroupsForIndex());
		// context.OnGroupCleared = (context, group)->
		// assertNull(context.OnEntityCreated);

	}

	@Test
	public void updateGroupsComponentReplacedTest() {
		var position = new Position();
		var position2 = new Position();
		var groupE = context.getGroup(TestMatcher.Position());
		groupE.onEntityUpdated((IGroup<TestEntity> group, final TestEntity entity, int index,
				IComponent previousComponent, IComponent nc) -> {
			System.out.println("Removed...........");
			assertEquals(position2, nc);
		});

		entity.addComponent(TestComponentIds.Position, position);
		context.__updateGroupsComponentReplaced(entity, TestComponentIds.Position, position, position2,
				context.getGroupsForIndex());

	}

	@Test
	public void createEntityCollectorTest() {
		var collector = context.createCollector(TestMatcher.Position());
		assertNotNull(collector);
	}

}
