package com.tenio.examples.example4.entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.fsm.entity.Telegram;
import com.tenio.engine.physic2d.common.MoveableEntity;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utilities.SmootherVector;
import com.tenio.engine.physic2d.utilities.Transformation;
import com.tenio.examples.example4.behavior.SteeringBehavior;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.world.World;

/**
 * Definition of a simple vehicle that uses steering behaviors
 */
public final class Vehicle extends MoveableEntity implements Renderable {

	// a pointer to the world data. So a vehicle can access any obstacle,
	// path, wall or agent data
	private World __world;
	// the steering behavior class
	private SteeringBehavior __behavior;
	// some steering behaviors give jerky looking movement. The
	// following members are used to smooth the vehicle's heading
	private SmootherVector<Vector2> __headingSmoother;
	// this vector represents the average of the vehicle's heading
	// vector smoothed over the last few frames
	private float __smoothedHeadingX;
	private float __smoothedHeadingY;
	private Vector2 __smoothedHeading = Vector2.newInstance();
	// when true, smoothing is active
	private boolean __enableSmoothing;
	// keeps a track of the most recent update time. (some of the
	// steering behaviors make use of this - see Wander)
	private float __timeElapsed;
	// buffer for the vehicle shape
	private List<Vector2> __shape = new ArrayList<Vector2>();
	// index of vehicle in the list
	private int __index;

	public Vehicle(World world, Vector2 position, float rotation, Vector2 velocity, float mass, float maxForce,
			float maxSpeed, float maxTurnRate, float scale) {
		super(position, scale, velocity, maxSpeed,
				Vector2.valueOf((float) Math.sin(rotation), (float) -Math.cos(rotation)), mass,
				Vector2.valueOf(scale, scale), maxTurnRate, maxForce);

		__world = world;
		__smoothedHeadingX = 0;
		__smoothedHeadingY = 0;
		__enableSmoothing = false;
		__timeElapsed = 0;

		__createShape();

		// set up the steering behavior class
		__behavior = new SteeringBehavior(this);

		// set up the smoother
		__headingSmoother = new SmootherVector<Vector2>(ParamLoader.getInstance().NUM_SAMPLES_FOR_SMOOTHING,
				Vector2.newInstance());

	}

	/**
	 * Fills the vehicle's shape buffer with its vertices
	 */
	private void __createShape() {
		final int numVehicleVerts = 3;

		Vector2 vehicle[] = { Vector2.valueOf(-1.0f, 0.6f), Vector2.valueOf(1.0f, 0.0f),
				Vector2.valueOf(-1.0f, -0.6f) };

		// setup the vertex buffers and calculate the bounding radius
		for (int vtx = 0; vtx < numVehicleVerts; ++vtx) {
			__shape.add(vehicle[vtx]);
		}
	}

	public SteeringBehavior getBehavior() {
		return __behavior;
	}

	public World getWorld() {
		return __world;
	}

	public Vector2 getSmoothedHeading() {
		return __smoothedHeading.set(__smoothedHeadingX, __smoothedHeadingY);
	}

	public void setSmoothedHeading(float x, float y) {
		__smoothedHeadingX = x;
		__smoothedHeadingY = y;
	}

	public void setSmoothedHeading(Vector2 smoothed) {
		setSmoothedHeading(smoothed.x, smoothed.y);
	}

	public boolean isSmoothing() {
		return __enableSmoothing;
	}

	public void enableSmoothing(boolean enabled) {
		__enableSmoothing = enabled;
	}

	public void toggleSmoothing() {
		__enableSmoothing = !__enableSmoothing;
	}

	/**
	 * @return time elapsed from last update
	 */
	public float getTimeElapsed() {
		return __timeElapsed;
	}

	public int getASCIIValueOfString(String question) {
		int result = 0;

		var chars = question.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			result += (int) chars[i];
		}

		return result;
	}

	public int getIndex() {
		return __index;
	}

	public void setIndex(int index) {
		__index = index;
	}

	/**
	 * Updates the vehicle's position and orientation from a series of steering
	 * behaviors
	 */
	@Override
	public void update(float delta) {
		// update the time elapsed
		__timeElapsed = delta;

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

		// update the heading if the vehicle has a non zero velocity
		if (getVelocity().getLengthSqr() > 0.00000001) {
			setHeading(getVelocity().normalize());
		}

		// treat the screen as a endless screen
		var around = Transformation.wrapAround(getPosition(), __world.getClientX(), __world.getClientY());
		setPosition(around);

		// update the vehicle's current cell if space partitioning is turned on
		if (getBehavior().isSpacePartitioning()) {
			getWorld().getCellSpace().updateEntity(this, oldPos);
		}

		if (isSmoothing()) {
			setSmoothedHeading(__headingSmoother.update(getHeading()));
		}

	}

	@Override
	public void render(Paint paint) {
		// render neighboring vehicles in different colors if requested
		if (__world.isRenderNeighbors()) {
			if (getId() == "dragon") {
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
			shape = Transformation.pointsToWorldSpace(__shape, getPosition(), getSmoothedHeading(),
					getSmoothedHeading().perpendicular(), getScale());
		} else {
			shape = Transformation.pointsToWorldSpace(__shape, getPosition(), getHeading(), getSide(), getScale());
		}
		paint.drawClosedShape(shape);

		// render any visual aids / and or user options
		if (__world.isViewKeys()) {
			getBehavior().render(paint);
		}
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		return false;
	}

}
