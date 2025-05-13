package com.tenio.examples.example4.entity;

import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.physic2d.common.MoveableEntity;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utility.SmootherVector;
import com.tenio.engine.physic2d.utility.Transformation;
import com.tenio.examples.example4.behavior.SteeringBehavior;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.world.World;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Definition of a simple vehicle that uses steering behaviors.
 */
public final class Vehicle extends MoveableEntity implements Renderable {

  // a pointer to the world data. So a vehicle can access any obstacle,
  // path, wall or agent data
  private final World world;
  // the steering behavior class
  private final SteeringBehavior steeringBehavior;
  // some steering behaviors give jerky looking movement. The
  // following members are used to smooth the vehicle's heading
  private final SmootherVector<Vector2> headingSmoother;
  private final Vector2 smoothedHeading = Vector2.newInstance();
  // buffer for the vehicle shape
  private final List<Vector2> shape = new ArrayList<>();
  // this vector represents the average of the vehicle's heading
  // vector smoothed over the last few frames
  private float smoothedHeadingX;
  private float smoothedHeadingY;
  // when true, smoothing is active
  private boolean enableSmoothing;
  // keeps a track of the most recent update time. (some
  // steering behaviors make use of this - see Wander)
  private float timeElapsed;
  // index of vehicle in the list
  private int index;

  public Vehicle(World world, Vector2 position, float rotation, Vector2 velocity, float mass,
                 float maxForce,
                 float maxSpeed, float maxTurnRate, float scale) {
    super(position, scale, velocity, maxSpeed,
        Vector2.valueOf((float) Math.sin(rotation), (float) -Math.cos(rotation)), mass,
        Vector2.valueOf(scale, scale), maxTurnRate, maxForce);

    this.world = world;
    smoothedHeadingX = 0;
    smoothedHeadingY = 0;
    enableSmoothing = false;
    timeElapsed = 0;

    createShape();

    // set up the steering behavior class
    steeringBehavior = new SteeringBehavior(this);

    // set up the smoother
    headingSmoother =
        new SmootherVector<>(ParamLoader.getInstance().NUM_SAMPLES_FOR_SMOOTHING,
            Vector2.newInstance());
  }

  /**
   * Fills the vehicle's shape buffer with its vertices.
   */
  private void createShape() {
    final int numVehicleVerts = 3;

    Vector2[] vehicle = {Vector2.valueOf(-1.0f, 0.6f), Vector2.valueOf(1.0f, 0.0f),
        Vector2.valueOf(-1.0f, -0.6f)};

    // set up the vertex buffers and calculate the bounding radius
    shape.addAll(Arrays.asList(vehicle).subList(0, numVehicleVerts));
  }

  public SteeringBehavior getBehavior() {
    return steeringBehavior;
  }

  public World getWorld() {
    return world;
  }

  public Vector2 getSmoothedHeading() {
    return smoothedHeading.set(smoothedHeadingX, smoothedHeadingY);
  }

  public void setSmoothedHeading(Vector2 smoothed) {
    setSmoothedHeading(smoothed.x, smoothed.y);
  }

  public void setSmoothedHeading(float x, float y) {
    smoothedHeadingX = x;
    smoothedHeadingY = y;
  }

  public boolean isSmoothing() {
    return enableSmoothing;
  }

  public void enableSmoothing(boolean enabled) {
    enableSmoothing = enabled;
  }

  public void toggleSmoothing() {
    enableSmoothing = !enableSmoothing;
  }

  /**
   * @return time elapsed from last update
   */
  public float getTimeElapsed() {
    return timeElapsed;
  }

  public int getASCIIValueOfString(String question) {
    int result = 0;

    var chars = question.toCharArray();
    for (char aChar : chars) {
      result += aChar;
    }

    return result;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * Updates the vehicle's position and orientation from a series of steering
   * behaviors.
   */
  @Override
  public void update(float delta) {
    // update the time elapsed
    timeElapsed = delta;

    // keep a record of its old position so we can update its cell later
    // in this method
    var oldPos = getPosition();

    // calculate the combined force from each steering behavior in the
    // vehicle's list
    var steeringForce = getBehavior().calculateAccumulate();
    // Vector2 steeringForce = new Vector2(1, 1);

    // Acceleration = Force/Mass
    var acceleration = steeringForce.div(getMass());

    // update velocity
    var velocity = acceleration.mul(delta).add(getVelocity());

    // make sure vehicle does not exceed maximum velocity
    velocity.truncate(getMaxSpeed());
    setVelocity(velocity);

    // update the position
    var position = velocity.mul(delta).add(getPosition());
    setPosition(position);

    // update the heading if the vehicle has a non-zero velocity
    if (getVelocity().getLengthSqr() > 0.00000001) {
      setHeading(getVelocity().normalize());
    }

    // treat the screen as an endless screen
    var around =
        Transformation.wrapAround(getPosition(), world.getClientX(), world.getClientY());
    setPosition(around);

    // update the vehicle's current cell if space partitioning is turned on
    if (getBehavior().isSpacePartitioning()) {
      getWorld().getCellSpace().updateEntity(this, oldPos);
    }

    if (isSmoothing()) {
      setSmoothedHeading(headingSmoother.update(getHeading()));
    }
  }

  @Override
  public void render(Paint paint) {
    // render neighboring vehicles in different colors if requested
    if (world.isRenderNeighbors()) {
      if (Objects.equals(getId(), "dragon")) {
        paint.setPenColor(Color.RED);
      } else if (isTagged()) {
        paint.setPenColor(Color.GREEN);
      } else {
        paint.setPenColor(Color.BLUE);
      }
    } else {
      paint.setPenColor(Color.BLUE);
    }

    if (getBehavior().isInterposeOn()) {
      paint.setPenColor(Color.RED);
    }

    if (getBehavior().isHideOn()) {
      paint.setPenColor(Color.GREEN);
    }

    // a vector to hold the transformed vertices
    List<Vector2> shape;

    if (isSmoothing()) {
      shape = Transformation.pointsToWorldSpace(this.shape, getPosition(), getSmoothedHeading(),
          getSmoothedHeading().perpendicular(), getScale());
    } else {
      shape = Transformation.pointsToWorldSpace(this.shape, getPosition(), getHeading(), getSide(),
          getScale());
    }
    paint.drawClosedShape(shape);

    // render any visual aids / and or user options
    if (world.isViewKeys()) {
      getBehavior().render(paint);
    }
  }

  @Override
  public boolean handleMessage(Telegram msg) {
    return false;
  }
}
