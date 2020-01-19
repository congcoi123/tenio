package com.tenio.entitas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.tenio.engine.entitas.Context;
import com.tenio.engine.entitas.Entity;
import com.tenio.engine.entitas.api.ContextInfo;
import com.tenio.engine.entitas.api.IContext;
import com.tenio.engine.entitas.api.entitas.EntityBaseFactory;
import com.tenio.engine.entitas.api.system.ICleanupSystem;
import com.tenio.engine.entitas.api.system.IExecuteSystem;
import com.tenio.engine.entitas.api.system.IInitializeSystem;
import com.tenio.engine.entitas.api.system.ITearDownSystem;
import com.tenio.engine.entitas.collector.Collector;
import com.tenio.engine.entitas.group.Group;
import com.tenio.engine.entitas.group.GroupEvent;
import com.tenio.engine.entitas.matcher.Matcher;
import com.tenio.engine.entitas.systems.ReactiveSystem;
import com.tenio.engine.entitas.systems.Systems;
import com.tenio.entitas.components.Position;
import com.tenio.entitas.utils.TestComponentIds;
import com.tenio.entitas.utils.TestEntity;
import com.tenio.entitas.utils.TestMatcher;

public class SystemsTest {

    @SuppressWarnings("rawtypes")
	private Context context;
    private Systems systems;
    private MoveSystem moveSystem;


    public EntityBaseFactory<Entity> factoryEntity() {
        return () -> {
            return new Entity();
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Context createTestPool() {
        return new Context(TestComponentIds.totalComponents, 0,
                new ContextInfo("Test", TestComponentIds.componentNames(),
                        TestComponentIds.componentTypes()), factoryEntity(), null);
    }

    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws Exception {
        systems = new Systems();
        context = createTestPool();
        moveSystem = new MoveSystem(context);
        moveSystem.flagExecute = false;
        moveSystem.flagInitialize = false;
        moveSystem.flagCleanup = false;
        moveSystem.flagTearDown = false;

    }

    @Test
    public void addTest() {
        systems.add(moveSystem);
        assertNotNull(moveSystem._group);

    }

    @Test
    public void add2Test() {
        @SuppressWarnings("unused")
		var contextsTest = new Object();
        systems.add(moveSystem);
        // assertEquals(contextsTest, moveSystem.contexts);

    }

    @SuppressWarnings("unchecked")
	@Test
    public void addReactiveSystemTest() {
        var reactiveSystem = new TestReactive(context);

        systems.add(reactiveSystem);
        systems.activateReactiveSystems();
        context.createEntity().
                addComponent(TestComponentIds.Position, new Position(100, 100));

        systems.execute(1);

        // assertTrue(reactiveSystem.flagExecute);

    }

    @SuppressWarnings({ "unused", "unchecked" })
	@Test
    public void addReactiveSystem2Test() {
        var reactiveSystem = new TestReactive(context);
        var contextsTest = new Object();

        systems.add(reactiveSystem);
        systems.deactivateReactiveSystems();
        context.createEntity().
                addComponent(TestComponentIds.Position, new Position(100, 100));

        systems.execute(1);

        context.createEntity().
                addComponent(TestComponentIds.Position, new Position(100, 100));

        systems.execute(1);

        // assertTrue(reactiveSystem.flagExecute);

    }

    @Test
    public void systemMethodsTest() {
        systems.add(moveSystem);
        systems.initialize();
        systems.execute(1);
        systems.cleanup();
        systems.tearDown();

        assertTrue(moveSystem.flagExecute);
        assertTrue(moveSystem.flagInitialize);
        assertTrue(moveSystem.flagCleanup);
        assertTrue(moveSystem.flagTearDown);

    }

    public class MoveSystem implements IExecuteSystem, IInitializeSystem, ICleanupSystem, ITearDownSystem {
        public Group<TestEntity> _group;
        public boolean flagExecute = false;
        public boolean flagInitialize = false;
        public boolean flagCleanup = false;
        public boolean flagTearDown = false;

        public MoveSystem(Context<TestEntity> context) {
            _group = context.getGroup(Matcher.allof(TestMatcher.View()));
        }

        @Override
        public void execute(float deltaTime) {
            flagExecute = true;
        }


        @Override
        public void initialize() {
            flagInitialize = true;
        }

        @Override
        public void cleanup() {
            flagCleanup = true;
        }

        @Override
        public void tearDown() {
            flagTearDown = true;
        }


    }

    public class TestReactive extends ReactiveSystem<TestEntity> {
        public boolean flagExecute = false;

        protected TestReactive(IContext<TestEntity> context) {
            super(context);
        }

        @SuppressWarnings("rawtypes")
		@Override
        public void _execute(List entities, float delta) {
            flagExecute = true;
        }

        @Override
        public void execute(float deltaTime) {

        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
        protected Collector<TestEntity> _getTrigger(IContext<TestEntity> context) {
            return new Collector(context.getGroup(Matcher.allof(TestMatcher.Position())), GroupEvent.ADDED);
        }

        @Override
        protected boolean _filter(TestEntity entity) {
            return true;
        }

        @Override
        public void activate() {

        }

        @Override
        public void deactivate() {

        }

        @Override
        public void clear() {

        }
    }

}
