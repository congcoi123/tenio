package com.tenio.examples.example4.world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.tenio.common.utilities.MathUtility;
import com.tenio.common.worker.WorkersPool;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.EMessage;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.InvertedAABBox2D;
import com.tenio.engine.physic2d.common.Path;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.window.Windows.PPoint;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utilities.CellSpacePartition;
import com.tenio.engine.physic2d.utilities.EntitiesRelationship;
import com.tenio.engine.physic2d.utilities.Geometry;
import com.tenio.engine.physic2d.utilities.Smoother;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.constant.SummingMethod;
import com.tenio.examples.example4.entities.Obstacle;
import com.tenio.examples.example4.entities.Vehicle;
import com.tenio.examples.example4.entities.Wall;

/**
 * All the environment data and methods for the Steering Behavior projects. This
 * class is the root of the project's update and render calls (excluding main of
 * course)
 */
public final class World extends AbstractHeartBeat {

	private final static int ONE_SECOND_EXPECT_SEND_PACKETS = 10;
	private final static float SEND_PACKETS_RATE = 1.0f / (float) ONE_SECOND_EXPECT_SEND_PACKETS;

	private final static int SAMPLE_RATE = 10;

	private ParamLoader __paramLoader = ParamLoader.getInstance();

	private final Vector2 __temp1 = Vector2.newInstance();
	private final InvertedAABBox2D __aabb = InvertedAABBox2D.newInstance();

	// a container of all the moving entities
	private final List<Vehicle> __vehicles = new ArrayList<Vehicle>(__paramLoader.NUM_AGENTS);
	// any obstacles
	private final List<BaseGameEntity> __obstacles = new ArrayList<BaseGameEntity>(__paramLoader.NUM_OBSTACLES);
	// container containing any walls in the environment
	private final List<Wall> __walls = new ArrayList<Wall>();
	private CellSpacePartition<Vehicle> __cellSpace;
	// any path we may create for the vehicles to follow
	private Path __path;
	// set true to pause the motion
	private boolean __pause;
	// local copy of client window dimensions
	private int __clientX, __clientY;
	// the position of the cross-hair
	private Vector2 __crosshair;
	// keeps track of the average FPS
	private float __fps;
	// flags to turn aids and obstacles etc on/off
	private boolean __enableShowWalls;
	private boolean __enableShowObstacles;
	private boolean __enableShowPath;
	private boolean __enableShowDetectionBox;
	private boolean __enableShowWanderCircle;
	private boolean __enableShowSensors;
	private boolean __enableShowSteeringForce;
	private boolean __enableShowFPS;
	private boolean __enableRenderNeighbors;
	private boolean __enableViewKeys;
	private boolean __enableShowCellSpaceInfo;

	private Smoother<Float> frameRateSmoother = new Smoother<Float>(SAMPLE_RATE, .0f);
	private float __sendingInterval = 0.0f;

	private WorkersPool __workersPool;

	private WorldListener __listener;

	public World(int cx, int cy) {
		super(cx, cy);

		__clientX = cx;
		__clientY = cy;
		__pause = false;
		__crosshair = Vector2.valueOf(getClientX() / 2, getClientX() / 2);
		__enableShowWalls = false;
		__enableShowObstacles = false;
		__enableShowPath = false;
		__enableShowWanderCircle = false;
		__enableShowSteeringForce = false;
		__enableShowSensors = false;
		__enableShowDetectionBox = false;
		__enableShowFPS = true;
		__fps = 0;
		__path = null;
		__enableRenderNeighbors = false;
		__enableViewKeys = false;
		__enableShowCellSpaceInfo = false;
		__workersPool = new WorkersPool("world", 150, 300);

		// setup the spatial subdivision class
		__cellSpace = new CellSpacePartition<Vehicle>((float) cx, (float) cy, __paramLoader.NUM_CELLS_X,
				__paramLoader.NUM_CELLS_Y, __paramLoader.NUM_AGENTS);

		float border = 30;
		__path = new Path(5, border, border, cx - border, cy - border, true);

		for (int a = 0; a < __paramLoader.NUM_AGENTS; ++a) {
			// determine a random starting position
			var SpawnPos = Vector2.valueOf(cx / 2 + MathUtility.randomClamped() * cx / 2,
					cy / 2 + MathUtility.randomClamped() * cy / 2);

			var pVehicle = new Vehicle(this, SpawnPos, // initial position
					MathUtility.randFloat() * MathUtility.TWO_PI, // start rotation
					Vector2.newInstance(), // velocity
					__paramLoader.VEHICLE_MASS, // mass
					__paramLoader.MAX_STEERING_FORCE, // max force
					__paramLoader.MAX_SPEED, // max velocity
					__paramLoader.MAX_TURN_RATE_PER_SECOND, // max turn rate
					__paramLoader.VEHICLE_SCALE); // scale

			pVehicle.setIndex(a);

			// Set the unique id for the big guy
			if (a == 0) {
				pVehicle.setId("dragon");
			}

			pVehicle.getBehavior().setFlockingOn();

			__vehicles.add(pVehicle);

			// add it to the cell subdivision
			__cellSpace.addEntity(pVehicle);
		}

		final boolean enableShoal = true;
		if (enableShoal) {
			__vehicles.get(__paramLoader.NUM_AGENTS - 1).getBehavior().setFlockingOff();
			__vehicles.get(__paramLoader.NUM_AGENTS - 1).setScale(Vector2.valueOf(10, 10));
			__vehicles.get(__paramLoader.NUM_AGENTS - 1).getBehavior().setWanderOn();
			__vehicles.get(__paramLoader.NUM_AGENTS - 1).setMaxSpeed(70);

			for (int i = 0; i < __paramLoader.NUM_AGENTS - 1; ++i) {
				__vehicles.get(i).getBehavior().setEvadeOn(__vehicles.get(__paramLoader.NUM_AGENTS - 1));
			}
		}

	}

