package com.tenio.examples.example4.world;

import com.tenio.common.utility.MathUtility;
import com.tenio.common.worker.WorkerPool;
import com.tenio.engine.heartbeat.AbstractHeartBeat;
import com.tenio.engine.message.ExtraMessage;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.InvertedAabbBox2D;
import com.tenio.engine.physic2d.common.Path;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.window.Windows.P2Point;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utility.CellSpacePartition;
import com.tenio.engine.physic2d.utility.EntitiesRelationship;
import com.tenio.engine.physic2d.utility.Geometry;
import com.tenio.engine.physic2d.utility.Smoother;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.constant.Example4Constant;
import com.tenio.examples.example4.constant.SummingMethod;
import com.tenio.examples.example4.entity.Obstacle;
import com.tenio.examples.example4.entity.Vehicle;
import com.tenio.examples.example4.entity.Wall;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * All the environment data and methods for the Steering Behavior projects. This
 * class is the root of the project's update and render calls (excluding main of
 * course).
 */
public final class World extends AbstractHeartBeat {

  private final static int ONE_SECOND_EXPECT_SEND_PACKETS = 10;
  private final static float SEND_PACKETS_RATE = 1.0f / (float) ONE_SECOND_EXPECT_SEND_PACKETS;
  private final static int SAMPLE_RATE = 10;

  private final InvertedAabbBox2D aabb = InvertedAabbBox2D.newInstance();
  // container containing any walls in the environment
  private final List<Wall> walls = new ArrayList<>();
  private final ParamLoader paramLoader = ParamLoader.getInstance();
  // a container of all the moving entities
  private final List<Vehicle> vehicles = new ArrayList<>(paramLoader.NUM_AGENTS);
  // any obstacles
  private final List<BaseGameEntity> obstacles =
      new ArrayList<>(paramLoader.NUM_OBSTACLES);
  private final CellSpacePartition<Vehicle> cellSpace;
  // local copy of client window dimensions
  private final int clientX;
  private final int clientY;
  // flags to turn aids and obstacles etc. on/off
  private final boolean enableShowWalls;
  private final boolean enableShowObstacles;
  private final boolean enableShowPath;
  private final boolean enableShowDetectionBox;
  private final boolean enableShowWanderCircle;
  private final boolean enableShowSensors;
  private final boolean enableShowSteeringForce;
  private final boolean enableShowCellSpaceInfo;
  private final Smoother<Float> frameRateSmoother = new Smoother<>(SAMPLE_RATE, .0f);
  private final WorkerPool workerPool;
  // any path we may create for the vehicles to follow
  private Path path;
  // set true to pause the motion
  private boolean pause;
  // the position of the cross-hair
  private Vector2 crosshair;
  // keeps track of the average FPS
  private float fps;
  private boolean enableShowFPS;
  private boolean enableRenderNeighbors;
  private boolean enableViewKeys;
  private float sendingInterval = 0.0f;
  private WorldListener worldListener;