	public void nonPenetrationContraint(Vehicle vehicle) {
		EntitiesRelationship.enforceNonPenetrationConstraint(vehicle, __vehicles);
	}

	public void tagVehiclesWithinViewRange(BaseGameEntity vehicle, float range) {
		EntitiesRelationship.tagNeighbors(vehicle, __vehicles, range);
	}

	public void tagObstaclesWithinViewRange(BaseGameEntity vehicle, float range) {
		EntitiesRelationship.tagNeighbors(vehicle, __obstacles, range);
	}

	public List<Wall> getWalls() {
		return __walls;
	}

	public CellSpacePartition<Vehicle> getCellSpace() {
		return __cellSpace;
	}

	public List<BaseGameEntity> getObstacles() {
		return __obstacles;
	}

	public List<Vehicle> getAgents() {
		return __vehicles;
	}

	public void togglePause() {
		__pause = !__pause;
	}

	public boolean isPaused() {
		return __pause;
	}

	public Vector2 getCrosshair() {
		return __crosshair;
	}

	public void setCrosshair(Vector2 vector) {
		__crosshair = vector;
	}

	public int getClientX() {
		return __clientX;
	}

	public int getClientY() {
		return __clientY;
	}

	public boolean isRenderWalls() {
		return __enableShowWalls;
	}

	public boolean isRenderObstacles() {
		return __enableShowObstacles;
	}

	public boolean isRenderPath() {
		return __enableShowPath;
	}

	public boolean isRenderDetectionBox() {
		return __enableShowDetectionBox;
	}

	public boolean isRenderWanderCircle() {
		return __enableShowWanderCircle;
	}

	public boolean isRenderSensors() {
		return __enableShowSensors;
	}

	public boolean isRenderSteeringForce() {
		return __enableShowSteeringForce;
	}

	public boolean isRenderFPS() {
		return __enableShowFPS;
	}

	public void toggleShowFPS() {
		__enableShowFPS = !__enableShowFPS;
	}

	public void toggleRenderNeighbors() {
		__enableRenderNeighbors = !__enableRenderNeighbors;
	}

	public boolean isRenderNeighbors() {
		return __enableRenderNeighbors;
	}

	public void toggleViewKeys() {
		__enableViewKeys = !__enableViewKeys;
	}

	public boolean isViewKeys() {
		return __enableViewKeys;
	}

	/**
	 * Creates some walls that form an enclosure for the steering agents. used to
	 * demonstrate several of the steering behaviors
	 */
	// FIXME
	@SuppressWarnings("unused")
	private void __createWalls() {
		// create the walls
		float bordersize = 20;
		float cornerSize = 0.2f;
		float vDist = __clientY - 2 * bordersize;
		float hDist = __clientX - 2 * bordersize;

		Vector2 walls[] = { Vector2.valueOf(hDist * cornerSize + bordersize, bordersize),
				Vector2.valueOf(__clientX - bordersize - hDist * cornerSize, bordersize),
				Vector2.valueOf(__clientX - bordersize, bordersize + vDist * cornerSize),
				Vector2.valueOf(__clientX - bordersize, __clientY - bordersize - vDist * cornerSize),
				Vector2.valueOf(__clientX - bordersize - hDist * cornerSize, __clientY - bordersize),
				Vector2.valueOf(hDist * cornerSize + bordersize, __clientY - bordersize),
				Vector2.valueOf(bordersize, __clientY - bordersize - vDist * cornerSize),
				Vector2.valueOf(bordersize, bordersize + vDist * cornerSize) };

		final int numWallVerts = walls.length;

		for (int w = 0; w < numWallVerts - 1; ++w) {
			__walls.add(new Wall(walls[w].x, walls[w].y, walls[w + 1].x, walls[w + 1].y));
		}

		__walls.add(new Wall(walls[numWallVerts - 1].x, walls[numWallVerts - 1].y, walls[0].x, walls[0].y));
	}

	/**
	 * Sets up the vector of obstacles with random positions and sizes. Makes sure
	 * the obstacles do not overlap
	 */
	// FIXME
	@SuppressWarnings("unused")
	private void __createObstacles() {
		// create a number of randomly sized tiddlywinks
		for (int o = 0; o < __paramLoader.NUM_OBSTACLES; ++o) {
			boolean overlapped = true;

			// keep creating tiddlywinks until we find one that doesn't overlap
			// any others.Sometimes this can get into an endless loop because the
			// obstacle has nowhere to fit. We test for this case and exit accordingly

			int numTrys = 0;
			int numAllowableTrys = 2000;

			while (overlapped) {
				numTrys++;

				if (numTrys > numAllowableTrys) {
					return;
				}
				int radius = MathUtility.randInt((int) __paramLoader.MIN_OBSTACLE_RADIUS,
						(int) __paramLoader.MAX_OBSTACLE_RADIUS);
				final int border = 10;
				final int minGapBetweenObstacles = 20;

				var ob = new Obstacle(MathUtility.randInt(radius + border, __clientX - radius - border),
						MathUtility.randInt(radius + border, __clientY - radius - 30 - border), radius);

				if (!EntitiesRelationship.isOverlapped((BaseGameEntity) ob, __obstacles, minGapBetweenObstacles)) {
					// its not overlapped so we can add it
					__obstacles.add(ob);
					overlapped = false;
				} else {
					ob = null;
				}
			}
		}
	}

	/**
	 * The user can set the position of the crosshair by right clicking the mouse.
	 * This method makes sure the click is not inside any enabled Obstacles and sets
	 * the position appropriately
	 */
	synchronized public void setCrosshair(PPoint p) {
		var proposedPosition = Vector2.valueOf((float) p.x, (float) p.y);

		// make sure it's not inside an obstacle
		var it = __obstacles.listIterator();
		while (it.hasNext()) {
			var curOb = it.next();
			if (Geometry.isPointInCircle(curOb.getPosition(), curOb.getBoundingRadius(), proposedPosition)) {
				return;
			}

		}
		__crosshair.x = (float) p.x;
		__crosshair.y = (float) p.y;
	}

	// ------------------------------ Render ----------------------------------
	// ------------------------------------------------------------------------
	@Override
	public void __onRender(Paint paint) {
		paint.enableOpaqueText(false);

		// render any walls
		paint.setPenColor(Color.BLACK);
		for (int w = 0; w < __walls.size(); ++w) {
			__walls.get(w).enableRenderNormal(true);
			__walls.get(w).render(paint); // true flag shows normals
		}

		// render any obstacles
		paint.setPenColor(Color.BLACK);

		for (int ob = 0; ob < __obstacles.size(); ++ob) {
			paint.drawCircle(__obstacles.get(ob).getPosition(), __obstacles.get(ob).getBoundingRadius());
			System.err.println(__obstacles.get(ob).getPosition().toString());
		}

		// render the agents
		for (int a = 0; a < __vehicles.size(); ++a) {
			__vehicles.get(a).render(paint);

			// render cell partitioning stuff
			if (__enableShowCellSpaceInfo && a == 0) {
				paint.setBgColor(null);

				__temp1.set(__vehicles.get(a).getPosition()).sub(__paramLoader.VIEW_DISTANCE,
						__paramLoader.VIEW_DISTANCE);
				__aabb.setLeft(__temp1.x);
				__aabb.setTop(__temp1.y);

				__temp1.set(__vehicles.get(a).getPosition()).add(__paramLoader.VIEW_DISTANCE,
						__paramLoader.VIEW_DISTANCE);
				__aabb.setRight(__temp1.x);
				__aabb.setBottom(__temp1.y);

				__aabb.render(paint);

				paint.setPenColor(Color.RED);

				getCellSpace().calculateNeighbors(__vehicles.get(a).getPosition(), __paramLoader.VIEW_DISTANCE);
				for (BaseGameEntity pV = getCellSpace().getFrontOfNeighbor(); !getCellSpace()
						.isEndOfNeighbors(); pV = getCellSpace().getNextOfNeighbor()) {
					paint.drawCircle(pV.getPosition(), pV.getBoundingRadius());
				}

				paint.setPenColor(Color.GREEN);
				paint.drawCircle(__vehicles.get(a).getPosition(), __paramLoader.VIEW_DISTANCE);
			}
		}

		boolean enableCrosshair = false;
		if (enableCrosshair) {
			// and finally the cross-hair
			paint.setPenColor(Color.RED);
			paint.drawCircle(__crosshair, 4);
			paint.drawLine(__crosshair.x - 8, __crosshair.y, __crosshair.x + 8, __crosshair.y);
			paint.drawLine(__crosshair.x, __crosshair.y - 8, __crosshair.x, __crosshair.y + 8);
			paint.drawTextAtPosition(5, getClientY() - 20, "Click to move crosshair");
		}

		paint.setTextColor(Color.GRAY);
		if (isRenderPath()) {
			paint.drawTextAtPosition((int) (getClientX() / 2.0f - 80), getClientY() - 20, "Press 'U' for random path");

			__path.render(paint);
		}

		if (isRenderFPS()) {
			paint.setTextColor(Color.GRAY);
			paint.drawTextAtPosition(5, getClientY() - 20, String.valueOf(1 / __fps));
		}

		if (__enableShowCellSpaceInfo) {
			__cellSpace.render(paint);
		}

	}