  public World(int cx, int cy) {
    super(cx, cy);

    clientX = cx;
    clientY = cy;
    pause = false;
    crosshair = Vector2.valueOf((float) getClientX() / 2, (float) getClientX() / 2);
    enableShowWalls = false;
    enableShowObstacles = false;
    enableShowPath = false;
    enableShowWanderCircle = false;
    enableShowSteeringForce = false;
    enableShowSensors = false;
    enableShowDetectionBox = false;
    enableShowFPS = true;
    fps = 0;
    path = null;
    enableRenderNeighbors = false;
    enableViewKeys = false;
    enableShowCellSpaceInfo = false;
    workerPool = new WorkerPool("world", Example4Constant.NUMBER_OF_THREADS_WORKER_POOL,
        Example4Constant.NUMBER_OF_TASKS_PER_THREAD_WORKER_POOL);

    // set up the spatial subdivision class
    cellSpace = new CellSpacePartition<>((float) cx, (float) cy, paramLoader.NUM_CELLS_X,
        paramLoader.NUM_CELLS_Y, paramLoader.NUM_AGENTS);

    float border = 30;
    path = new Path(5, border, border, cx - border, cy - border, true);

    for (int a = 0; a < paramLoader.NUM_AGENTS; ++a) {
      // determine a random starting position
      var SpawnPos = Vector2.valueOf((float) cx / 2 + MathUtility.randomClamped() * cx / 2,
          (float) cy / 2 + MathUtility.randomClamped() * cy / 2);

      var pVehicle = new Vehicle(this, SpawnPos, // initial position
          MathUtility.randFloat() * MathUtility.TWO_PI, // start rotation
          Vector2.newInstance(), // velocity
          paramLoader.VEHICLE_MASS, // mass
          paramLoader.MAX_STEERING_FORCE, // max force
          paramLoader.MAX_SPEED, // max velocity
          paramLoader.MAX_TURN_RATE_PER_SECOND, // max turn rate
          paramLoader.VEHICLE_SCALE); // scale

      pVehicle.setIndex(a);

      // Set the unique id for the big guy
      if (a == 0) {
        pVehicle.setId("dragon");
      }

      pVehicle.getBehavior().setFlockingOn();

      vehicles.add(pVehicle);

      // add it to the cell subdivision
      cellSpace.addEntity(pVehicle);
    }

    final boolean enableShoal = true;
    if (enableShoal) {
      vehicles.get(paramLoader.NUM_AGENTS - 1).getBehavior().setFlockingOff();
      vehicles.get(paramLoader.NUM_AGENTS - 1).setScale(Vector2.valueOf(10, 10));
      vehicles.get(paramLoader.NUM_AGENTS - 1).getBehavior().setWanderOn();
      vehicles.get(paramLoader.NUM_AGENTS - 1).setMaxSpeed(70);

      for (int i = 0; i < paramLoader.NUM_AGENTS - 1; ++i) {
        vehicles.get(i).getBehavior().setEvadeOn(vehicles.get(paramLoader.NUM_AGENTS - 1));
      }
    }
  }

  public void nonPenetrationConstraint(Vehicle vehicle) {
    EntitiesRelationship.enforceNonPenetrationConstraint(vehicle, vehicles);
  }

  public void tagVehiclesWithinViewRange(BaseGameEntity vehicle, float range) {
    EntitiesRelationship.tagNeighbors(vehicle, vehicles, range);
  }

  public void tagObstaclesWithinViewRange(BaseGameEntity vehicle, float range) {
    EntitiesRelationship.tagNeighbors(vehicle, obstacles, range);
  }

  public List<Wall> getWalls() {
    return walls;
  }

  public CellSpacePartition<Vehicle> getCellSpace() {
    return cellSpace;
  }

  public List<BaseGameEntity> getObstacles() {
    return obstacles;
  }

  public List<Vehicle> getAgents() {
    return vehicles;
  }

  public void togglePause() {
    pause = !pause;
  }

  public boolean isPaused() {
    return pause;
  }

  public Vector2 getCrosshair() {
    return crosshair;
  }

  public void setCrosshair(Vector2 vector) {
    crosshair = vector;
  }

  /**
   * The user can set the position of the crosshair by right-clicking the mouse.
   * This method makes sure the click is not inside any enabled Obstacles and sets
   * the position appropriately.
   */
  synchronized public void setCrosshair(P2Point p) {
    var proposedPosition = Vector2.valueOf((float) p.x, (float) p.y);

    // make sure it's not inside an obstacle
    var it = obstacles.listIterator();
    while (it.hasNext()) {
      var curOb = it.next();
      if (Geometry.isPointInCircle(curOb.getPosition(), curOb.getBoundingRadius(),
          proposedPosition)) {
        return;
      }

    }
    crosshair.x = (float) p.x;
    crosshair.y = (float) p.y;
  }

  public int getClientX() {
    return clientX;
  }

  public int getClientY() {
    return clientY;
  }

  public boolean isRenderWalls() {
    return enableShowWalls;
  }

  public boolean isRenderObstacles() {
    return enableShowObstacles;
  }

  public boolean isRenderPath() {
    return enableShowPath;
  }

  public boolean isRenderDetectionBox() {
    return enableShowDetectionBox;
  }

  public boolean isRenderWanderCircle() {
    return enableShowWanderCircle;
  }

  public boolean isRenderSensors() {
    return enableShowSensors;
  }

  public boolean isRenderSteeringForce() {
    return enableShowSteeringForce;
  }

  public boolean isRenderFPS() {
    return enableShowFPS;
  }

  public void toggleShowFPS() {
    enableShowFPS = !enableShowFPS;
  }

  public void toggleRenderNeighbors() {
    enableRenderNeighbors = !enableRenderNeighbors;
  }

  public boolean isRenderNeighbors() {
    return enableRenderNeighbors;
  }

  public void toggleViewKeys() {
    enableViewKeys = !enableViewKeys;
  }

  public boolean isViewKeys() {
    return enableViewKeys;
  }

  /**
   * Creates some walls that form an enclosure for the steering agents. used to
   * demonstrate several of the steering behaviors.
   */
  @SuppressWarnings("unused")
  private void createWalls() {
    // create the walls
    float bordersize = 20;
    float cornerSize = 0.2f;
    float vDist = clientY - 2 * bordersize;
    float hDist = clientX - 2 * bordersize;

    Vector2[] walls = {Vector2.valueOf(hDist * cornerSize + bordersize, bordersize),
        Vector2.valueOf(clientX - bordersize - hDist * cornerSize, bordersize),
        Vector2.valueOf(clientX - bordersize, bordersize + vDist * cornerSize),
        Vector2.valueOf(clientX - bordersize, clientY - bordersize - vDist * cornerSize),
        Vector2.valueOf(clientX - bordersize - hDist * cornerSize, clientY - bordersize),
        Vector2.valueOf(hDist * cornerSize + bordersize, clientY - bordersize),
        Vector2.valueOf(bordersize, clientY - bordersize - vDist * cornerSize),
        Vector2.valueOf(bordersize, bordersize + vDist * cornerSize)};

    final int numWallVerts = walls.length;

    for (int w = 0; w < numWallVerts - 1; ++w) {
      this.walls.add(new Wall(walls[w].x, walls[w].y, walls[w + 1].x, walls[w + 1].y));
    }

    this.walls.add(
        new Wall(walls[numWallVerts - 1].x, walls[numWallVerts - 1].y, walls[0].x, walls[0].y));
  }