	@Override
	protected void __onCreate() {

	}

	/**
	 * Create a smoother to smooth the frame-rate
	 */
	@Override
	protected void __onUpdate(float delta) {

		__sendingInterval += delta;

		if (__sendingInterval >= SEND_PACKETS_RATE) {

			if (__listener != null) {
				var currentCCU = __listener.getCcu();
				setCCU(currentCCU);

				try {
					__workersPool.execute(() -> {
						List<Vehicle> vehicles = new ArrayList<Vehicle>();

						synchronized (__vehicles) {
							__vehicles.forEach(vehicle -> {
								vehicles.add(vehicle);
							});
						}

						// update the vehicles
						var iterator = vehicles.iterator();
						while (iterator.hasNext()) {
							var vehicle = iterator.next();
							__listener.updateVehiclePosition(vehicle);
						}

					}, "update-vehicles-position");
				} catch (Exception e) {
					error(e);
				}
			}

			__sendingInterval = 0;
		}

		__fps = frameRateSmoother.update(delta);

		for (int i = 0; i < __vehicles.size(); ++i) {
			__vehicles.get(i).update(delta);
		}
	}

	@Override
	protected void __onPause() {

	}

	@Override
	protected void __onResume() {

	}

	@Override
	protected void __onDispose() {
		__workersPool.waitUntilAllTasksFinished();
		__workersPool.stop();
	}

	@Override
	protected void __onAction1() {
		__vehicles.forEach(vehicle -> {
			vehicle.getBehavior().setSummingMethod(SummingMethod.PRIORITIZED);
		});
	}

	@Override
	protected void __onAction2() {
		__vehicles.forEach(vehicle -> {
			vehicle.getBehavior().setSummingMethod(SummingMethod.WEIGHTED_AVERAGE);
		});
	}

	@Override
	protected void __onAction3() {
		__vehicles.forEach(vehicle -> {
			vehicle.getBehavior().setSummingMethod(SummingMethod.DITHERED);
		});
	}

	@Override
	protected void __onMessage(EMessage message) {
		// System.out.println("World._onMessage(): " + message.getContent().toString());
		if (__listener == null) {
			return;
		}

		var name = (String) message.getContentByKey("id");
		var id = Integer.parseInt(name);
		try {
			__workersPool.execute(() -> {
				int entityId = id;
				if (id > __paramLoader.NUM_AGENTS - 1) {
					entityId = __paramLoader.NUM_AGENTS - 1;
				}

				List<Vehicle> neighbours = __getNeighboursOf(entityId);

				__listener.reponseVehicleNeighbours(name, neighbours, __getFPS());
			}, name);
		} catch (Exception e) {
			error(e);
		}
	}

	private List<Vehicle> __getNeighboursOf(final int index) {

		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		List<Vehicle> neighbours = new ArrayList<Vehicle>();

		synchronized (__vehicles) {
			__vehicles.forEach(vehicle -> {
				vehicles.add(vehicle);
			});
		}

		vehicles.forEach(vehicle -> {
			if (vehicles.get(index) != vehicle) {
				if (vehicles.get(index).getPosition().getDistanceValue(vehicle.getPosition()) < 50) {
					neighbours.add(vehicle);
				}
			}
		});

		return neighbours;
	}

	public void setListener(WorldListener listener) {
		__listener = listener;
	}

}