  /**
   * Sets up the vector of obstacles with random positions and sizes. Makes sure
   * the obstacles do not overlap
   */
  @SuppressWarnings("unused")
  private void createObstacles() {
    // create a number of randomly sized tiddlywinks
    for (int o = 0; o < paramLoader.NUM_OBSTACLES; ++o) {
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
        int radius = MathUtility.randInt((int) paramLoader.MIN_OBSTACLE_RADIUS,
            (int) paramLoader.MAX_OBSTACLE_RADIUS);
        final int border = 10;
        final int minGapBetweenObstacles = 20;

        var ob = new Obstacle(MathUtility.randInt(radius + border, clientX - radius - border),
            MathUtility.randInt(radius + border, clientY - radius - 30 - border), radius);

        if (!EntitiesRelationship.isOverlapped(ob, obstacles,
            minGapBetweenObstacles)) {
          // it's not overlapped so we can add it
          obstacles.add(ob);
          overlapped = false;
        }
      }
    }
  }

  // ------------------------------ Render ----------------------------------
  // ------------------------------------------------------------------------
  @Override
  public void onRender(Paint paint) {
    paint.enableOpaqueText(false);

    // render any walls
    paint.setPenColor(Color.BLACK);
    for (Wall wall : walls) {
      wall.enableRenderNormal(true);
      wall.render(paint); // true flag shows normals
    }

    // render any obstacles
    paint.setPenColor(Color.BLACK);

    for (BaseGameEntity obstacle : obstacles) {
      paint.drawCircle(obstacle.getPosition(), obstacle.getBoundingRadius());
    }

    // render the agents
    for (int a = 0; a < vehicles.size(); ++a) {
      vehicles.get(a).render(paint);

      // render cell partitioning stuff
      if (enableShowCellSpaceInfo && a == 0) {
        paint.setBgColor(null);

        var temp =
            Vector2.newInstance().set(vehicles.get(a).getPosition()).sub(paramLoader.VIEW_DISTANCE,
                paramLoader.VIEW_DISTANCE);
        aabb.setLeft(temp.x);
        aabb.setTop(temp.y);

        temp.set(vehicles.get(a).getPosition()).add(paramLoader.VIEW_DISTANCE,
            paramLoader.VIEW_DISTANCE);
        aabb.setRight(temp.x);
        aabb.setBottom(temp.y);

        aabb.render(paint);

        paint.setPenColor(Color.RED);

        getCellSpace().calculateNeighbors(vehicles.get(a).getPosition(),
            paramLoader.VIEW_DISTANCE);
        for (BaseGameEntity pV = getCellSpace().getFrontOfNeighbor(); !getCellSpace()
            .isEndOfNeighbors(); pV = getCellSpace().getNextOfNeighbor()) {
          paint.drawCircle(pV.getPosition(), pV.getBoundingRadius());
        }

        paint.setPenColor(Color.GREEN);
        paint.drawCircle(vehicles.get(a).getPosition(), paramLoader.VIEW_DISTANCE);
      }
    }

    boolean enableCrosshair = false;
    if (enableCrosshair) {
      // and finally the cross-hair
      paint.setPenColor(Color.RED);
      paint.drawCircle(crosshair, 4);
      paint.drawLine(crosshair.x - 8, crosshair.y, crosshair.x + 8, crosshair.y);
      paint.drawLine(crosshair.x, crosshair.y - 8, crosshair.x, crosshair.y + 8);
      paint.drawTextAtPosition(5, getClientY() - 20, "Click to move crosshair");
    }

    paint.setTextColor(Color.GRAY);
    if (isRenderPath()) {
      paint.drawTextAtPosition((int) (getClientX() / 2.0f - 80), getClientY() - 20,
          "Press 'U' for random path");

      path.render(paint);
    }

    if (isRenderFPS()) {
      paint.setTextColor(Color.GRAY);
      paint.drawTextAtPosition(5, getClientY() - 20, String.valueOf(1 / fps));
    }

    if (enableShowCellSpaceInfo) {
      cellSpace.render(paint);
    }

  }

  @Override
  protected void onCreate() {
  }

  /**
   * Create a smoother to smooth the frame-rate
   */
  @Override
  protected void onUpdate(float delta) {

    sendingInterval += delta;

    if (sendingInterval >= SEND_PACKETS_RATE) {

      if (worldListener != null) {
        var currentCCU = worldListener.getCcu();
        setCcu(currentCCU);

        try {
          workerPool.execute(() -> {
            List<Vehicle> vehicles;

            synchronized (this.vehicles) {
              vehicles = new ArrayList<>(this.vehicles);
            }

            // update the vehicles
            var iterator = vehicles.iterator();
            while (iterator.hasNext()) {
              var vehicle = iterator.next();
              worldListener.updateVehiclePosition(vehicle);
            }

          }, "update-vehicles-position");
        } catch (Exception exception) {
          if (isErrorEnabled()) {
            error(exception);
          }
        }
      }

      sendingInterval = 0;
    }

    fps = frameRateSmoother.update(delta);

    for (Vehicle vehicle : vehicles) {
      vehicle.update(delta);
    }
  }

  @Override
  protected void onPause() {

  }

  @Override
  protected void onResume() {

  }

  @Override
  protected void onDispose() {
    workerPool.waitUntilAllTasksFinished();
    workerPool.stop();
  }

  @Override
  protected void onAction1() {
    vehicles.forEach(vehicle -> vehicle.getBehavior().setSummingMethod(SummingMethod.PRIORITIZED));
  }

  @Override
  protected void onAction2() {
    vehicles.forEach(vehicle -> vehicle.getBehavior().setSummingMethod(SummingMethod.WEIGHTED_AVERAGE));
  }

  @Override
  protected void onAction3() {
    vehicles.forEach(vehicle -> vehicle.getBehavior().setSummingMethod(SummingMethod.DITHERED));
  }

  @Override
  protected void onMessage(ExtraMessage message) {
    if (worldListener == null) {
      return;
    }

    var name = (String) message.getContentByKey("id");
    var id = Integer.parseInt(name);
    try {
      workerPool.execute(() -> {
        int entityId = Math.min(id, paramLoader.NUM_AGENTS - 1);
        List<Vehicle> neighbours = getNeighboursOf(entityId);
        worldListener.responseVehicleNeighbours(name, neighbours, getFps());
      }, name);
    } catch (Exception exception) {
      if (isErrorEnabled()) {
        error(exception);
      }
    }
  }

  private List<Vehicle> getNeighboursOf(final int index) {

    List<Vehicle> vehicles;
    List<Vehicle> neighbours = new ArrayList<>();

    synchronized (this.vehicles) {
      vehicles = new ArrayList<>(this.vehicles);
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
    worldListener = listener;
  }
}
