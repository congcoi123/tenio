package com.tenio.examples.example4.behavior;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.Path;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utilities.Geometry;
import com.tenio.engine.physic2d.utilities.Transformation;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.constant.Behavior;
import com.tenio.examples.example4.constant.Deceleration;
import com.tenio.examples.example4.constant.SummingMethod;
import com.tenio.examples.example4.entities.Vehicle;
import com.tenio.examples.example4.entities.Wall;

/**
 * This class is used to encapsulate steering behaviors for a vehicle
 * 
 * @see Vehicle
 */
public final class SteeringBehavior implements Renderable {

	// the radius of the constraining circle for the wander behavior
	public static final float WANDER_RADIUS = 1.2f;
	// distance the wander circle is projected in front of the agent
	public static final float WANDER_DISTANCE = 2;
	// the maximum amount of displacement along the circle each frame
	public static final float WANDER_JITTER_PER_SECOND = 80;
	// used in path following
	public static final float WAYPOINT_SEEK_DISTANCE = 20;

	private ParamLoader __paramLoader = ParamLoader.getInstance();

	private final Vector2 __temp1 = Vector2.newInstance();
	private final Vector2 __temp2 = Vector2.newInstance();
	private final Vector2 __temp3 = Vector2.newInstance();
	private final Vector2 __temp4 = Vector2.newInstance();
	private final Vector2 __temp5 = Vector2.newInstance();

	// a pointer to the owner of this instance
	private Vehicle __vehicle;
	// the steering force created by the combined effect of all
	// the selected behaviors
	private Vector2 __steeringForce = Vector2.newInstance();
	// these can be used to keep track of friends, pursuers, or prey
	private Vehicle __targetAgent1;
	private Vehicle __targetAgent2;
	// length of the 'detection box' utilized in obstacle avoidance
	private float __detectBoxLength;
	// a vertex buffer to contain the feelers for wall avoidance
	private List<Vector2> __sensors;
	// the length of the 'feeler/s' used in wall detection
	private float __wallDetectionSensorLength;
	// the current position on the wander circle the agent is
	// attempting to steer towards
	private Vector2 __wanderTarget;
	// explained above
	private float __wanderJitter;
	private float __wanderRadius;
	private float __wanderDistance;
	// multipliers. These can be adjusted to effect strength of the
	// appropriate behavior. Useful to get flocking the way you require
	// for example.
	private float __weightSeparation;
	private float __weightCohesion;
	private float __weightAlignment;
	private float __weightWander;
	private float __weightObstacleAvoidance;
	private float __weightWallAvoidance;
	private float __weightSeek;
	private float __weightFlee;
	private float __weightArrive;
	private float __weightPursuit;
	private float __weightOffsetPursuit;
	private float __weightInterpose;
	private float __weightHide;
	private float __weightEvade;
	private float __weightFollowPath;
	// how far the agent can 'see'
	// private float __agentViewDistance;
	// pointer to any current path
	private Path __path;
	// the distance (squared) a vehicle has to be from a path way-point before
	// it starts seeking to the next way-point
	private float __waypointSeekDistanceSqr;
	// any offset used for formations or offset pursuit
	private Vector2 __offset;
	// binary flags to indicate whether or not a behavior should be active
	private int __behaviorFlag;
	// default
	private Deceleration __deceleration;
	// is cell space partitioning to be used or not?
	private boolean __enableCellSpace;
	// what type of method is used to sum any active behavior
	private SummingMethod __summingMethod;
	// a vertex buffer rqd for drawing the detection box
	private List<Vector2> __detectBox = new ArrayList<Vector2>(4);

	public SteeringBehavior(Vehicle agent) {

		__vehicle = agent;
		__behaviorFlag = 0;
		__detectBoxLength = __paramLoader.MIN_DETECTION_BOX_LENGTH;
		__weightCohesion = __paramLoader.COHESION_WEIGHT;
		__weightAlignment = __paramLoader.ALIGNMENT_WEIGHT;
		__weightSeparation = __paramLoader.SEPARATION_WEIGHT;
		__weightObstacleAvoidance = __paramLoader.OBSTACLE_AVOIDANCE_WEIGHT;
		__weightWander = __paramLoader.WANDER_WEIGHT;
		__weightWallAvoidance = __paramLoader.WALL_AVOIDANCE_WEIGHT;
		// __agentViewDistance = __paramLoader.VIEW_DISTANCE;
		__wallDetectionSensorLength = __paramLoader.WALL_DETECTION_FEELER_LENGTH;
		__sensors = new ArrayList<Vector2>(3);
		__deceleration = Deceleration.NORMAL;
		__targetAgent1 = null;
		__targetAgent2 = null;
		__wanderDistance = WANDER_DISTANCE;
		__wanderJitter = WANDER_JITTER_PER_SECOND;
		__wanderRadius = WANDER_RADIUS;
		__waypointSeekDistanceSqr = WAYPOINT_SEEK_DISTANCE * WAYPOINT_SEEK_DISTANCE;
		__weightSeek = __paramLoader.SEEK_WEIGHT;
		__weightFlee = __paramLoader.FLEE_WEIGHT;
		__weightArrive = __paramLoader.ARRIVE_WEIGHT;
		__weightPursuit = __paramLoader.PURSUIT_WEIGHT;
		__weightOffsetPursuit = __paramLoader.OFFSET_PURSUIT_WEIGHT;
		__weightInterpose = __paramLoader.INTERPOSE_WEIGHT;
		__weightHide = __paramLoader.HIDE_WEIGHT;
		__weightEvade = __paramLoader.EVADE_WEIGHT;
		__weightFollowPath = __paramLoader.FOLLOW_PATH_WEIGHT;
		__enableCellSpace = false;
		__summingMethod = SummingMethod.PRIORITIZED;

		// stuff for the wander behavior
		float theta = MathUtility.randFloat() * MathUtility.TWO_PI;

		// create a vector to a target position on the wander circle
		__wanderTarget = Vector2.valueOf((float) (__wanderRadius * Math.cos(theta)),
				(float) (__wanderRadius * Math.sin(theta)));

		// create a Path
		__path = new Path();
		__path.enableLoop(true);

	}

	// this function tests if a specific bit of m_iFlags is set
	private boolean __isBehavior(Behavior behavior) {
		return (__behaviorFlag & behavior.get()) == behavior.get();
	}

	/**
	 *
	 * This function calculates how much of its max steering force the vehicle has
	 * left to apply and then applies that amount of the force to add.
	 */
	private Vector2 __vAccumulateForce = Vector2.newInstance();

	private void __accumulateForce(Vector2 forceToAdd) {

		// calculate how much steering force the vehicle has used so far
		float magnitudeSoFar = __steeringForce.getLength();

		// calculate how much steering force remains to be used by this vehicle
		float magnitudeRemaining = __vehicle.getMaxForce() - magnitudeSoFar;

		// return false if there is no more force left to use
		if (magnitudeRemaining <= 0) {
			return;
		}

		// calculate the magnitude of the force we want to add
		float magnitudeToAdd = forceToAdd.getLength();

		// if the magnitude of the sum of ForceToAdd and the running total
		// does not exceed the maximum force available to this vehicle, just
		// add together. Otherwise add as much of the ForceToAdd vector is
		// possible without going over the max.
		if (magnitudeToAdd < magnitudeRemaining) {
			__steeringForce.add(forceToAdd);
		} else {
			// add it to the steering force
			__vAccumulateForce.set(forceToAdd).normalize().mul(magnitudeRemaining);
			__steeringForce.add(__vAccumulateForce);
		}

	}

	/**
	 * Creates the antenna utilized by WallAvoidance
	 */
	private void __createSensors() {
		__sensors.clear();
		// feeler pointing straight in front
		var front = Vector2.valueOf(__vehicle.getHeading()).mul(__wallDetectionSensorLength)
				.add(__vehicle.getPosition());
		__sensors.add(front);

		// feeler to left
		var left = Transformation.vec2DRotateAroundOrigin(__vehicle.getHeading(), MathUtility.HALF_PI * 3.5f);
		left.mul(__wallDetectionSensorLength / 2.0f).add(__vehicle.getPosition());
		__sensors.add(left);

		// feeler to right
		var right = Transformation.vec2DRotateAroundOrigin(__vehicle.getHeading(), MathUtility.HALF_PI * 0.5f);
		right.mul(__wallDetectionSensorLength / 2.0f).add(__vehicle.getPosition());
		__sensors.add(right);
	}

	// ---------------------------- START OF BEHAVIORS ----------------------------
	//
	// ----------------------------------------------------------------------------
	/**
	 * Given a target, this behavior returns a steering force which will direct the
	 * agent towards the target
	 */
	private Vector2 __doSeek(Vector2 targetPos) {
		var desiredVelocity = Vector2.valueOf(targetPos).sub(__vehicle.getPosition()).normalize()
				.mul(__vehicle.getMaxSpeed());
		return desiredVelocity.sub(__vehicle.getVelocity());
	}

	/**
	 * Does the opposite of Seek
	 */
	private Vector2 __doFlee(Vector2 targetPos) {
		// only flee if the target is within 'panic distance'. Work in distance squared
		// space.
		var desiredVelocity = Vector2.valueOf(__vehicle.getPosition()).sub(targetPos).normalize()
				.mul(__vehicle.getMaxSpeed());
		return desiredVelocity.sub(__vehicle.getVelocity());
	}

	/**
	 * This behavior is similar to seek but it attempts to arrive at the target with
	 * a zero velocity
	 */
	private Vector2 __doArrive(Vector2 targetPos, Deceleration deceleration) {
		var toTarget = Vector2.valueOf(targetPos).sub(__vehicle.getPosition());

		// calculate the distance to the target
		float dist = toTarget.getLength();

		if (dist > 0) {
			// because Deceleration is enumerated as an integer, this value is required
			// to provide fine tweaking of the deceleration..
			final float decelerationTweaker = 0.3f;

			// calculate the speed required to reach the target given the desired
			// deceleration
			float speed = dist / ((float) deceleration.get() * decelerationTweaker);

			// make sure the velocity does not exceed the max
			speed = Math.min(speed, __vehicle.getMaxSpeed());

			// from here proceed just like Seek except we don't need to normalize
			// the ToTarget vector because we have already gone to the trouble
			// of calculating its length: dist.
			var desiredVelocity = toTarget.mul(speed / dist);

			return desiredVelocity.sub(__vehicle.getVelocity());
		}

		return toTarget.zero();
	}

	/**
	 * This behavior creates a force that steers the agent towards the evader
	 */
	private Vector2 __doPursuit(Vehicle evader) {
		// if the evader is ahead and facing the agent then we can just seek
		// for the evader's current position.
		var toEvader = evader.getPosition().sub(__vehicle.getPosition());

		float relativeHeading = __vehicle.getHeading().getDotProductValue(evader.getHeading());

		if ((toEvader.getDotProductValue(__vehicle.getHeading()) > 0) && (relativeHeading < -0.95)) // acos(0.95)=18
																									// degs
		{
			return __doSeek(evader.getPosition());
		}

		// Not considered ahead so we predict where the evader will be.

		// the lookahead time is proportional to the distance between the evader
		// and the pursuer; and is inversely proportional to the sum of the
		// agent's velocities
		float lookAheadTime = toEvader.getLength() / (__vehicle.getMaxSpeed() + evader.getSpeed());

		// now seek to the predicted future position of the evader
		return __doSeek(evader.getVelocity().mul(lookAheadTime).add(evader.getPosition()));
	}

	/**
	 * Similar to pursuit except the agent Flees from the estimated future position
	 * of the pursuer
	 */
	private Vector2 __doEvade(final Vehicle pursuer) {
		// Not necessary to include the check for facing direction this time
		var toPursuer = pursuer.getPosition().sub(__vehicle.getPosition());

		// uncomment the following two lines to have Evade only consider pursuers
		// within a 'threat range'
		final float threatRange = 100;
		if (toPursuer.getLengthSqr() > threatRange * threatRange) {
			return Vector2.newInstance();
		}

		// the lookahead time is proportional to the distance between the pursuer
		// and the pursuer; and is inversely proportional to the sum of the
		// agents' velocities
		float lookAheadTime = toPursuer.getLength() / (__vehicle.getMaxSpeed() + pursuer.getSpeed());

		// now flee away from predicted future position of the pursuer
		return __doFlee(pursuer.getVelocity().mul(lookAheadTime).add(pursuer.getPosition()));
	}

	/**
	 * This behavior makes the agent wander about randomly
	 */
	private Vector2 __doWander() {
		// this behavior is dependent on the update rate, so this line must
		// be included when using time independent frame rate.
		float jitterThisTimeSlice = __wanderJitter * __vehicle.getTimeElapsed();

		// first, add a small random vector to the target's position
		__temp2.set(__wanderTarget).add(MathUtility.randomClamped() * jitterThisTimeSlice,
				MathUtility.randomClamped() * jitterThisTimeSlice);

		// re-project this new vector back on to a unit circle
		__temp2.normalize();

		// increase the length of the vector to the same as the radius
		// of the wander circle
		__temp2.mul(__wanderRadius);

		// move the target into a position WanderDist in front of the agent
		var target = __temp1.set(__wanderDistance, 0).add(__temp2);

		// project the target into world space
		var targetToWorldSpace = Transformation.pointToWorldSpace(target, __vehicle.getHeading(), __vehicle.getSide(),
				__vehicle.getPosition());

		// and steer towards it
		return targetToWorldSpace.sub(__vehicle.getPosition());
	}

	/**
	 * Given a vector of obstacles, this method returns a steering force that will
	 * prevent the agent colliding with the closest obstacle
	 */
	private Vector2 __doObstacleAvoidance(List<BaseGameEntity> obstacles) {
		// the detection box length is proportional to the agent's velocity
		__detectBoxLength = __paramLoader.MIN_DETECTION_BOX_LENGTH
				+ (__vehicle.getSpeed() / __vehicle.getMaxSpeed()) * __paramLoader.MIN_DETECTION_BOX_LENGTH;

		// tag all obstacles within range of the box for processing
		__vehicle.getWorld().tagObstaclesWithinViewRange(__vehicle, __detectBoxLength);

		// this will keep track of the closest intersecting obstacle (CIB)
		BaseGameEntity closestIntersectingObstacle = null;

		// this will be used to track the distance to the CIB
		float distToClosestIP = MathUtility.MAX_FLOAT;

		// this will record the transformed local coordinates of the CIB
		var localPosOfClosestObstacle = __temp1.zero();

		var it = obstacles.listIterator();

		while (it.hasNext()) {
			// if the obstacle has been tagged within range proceed
			var curOb = it.next();
			if (curOb.isTagged()) {
				// calculate this obstacle's position in local space
				var localPos = Transformation.pointToLocalSpace(curOb.getPosition(), __vehicle.getHeading(),
						__vehicle.getSide(), __vehicle.getPosition());

				// if the local position has a negative x value then it must lay
				// behind the agent. (in which case it can be ignored)
				if (localPos.x >= 0) {
					// if the distance from the x axis to the object's position is less
					// than its radius + half the width of the detection box then there
					// is a potential intersection.
					float expandedRadius = curOb.getBoundingRadius() + __vehicle.getBoundingRadius();

					if (Math.abs(localPos.y) < expandedRadius) {
						// now to do a line/circle intersection test. The center of the
						// circle is represented by (cX, cY). The intersection points are
						// given by the formula x = cX +/-sqrt(r^2-cY^2) for y=0.
						// We only need to look at the smallest positive value of x because
						// that will be the closest point of intersection.
						float cX = localPos.x;
						float cY = localPos.y;

						// we only need to calculate the sqrt part of the above equation once
						float sqrtPart = (float) Math.sqrt(expandedRadius * expandedRadius - cY * cY);

						float ip = cX - sqrtPart;

						if (ip <= 0.0) {
							ip = cX + sqrtPart;
						}

						// test to see if this is the closest so far. If it is keep a
						// record of the obstacle and its local coordinates
						if (ip < distToClosestIP) {
							distToClosestIP = ip;

							closestIntersectingObstacle = curOb;

							localPosOfClosestObstacle = localPos;
						}
					}
				}
			}
		}

		// if we have found an intersecting obstacle, calculate a steering
		// force away from it
		var steeringForce = __temp2.zero();

		if (closestIntersectingObstacle != null) {
			// the closer the agent is to an object, the stronger the
			// steering force should be
			float multiplier = 1 + (__detectBoxLength - localPosOfClosestObstacle.x) / __detectBoxLength;

			// calculate the lateral force
			steeringForce.y = (closestIntersectingObstacle.getBoundingRadius() - localPosOfClosestObstacle.y)
					* multiplier;

			// apply a braking force proportional to the obstacles distance from
			// the vehicle.
			final float brakingWeight = 0.2f;

			steeringForce.x = (closestIntersectingObstacle.getBoundingRadius() - localPosOfClosestObstacle.x)
					* brakingWeight;
		}

		// finally, convert the steering vector from local to world space
		return Transformation.vectorToWorldSpace(steeringForce, __vehicle.getHeading(), __vehicle.getSide());
	}

	/**
	 * This returns a steering force that will keep the agent away from any walls it
	 * may encounter
	 */
	private Vector2 __doWallAvoidance(List<Wall> walls) {
		// the feelers are contained in a list, m_Feelers
		__createSensors();

		float distToThisIP = 0;
		float distToClosestIP = MathUtility.MAX_FLOAT;

		// this will hold an index into the vector of walls
		int closestWall = -1;

		var steeringForce = __temp1.zero();
		var closestPoint = __temp3.zero(); // holds the closest intersection point

		// examine each feeler in turn
		for (int flr = 0; flr < __sensors.size(); ++flr) {
			// run through each wall checking for any intersection points
			for (int w = 0; w < walls.size(); ++w) {
				distToThisIP = Geometry.getDistanceTwoSegmentIntersect(__vehicle.getPosition(), __sensors.get(flr),
						walls.get(w).getFrom(), walls.get(w).getTo());
				if (distToThisIP != -1) {
					// is this the closest found so far? If so keep a record
					if (distToThisIP < distToClosestIP) {
						distToClosestIP = distToThisIP;

						closestWall = w;

						closestPoint = Geometry.getPointTwoSegmentIntersect(__vehicle.getPosition(), __sensors.get(flr),
								walls.get(w).getFrom(), walls.get(w).getTo());
					}
				}
			} // next wall

			// if an intersection point has been detected, calculate a force
			// that will direct the agent away
			if (closestWall >= 0) {
				// calculate by what distance the projected position of the agent
				// will overshoot the wall
				var overShoot = __sensors.get(flr).sub(closestPoint);

				// create a force in the direction of the wall normal, with a
				// magnitude of the overshoot
				steeringForce = __temp2.set(walls.get(closestWall).getNormal()).mul(overShoot.getLength());
			}

		} // next feeler

		return steeringForce.clone();
	}

	/**
	 * This calculates a force re-pelling from the other neighbors
	 */
	private Vector2 __doSeparation(List<Vehicle> neighbors) {
		var steeringForce = __temp1.zero();

		for (int a = 0; a < neighbors.size(); ++a) {
			// make sure this agent isn't included in the calculations and that
			// the agent being examined is close enough. ***also make sure it doesn't
			// include the evade target ***
			if ((neighbors.get(a) != __vehicle) && neighbors.get(a).isTagged()
					&& (neighbors.get(a) != __targetAgent1)) {
				var toAgent = __temp2.set(__vehicle.getPosition()).sub(neighbors.get(a).getPosition());

				// scale the force inversely proportional to the agents distance
				// from its neighbor.
				steeringForce.add(toAgent.normalize().div(toAgent.getLength()));
			}
		}

		return steeringForce.clone();
	}

	/**
	 * Returns a force that attempts to align this agents heading with that of its
	 * neighbors
	 */
	private Vector2 __doAlignment(List<Vehicle> neighbors) {
		// used to record the average heading of the neighbors
		var averageHeading = __temp1.zero();

		// used to count the number of vehicles in the neighborhood
		int neighborCount = 0;

		// iterate through all the tagged vehicles and sum their heading vectors
		for (int a = 0; a < neighbors.size(); ++a) {
			// make sure *this* agent isn't included in the calculations and that
			// the agent being examined is close enough ***also make sure it doesn't
			// include any evade target ***
			if ((neighbors.get(a) != __vehicle) && neighbors.get(a).isTagged()
					&& (neighbors.get(a) != __targetAgent1)) {
				averageHeading.add(neighbors.get(a).getHeading());

				++neighborCount;
			}
		}

		// if the neighborhood contained one or more vehicles, average their
		// heading vectors.
		if (neighborCount > 0) {
			averageHeading.div((float) neighborCount);
			averageHeading.sub(__vehicle.getHeading());
		}

		return averageHeading.clone();
	}

	/**
	 * Returns a steering force that attempts to move the agent towards the center
	 * of mass of the agents in its immediate area
	 */
	private Vector2 __doCohesion(final List<Vehicle> neighbors) {
		// first find the center of mass of all the agents
		var centerOfMass = __temp1.zero();
		var steeringForce = __temp2.zero();

		int neighborCount = 0;

		// iterate through the neighbors and sum up all the position vectors
		for (int a = 0; a < neighbors.size(); ++a) {
			// make sure *this* agent isn't included in the calculations and that
			// the agent being examined is close enough ***also make sure it doesn't
			// include the evade target ***
			if ((neighbors.get(a) != __vehicle) && neighbors.get(a).isTagged()
					&& (neighbors.get(a) != __targetAgent1)) {
				centerOfMass.add(neighbors.get(a).getPosition());

				++neighborCount;
			}
		}

		if (neighborCount > 0) {
			// the center of mass is the average of the sum of positions
			centerOfMass.div((float) neighborCount);

			// now seek towards that position
			steeringForce = __doSeek(centerOfMass);
		}

		// the magnitude of cohesion is usually much larger than separation or
		// alignment so it usually helps to normalize it.
		return steeringForce.normalize();
	}

	/*
	 * NOTE: the next three behaviors are the same as the above three, except that
	 * they use a cell-space partition to find the neighbors
	 */
	/**
	 * This calculates a force re-pelling from the other neighbors
	 *
	 * USES SPACIAL PARTITIONING
	 */
	private Vector2 __doSeparationPlus(List<Vehicle> neighbors) {
		var steeringForce = __temp1.zero();

		// iterate through the neighbors and sum up all the position vectors
		for (var pV = __vehicle.getWorld().getCellSpace().getFrontOfNeighbor(); !__vehicle.getWorld().getCellSpace()
				.isEndOfNeighbors(); pV = __vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
			// make sure this agent isn't included in the calculations and that
			// the agent being examined is close enough
			if (pV != __vehicle) {
				var toAgent = __temp2.set(__vehicle.getPosition()).sub(pV.getPosition());

				// scale the force inversely proportional to the agents distance
				// from its neighbor.
				steeringForce.add(toAgent.normalize().div(toAgent.getLength()));
			}

		}

		return steeringForce.clone();
	}

	/**
	 * Returns a force that attempts to align this agents heading with that of its
	 * neighbors
	 *
	 * USES SPACIAL PARTITIONING
	 */
	private Vector2 __doAlignmentPlus(List<Vehicle> neighbors) {
		// This will record the average heading of the neighbors
		var averageHeading = __temp1.zero();

		// This count the number of vehicles in the neighborhood
		float neighborCount = 0;

		// iterate through the neighbors and sum up all the position vectors
		for (var pV = __vehicle.getWorld().getCellSpace().getFrontOfNeighbor(); !__vehicle.getWorld().getCellSpace()
				.isEndOfNeighbors(); pV = __vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
			// make sure *this* agent isn't included in the calculations and that
			// the agent being examined is close enough
			if (pV != __vehicle) {
				averageHeading.add(pV.getHeading());
				++neighborCount;
			}
		}

		// if the neighborhood contained one or more vehicles, average their
		// heading vectors.
		if (neighborCount > 0) {
			averageHeading.div(neighborCount);
			averageHeading.sub(__vehicle.getHeading());
		}

		return averageHeading.clone();
	}

	/**
	 * Returns a steering force that attempts to move the agent towards the center
	 * of mass of the agents in its immediate area
	 *
	 * USES SPACIAL PARTITIONING
	 */
	private Vector2 __doCohesionPlus(final List<Vehicle> neighbors) {
		// first find the center of mass of all the agents
		var centerOfMass = __temp1.zero();
		var steeringForce = __temp2.zero();

		int neighborCount = 0;

		// iterate through the neighbors and sum up all the position vectors
		for (var pV = __vehicle.getWorld().getCellSpace().getFrontOfNeighbor(); !__vehicle.getWorld().getCellSpace()
				.isEndOfNeighbors(); pV = __vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
			// make sure *this* agent isn't included in the calculations and that
			// the agent being examined is close enough
			if (pV != __vehicle) {
				centerOfMass.add(pV.getPosition());

				++neighborCount;
			}
		}

		if (neighborCount > 0) {
			// the center of mass is the average of the sum of positions
			centerOfMass.div((float) neighborCount);

			// now seek towards that position
			steeringForce = __doSeek(centerOfMass);
		}

		// the magnitude of cohesion is usually much larger than separation or
		// alignment so it usually helps to normalize it.
		return steeringForce.normalize();
	}

	/**
	 * Given two agents, this method returns a force that attempts to position the
	 * vehicle between them
	 */
	private Vector2 __doInterpose(final Vehicle agentA, final Vehicle agentB) {
		// first we need to figure out where the two agents are going to be at
		// time T in the future. This is approximated by determining the time
		// taken to reach the mid way point at the current time at at max speed.
		var midPoint = __temp1.set(agentA.getPosition()).add(agentB.getPosition()).div(2);

		float timeToReachMidPoint = __vehicle.getPosition().getDistanceValue(midPoint) / __vehicle.getMaxSpeed();

		// now we have T, we assume that agent A and agent B will continue on a
		// straight trajectory and extrapolate to get their future positions
		var aPos = __temp2.set(agentA.getVelocity()).mul(timeToReachMidPoint).add(agentA.getPosition());
		var bPos = __temp3.set(agentB.getVelocity()).mul(timeToReachMidPoint).add(agentB.getPosition());

		// calculate the mid point of these predicted positions
		midPoint = __temp4.set(aPos).add(bPos).div(2);

		// then steer to Arrive at it
		return __doArrive(midPoint, Deceleration.FAST);
	}

	private Vector2 __doHide(final Vehicle hunter, final List<BaseGameEntity> obstacles) {
		float distToClosest = MathUtility.MAX_FLOAT;
		var bestHidingSpot = __temp1.zero();

		var it = obstacles.listIterator();

		while (it.hasNext()) {
			var curOb = it.next();
			// calculate the position of the hiding spot for this obstacle
			var hidingSpot = __getHidingPosition(curOb.getPosition(), curOb.getBoundingRadius(), hunter.getPosition());

			// work in distance-squared space to find the closest hiding
			// spot to the agent
			float dist = hidingSpot.getDistanceSqrValue(__vehicle.getPosition());

			if (dist < distToClosest) {
				distToClosest = dist;
				bestHidingSpot = hidingSpot;
			}
		} // end while

		// if no suitable obstacles found then Evade the hunter
		if (distToClosest == MathUtility.MAX_FLOAT) {
			return __doEvade(hunter);
		}

		// else use Arrive on the hiding spot
		return __doArrive(bestHidingSpot, Deceleration.FAST);
	}

	/**
	 * Given the position of a hunter, and the position and radius of an obstacle,
	 * this method calculates a position DistanceFromBoundary away from its bounding
	 * radius and directly opposite the hunter
	 */
	private Vector2 __getHidingPosition(final Vector2 posOb, final float radiusOb, final Vector2 posHunter) {
		// calculate how far away the agent is to be from the chosen obstacle's
		// bounding radius
		final float distanceFromBoundary = 30;
		float distAway = radiusOb + distanceFromBoundary;

		// calculate the heading toward the object from the hunter
		var toOb = __temp1.set(posOb).sub(posHunter).normalize();

		// scale it to size and add to the obstacles position to get
		// the hiding spot.
		return toOb.mul(distAway).add(posOb).clone();
	}

	/**
	 * Given a series of Vector2Ds, this method produces a force that will move the
	 * agent along the way-points in order. The agent uses the 'Seek' behavior to
	 * move to the next way-point - unless it is the last way-point, in which case
	 * it 'Arrives'
	 */
	private Vector2 __doFollowPath() {
		// move to next target if close enough to current target (working in
		// distance squared space)
		if (__path.getCurrentWayPoint().getDistanceSqrValue(__vehicle.getPosition()) < __waypointSeekDistanceSqr) {
			__path.setToNextWayPoint();
		}

		if (!__path.isEndOfWayPoints()) {
			return __doSeek(__path.getCurrentWayPoint());
		} else {
			return __doArrive(__path.getCurrentWayPoint(), Deceleration.NORMAL);
		}
	}

	/**
	 * Produces a steering force that keeps a vehicle at a specified offset from a
	 * leader vehicle
	 */
	private Vector2 __doOffsetPursuit(final Vehicle leader, final Vector2 offset) {
		// calculate the offset's position in world space
		var worldOffsetPos = Transformation.pointToWorldSpace(offset, leader.getHeading(), leader.getSide(),
				leader.getPosition());

		var toOffset = worldOffsetPos.sub(__vehicle.getPosition());

		// the lookahead time is proportional to the distance between the leader
		// and the pursuer; and is inversely proportional to the sum of both
		// agent's velocities
		float lookAheadTime = toOffset.getLength() / (__vehicle.getMaxSpeed() + leader.getSpeed());

		// now Arrive at the predicted future position of the offset
		return __doArrive(leader.getVelocity().mul(lookAheadTime).add(worldOffsetPos), Deceleration.FAST);
	}

	/**
	 * Renders visual aids and info for seeing how each behavior is calculated
	 */
	@Override
	public void render(Paint paint) {
		paint.enableOpaqueText(false);
		paint.setTextColor(Color.GRAY);

		int nextSlot = paint.getFontHeight();
		int slotSize = 20;

		if (__vehicle.getMaxForce() < 0) {
			__vehicle.setMaxForce(0.0f);
		}
		if (__vehicle.getMaxSpeed() < 0) {
			__vehicle.setMaxSpeed(0.0f);
		}

		if (__vehicle.getId() == "dragon") {
			paint.drawTextAtPosition(5, nextSlot, "MaxForce(Ins/Del):");
			paint.drawTextAtPosition(160, nextSlot,
					String.valueOf(__vehicle.getMaxForce() / __paramLoader.STEERING_FORCE_TWEAKER));
			nextSlot += slotSize;
		}
		if (__vehicle.getId() == "dragon") {
			paint.drawTextAtPosition(5, nextSlot, "MaxSpeed(Home/End):");
			paint.drawTextAtPosition(160, nextSlot, String.valueOf(__vehicle.getMaxSpeed()));
			nextSlot += slotSize;
		}

		// render the steering force
		if (__vehicle.getWorld().isRenderSteeringForce()) {
			paint.setPenColor(Color.RED);
			var F = __temp1.set(__steeringForce).div(__paramLoader.STEERING_FORCE_TWEAKER)
					.mul(__paramLoader.VEHICLE_SCALE);
			paint.drawLine(__vehicle.getPosition(), __temp2.set(__vehicle.getPosition().add(F)));
		}

		// render wander stuff if relevant
		if (__isBehavior(Behavior.WANDER) && __vehicle.getWorld().isRenderWanderCircle()) {

			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Jitter(F/V): ");
				paint.drawTextAtPosition(160, nextSlot, String.valueOf(__wanderJitter));
				nextSlot += slotSize;
			}
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Distance(G/B): ");
				paint.drawTextAtPosition(160, nextSlot, String.valueOf(__wanderDistance));
				nextSlot += slotSize;
			}
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Radius(H/N): ");
				paint.drawTextAtPosition(160, nextSlot, String.valueOf(__wanderRadius));
				nextSlot += slotSize;
			}

			// calculate the center of the wander circle
			var vTCC = Transformation.pointToWorldSpace(
					__temp1.set(__wanderDistance * __vehicle.getBoundingRadius(), 0), __vehicle.getHeading(),
					__vehicle.getSide(), __vehicle.getPosition());
			// draw the wander circle
			paint.setPenColor(Color.GREEN);
			paint.setBgColor(null);
			paint.drawCircle(vTCC, __wanderRadius * __vehicle.getBoundingRadius());

			// draw the wander target
			paint.setPenColor(Color.RED);
			paint.drawCircle(Transformation.pointToWorldSpace(
					__temp1.set(__wanderTarget).add(__wanderDistance, 0).mul(__vehicle.getBoundingRadius()),
					__vehicle.getHeading(), __vehicle.getSide(), __vehicle.getPosition()), 3);
		}

		// render the detection box if relevant
		if (__vehicle.getWorld().isRenderDetectionBox()) {

			paint.setPenColor(Color.GRAY);

			float length = __paramLoader.MIN_DETECTION_BOX_LENGTH
					+ (__vehicle.getSpeed() / __vehicle.getMaxSpeed()) * __paramLoader.MIN_DETECTION_BOX_LENGTH;

			// verts for the detection box buffer
			__detectBox.clear();
			__detectBox.add(__temp1.set(0, __vehicle.getBoundingRadius()));
			__detectBox.add(__temp2.set(length, __vehicle.getBoundingRadius()));
			__detectBox.add(__temp3.set(length, -__vehicle.getBoundingRadius()));
			__detectBox.add(__temp4.set(0, -__vehicle.getBoundingRadius()));

			if (!__vehicle.isSmoothing()) {
				__detectBox = Transformation.pointsToWorldSpace(__detectBox, __vehicle.getPosition(),
						__vehicle.getHeading(), __vehicle.getSide());
				paint.drawClosedShape(__detectBox);
			} else {
				__detectBox = Transformation.pointsToWorldSpace(__detectBox, __vehicle.getPosition(),
						__vehicle.getSmoothedHeading(), __temp5.set(__vehicle.getSmoothedHeading()).perpendicular());
				paint.drawClosedShape(__detectBox);
			}

			// the detection box length is proportional to the agent's velocity
			__detectBoxLength = __paramLoader.MIN_DETECTION_BOX_LENGTH
					+ (__vehicle.getSpeed() / __vehicle.getMaxSpeed()) * __paramLoader.MIN_DETECTION_BOX_LENGTH;

			// tag all obstacles within range of the box for processing
			__vehicle.getWorld().tagObstaclesWithinViewRange(__vehicle, __detectBoxLength);

			var it = __vehicle.getWorld().getObstacles().listIterator();

			while (it.hasNext()) {
				var curOb = it.next();
				// if the obstacle has been tagged within range proceed
				if (curOb.isTagged()) {
					// calculate this obstacle's position in local space
					var localPos = Transformation.pointToLocalSpace(curOb.getPosition(), __vehicle.getHeading(),
							__vehicle.getSide(), __vehicle.getPosition());

					// if the local position has a negative x value then it must lay
					// behind the agent. (in which case it can be ignored)
					if (localPos.x >= 0) {
						// if the distance from the x axis to the object's position is less
						// than its radius + half the width of the detection box then there
						// is a potential intersection.
						if (Math.abs(localPos.y) < (curOb.getBoundingRadius() + __vehicle.getBoundingRadius())) {
							paint.setPenColor(Color.RED);
							paint.drawClosedShape(__detectBox);
						}
					}
				}
			}
		}

		// render the wall avoidance feelers
		if (__isBehavior(Behavior.WALL_AVOIDANCE) && __vehicle.getWorld().isRenderSensors()) {
			paint.setPenColor(Color.ORANGE);
			for (int flr = 0; flr < __sensors.size(); ++flr) {
				paint.drawLine(__vehicle.getPosition(), __sensors.get(flr));
			}
		}

		// render path info
		if (__isBehavior(Behavior.FOLLOW_PATH) && __vehicle.getWorld().isRenderPath()) {
			__path.render(paint);
		}

		if (__isBehavior(Behavior.SEPARATION)) {
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Separation(S/X):");
				paint.drawTextAtPosition(160, nextSlot,
						String.valueOf(__weightSeparation / __paramLoader.STEERING_FORCE_TWEAKER));
				nextSlot += slotSize;
			}
		}

		if (__isBehavior(Behavior.ALLIGNMENT)) {
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Alignment(A/Z):");
				paint.drawTextAtPosition(160, nextSlot,
						String.valueOf(__weightAlignment / __paramLoader.STEERING_FORCE_TWEAKER));
				nextSlot += slotSize;
			}
		}

		if (__isBehavior(Behavior.COHESION)) {
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "Cohesion(D/C):");
				paint.drawTextAtPosition(160, nextSlot,
						String.valueOf(__weightCohesion / __paramLoader.STEERING_FORCE_TWEAKER));
				nextSlot += slotSize;
			}
		}

		if (__isBehavior(Behavior.FOLLOW_PATH)) {
			float sd = (float) Math.sqrt(__waypointSeekDistanceSqr);
			if (__vehicle.getId() == "dragon") {
				paint.drawTextAtPosition(5, nextSlot, "SeekDistance(D/C):");
				paint.drawTextAtPosition(160, nextSlot, String.valueOf(sd));
				nextSlot += slotSize;
			}
		}
	}

	// ---------------------------- CALCULATE METHODS ----------------------------
	//
	// ---------------------------------------------------------------------------
	/**
	 * Calculates the accumulated steering force according to the method set in
	 * m_SummingMethod
	 */
	public Vector2 calculateAccumulate() {
		// reset the steering force
		__steeringForce.zero();

		// use space partitioning to calculate the neighbors of this vehicle
		// if switched on. If not, use the standard tagging system
		if (!isSpacePartitioning()) {
			// tag neighbors if any of the following 3 group behaviors are switched on
			if (__isBehavior(Behavior.SEPARATION) || __isBehavior(Behavior.ALLIGNMENT)
					|| __isBehavior(Behavior.COHESION)) {
				// __vehicle.getWorld().tagVehiclesWithinViewRange(__vehicle,
				// __agentViewDistance);
			}
		} else {
			// calculate neighbors in cell-space if any of the following 3 group
			// behaviors are switched on
			if (__isBehavior(Behavior.SEPARATION) || __isBehavior(Behavior.ALLIGNMENT)
					|| __isBehavior(Behavior.COHESION)) {
				// __vehicle.getWorld().getCellSpace().calculateNeighbors(__vehicle.getPosition(),
				// __agentViewDistance);
			}
		}

		switch (__summingMethod) {
		case WEIGHTED_AVERAGE:
			__steeringForce = __getCalculateWeightedSum();
			break;

		case PRIORITIZED:
			__calculatePrioritized();
			break;

		case DITHERED:
			__steeringForce = __getCalculateDithered();
			break;

		default:
			__steeringForce = Vector2.newInstance();
			break;
		}

		return __steeringForce;
	}

	/**
	 * Returns the forward component of the steering force
	 */
	public float getForwardComponent() {
		return __vehicle.getHeading().getDotProductValue(__steeringForce);
	}

	/**
	 * Returns the side component of the steering force
	 */
	public float getSideComponent() {
		return __vehicle.getSide().getDotProductValue(__steeringForce);
	}

	/**
	 * This method calls each active steering behavior in order of priority and
	 * accumulates their forces until the max steering force magnitude is reached,
	 * at which time the function returns the steering force accumulated to that
	 * point
	 */
	private void __calculatePrioritized() {

		if (__isBehavior(Behavior.WALL_AVOIDANCE)) {
			var force = __doWallAvoidance(__vehicle.getWorld().getWalls()).mul(__weightWallAvoidance);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
			var force = __doObstacleAvoidance(__vehicle.getWorld().getObstacles()).mul(__weightObstacleAvoidance);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.EVADE)) {
			var force = __doEvade(__targetAgent1).mul(__weightEvade);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.FLEE)) {
			var force = __doFlee(__vehicle.getWorld().getCrosshair()).mul(__weightFlee);
			__accumulateForce(force);
		}

		// these next three can be combined for flocking behavior (wander is
		// also a good behavior to add into this mix)
		if (!isSpacePartitioning()) {
			if (__isBehavior(Behavior.SEPARATION)) {
				var force = __doSeparation(__vehicle.getWorld().getAgents()).mul(__weightSeparation);
				__accumulateForce(force);
			}

			if (__isBehavior(Behavior.ALLIGNMENT)) {
				var force = __doAlignment(__vehicle.getWorld().getAgents()).mul(__weightAlignment);
				__accumulateForce(force);
			}

			if (__isBehavior(Behavior.COHESION)) {
				var force = __doCohesion(__vehicle.getWorld().getAgents()).mul(__weightCohesion);
				__accumulateForce(force);
			}
		} else {
			if (__isBehavior(Behavior.SEPARATION)) {
				var force = __doSeparationPlus(__vehicle.getWorld().getAgents()).mul(__weightSeparation);
				__accumulateForce(force);
			}

			if (__isBehavior(Behavior.ALLIGNMENT)) {
				var force = __doAlignmentPlus(__vehicle.getWorld().getAgents()).mul(__weightAlignment);
				__accumulateForce(force);
			}

			if (__isBehavior(Behavior.COHESION)) {
				var force = __doCohesionPlus(__vehicle.getWorld().getAgents()).mul(__weightCohesion);
				__accumulateForce(force);
			}
		}

		if (__isBehavior(Behavior.SEEK)) {
			var force = __doSeek(__vehicle.getWorld().getCrosshair()).mul(__weightSeek);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.ARRIVE)) {
			var force = __doArrive(__vehicle.getWorld().getCrosshair(), __deceleration).mul(__weightArrive);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.WANDER)) {
			var force = __doWander().mul(__weightWander);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.PURSUIT)) {
			var force = __doPursuit(__targetAgent1).mul(__weightPursuit);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.OFFSET_PURSUIT)) {
			var force = __doOffsetPursuit(__targetAgent1, __offset);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.INTERPOSE)) {
			var force = __doInterpose(__targetAgent1, __targetAgent2).mul(__weightInterpose);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.HIDE)) {
			var force = __doHide(__targetAgent1, __vehicle.getWorld().getObstacles()).mul(__weightHide);
			__accumulateForce(force);
		}

		if (__isBehavior(Behavior.FOLLOW_PATH)) {
			var force = __doFollowPath().mul(__weightFollowPath);
			__accumulateForce(force);
		}

	}

	/**
	 * This simply sums up all the active behaviors X their weights and truncates
	 * the result to the max available steering force before returning
	 */
	private Vector2 __getCalculateWeightedSum() {
		if (__isBehavior(Behavior.WALL_AVOIDANCE)) {
			__steeringForce.add(__doWallAvoidance(__vehicle.getWorld().getWalls())).mul(__weightWallAvoidance);
		}

		if (__isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
			__steeringForce.add(__doObstacleAvoidance(__vehicle.getWorld().getObstacles()))
					.mul(__weightObstacleAvoidance);
		}

		if (__isBehavior(Behavior.EVADE)) {
			__steeringForce.add(__doEvade(__targetAgent1)).mul(__weightEvade);
		}

		// these next three can be combined for flocking behavior (wander is
		// also a good behavior to add into this mix)
		if (!isSpacePartitioning()) {
			if (__isBehavior(Behavior.SEPARATION)) {
				__steeringForce.add(__doSeparation(__vehicle.getWorld().getAgents())).mul(__weightSeparation);
			}

			if (__isBehavior(Behavior.ALLIGNMENT)) {
				__steeringForce.add(__doAlignment(__vehicle.getWorld().getAgents())).mul(__weightAlignment);
			}

			if (__isBehavior(Behavior.COHESION)) {
				__steeringForce.add(__doCohesion(__vehicle.getWorld().getAgents())).mul(__weightCohesion);
			}
		} else {
			if (__isBehavior(Behavior.SEPARATION)) {
				__steeringForce.add(__doSeparationPlus(__vehicle.getWorld().getAgents())).mul(__weightSeparation);
			}

			if (__isBehavior(Behavior.ALLIGNMENT)) {
				__steeringForce.add(__doAlignmentPlus(__vehicle.getWorld().getAgents())).mul(__weightAlignment);
			}

			if (__isBehavior(Behavior.COHESION)) {
				__steeringForce.add(__doCohesionPlus(__vehicle.getWorld().getAgents())).mul(__weightCohesion);
			}
		}

		if (__isBehavior(Behavior.WANDER)) {
			__steeringForce.add(__doWander()).mul(__weightWander);
		}

		if (__isBehavior(Behavior.SEEK)) {
			__steeringForce.add(__doSeek(__vehicle.getWorld().getCrosshair())).mul(__weightSeek);
		}

		if (__isBehavior(Behavior.FLEE)) {
			__steeringForce.add(__doFlee(__vehicle.getWorld().getCrosshair())).mul(__weightFlee);
		}

		if (__isBehavior(Behavior.ARRIVE)) {
			__steeringForce.add(__doArrive(__vehicle.getWorld().getCrosshair(), __deceleration)).mul(__weightArrive);
		}

		if (__isBehavior(Behavior.PURSUIT)) {
			__steeringForce.add(__doPursuit(__targetAgent1)).mul(__weightPursuit);
		}

		if (__isBehavior(Behavior.OFFSET_PURSUIT)) {
			__steeringForce.add(__doOffsetPursuit(__targetAgent1, __offset)).mul(__weightOffsetPursuit);
		}

		if (__isBehavior(Behavior.INTERPOSE)) {
			__steeringForce.add(__doInterpose(__targetAgent1, __targetAgent2)).mul(__weightInterpose);
		}

		if (__isBehavior(Behavior.HIDE)) {
			__steeringForce.add(__doHide(__targetAgent1, __vehicle.getWorld().getObstacles())).mul(__weightHide);
		}

		if (__isBehavior(Behavior.FOLLOW_PATH)) {
			__steeringForce.add(__doFollowPath()).mul(__weightFollowPath);
		}

		__steeringForce.truncate(__vehicle.getMaxForce());

		return __steeringForce;
	}

	/**
	 * This method sums up the active behaviors by assigning a probability of being
	 * calculated to each behavior. It then tests the first priority to see if it
	 * should be calculated this simulation-step. If so, it calculates the steering
	 * force resulting from this behavior. If it is more than zero it returns the
	 * force. If zero, or if the behavior is skipped it continues onto the next
	 * priority, and so on.
	 *
	 * NOTE: Not all of the behaviors have been implemented in this method, just a
	 * few, so you get the general idea
	 */
	private Vector2 __getCalculateDithered() {
		// reset the steering force
		__steeringForce.zero();

		if (__isBehavior(Behavior.WALL_AVOIDANCE) && MathUtility.randFloat() < __paramLoader.PR_WALL_AVOIDANCE) {
			__steeringForce = __doWallAvoidance(__vehicle.getWorld().getWalls())
					.mul(__weightWallAvoidance / __paramLoader.PR_WALL_AVOIDANCE);

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (__isBehavior(Behavior.OBSTACLE_AVOIDANCE)
				&& MathUtility.randFloat() < __paramLoader.PR_OBSTACLE_AVOIDANCE) {
			__steeringForce.add(__doObstacleAvoidance(__vehicle.getWorld().getObstacles())
					.mul(__weightObstacleAvoidance / __paramLoader.PR_OBSTACLE_AVOIDANCE));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (!isSpacePartitioning()) {
			if (__isBehavior(Behavior.SEPARATION) && MathUtility.randFloat() < __paramLoader.PR_SEPARATION) {
				__steeringForce.add(__doSeparation(__vehicle.getWorld().getAgents())
						.mul(__weightSeparation / __paramLoader.PR_SEPARATION));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}
		} else {
			if (__isBehavior(Behavior.SEPARATION) && MathUtility.randFloat() < __paramLoader.PR_SEPARATION) {
				__steeringForce.add(__doSeparationPlus(__vehicle.getWorld().getAgents())
						.mul(__weightSeparation / __paramLoader.PR_SEPARATION));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}
		}

		if (__isBehavior(Behavior.FLEE) && MathUtility.randFloat() < __paramLoader.PR_FLEE) {
			__steeringForce
					.add(__doFlee(__vehicle.getWorld().getCrosshair()).mul(__weightFlee / __paramLoader.PR_FLEE));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (__isBehavior(Behavior.EVADE) && MathUtility.randFloat() < __paramLoader.PR_EVADE) {
			__steeringForce.add(__doEvade(__targetAgent1).mul(__weightEvade / __paramLoader.PR_EVADE));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (!isSpacePartitioning()) {
			if (__isBehavior(Behavior.ALLIGNMENT) && MathUtility.randFloat() < __paramLoader.PR_ALIGNMENT) {
				__steeringForce.add(__doAlignment(__vehicle.getWorld().getAgents())
						.mul(__weightAlignment / __paramLoader.PR_ALIGNMENT));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}

			if (__isBehavior(Behavior.COHESION) && MathUtility.randFloat() < __paramLoader.PR_COHESION) {
				__steeringForce.add(__doCohesion(__vehicle.getWorld().getAgents())
						.mul(__weightCohesion / __paramLoader.PR_COHESION));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}
		} else {
			if (__isBehavior(Behavior.ALLIGNMENT) && MathUtility.randFloat() < __paramLoader.PR_ALIGNMENT) {
				__steeringForce.add(__doAlignmentPlus(__vehicle.getWorld().getAgents())
						.mul(__weightAlignment / __paramLoader.PR_ALIGNMENT));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}

			if (__isBehavior(Behavior.COHESION) && MathUtility.randFloat() < __paramLoader.PR_COHESION) {
				__steeringForce.add(__doCohesionPlus(__vehicle.getWorld().getAgents())
						.mul(__weightCohesion / __paramLoader.PR_COHESION));

				if (!__steeringForce.isZero()) {
					__steeringForce.truncate(__vehicle.getMaxForce());

					return __steeringForce;
				}
			}
		}

		if (__isBehavior(Behavior.WANDER) && MathUtility.randFloat() < __paramLoader.PR_WANDER) {
			__steeringForce.add(__doWander().mul(__weightWander / __paramLoader.PR_WANDER));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (__isBehavior(Behavior.SEEK) && MathUtility.randFloat() < __paramLoader.PR_SEEK) {
			__steeringForce
					.add(__doSeek(__vehicle.getWorld().getCrosshair()).mul(__weightSeek / __paramLoader.PR_SEEK));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		if (__isBehavior(Behavior.ARRIVE) && MathUtility.randFloat() < __paramLoader.PR_ARRIVE) {
			__steeringForce.add(__doArrive(__vehicle.getWorld().getCrosshair(), __deceleration)
					.mul(__weightArrive / __paramLoader.PR_ARRIVE));

			if (!__steeringForce.isZero()) {
				__steeringForce.truncate(__vehicle.getMaxForce());

				return __steeringForce;
			}
		}

		return __steeringForce;
	}

	public void setTargetAgent1(Vehicle Agent) {
		__targetAgent1 = Agent;
	}

	public void setTargetAgent2(Vehicle Agent) {
		__targetAgent2 = Agent;
	}

	public void setOffset(final Vector2 offset) {
		__offset = offset;
	}

	public Vector2 getOffset() {
		return __offset;
	}

	public void setPath(List<Vector2> path) {
		__path.setWayPoints(path);
	}

	public void createRandomPath(int numWaypoints, int mx, int my, int cx, int cy) {
		__path.createRandomPath(numWaypoints, mx, my, cx, cy);
	}

	public Vector2 getForce() {
		return __steeringForce;
	}

	public void toggleSpacePartitioning() {
		__enableCellSpace = !__enableCellSpace;
	}

	public boolean isSpacePartitioning() {
		return __enableCellSpace;
	}

	public void setSummingMethod(SummingMethod sm) {
		__summingMethod = sm;
	}

	public void setFleeOn() {
		__behaviorFlag |= Behavior.FLEE.get();
	}

	public void setSeekOn() {
		__behaviorFlag |= Behavior.SEEK.get();
	}

	public void setArriveOn() {
		__behaviorFlag |= Behavior.ARRIVE.get();
	}

	public void setWanderOn() {
		__behaviorFlag |= Behavior.WANDER.get();
	}

	public void setPursuitOn(Vehicle vehicle) {
		__behaviorFlag |= Behavior.PURSUIT.get();
		__targetAgent1 = vehicle;
	}

	public void setEvadeOn(Vehicle vehicle) {
		__behaviorFlag |= Behavior.EVADE.get();
		__targetAgent1 = vehicle;
	}

	public void setCohesionOn() {
		__behaviorFlag |= Behavior.COHESION.get();
	}

	public void setSeparationOn() {
		__behaviorFlag |= Behavior.SEPARATION.get();
	}

	public void setAlignmentOn() {
		__behaviorFlag |= Behavior.ALLIGNMENT.get();
	}

	public void setObstacleAvoidanceOn() {
		__behaviorFlag |= Behavior.OBSTACLE_AVOIDANCE.get();
	}

	public void setWallAvoidanceOn() {
		__behaviorFlag |= Behavior.WALL_AVOIDANCE.get();
	}

	public void setFollowPathOn() {
		__behaviorFlag |= Behavior.FOLLOW_PATH.get();
	}

	public void setInterposeOn(Vehicle v1, Vehicle v2) {
		__behaviorFlag |= Behavior.INTERPOSE.get();
		__targetAgent1 = v1;
		__targetAgent2 = v2;
	}

	public void setHideOn(Vehicle vehicle) {
		__behaviorFlag |= Behavior.HIDE.get();
		__targetAgent1 = vehicle;
	}

	public void setOffsetPursuitOn(Vehicle vehicle, final Vector2 offset) {
		__behaviorFlag |= Behavior.OFFSET_PURSUIT.get();
		__offset = offset;
		__targetAgent1 = vehicle;
	}

	public void setFlockingOn() {
		setCohesionOn();
		setAlignmentOn();
		setSeparationOn();
		setWanderOn();
	}

	public void setFleeOff() {
		if (__isBehavior(Behavior.FLEE)) {
			__behaviorFlag ^= Behavior.FLEE.get();
		}
	}

	public void setSeekOff() {
		if (__isBehavior(Behavior.SEEK)) {
			__behaviorFlag ^= Behavior.SEEK.get();
		}
	}

	public void setArriveOff() {
		if (__isBehavior(Behavior.ARRIVE)) {
			__behaviorFlag ^= Behavior.ARRIVE.get();
		}
	}

	public void setWanderOff() {
		if (__isBehavior(Behavior.WANDER)) {
			__behaviorFlag ^= Behavior.WANDER.get();
		}
	}

	public void setPursuitOff() {
		if (__isBehavior(Behavior.PURSUIT)) {
			__behaviorFlag ^= Behavior.PURSUIT.get();
		}
	}

	public void setEvadeOff() {
		if (__isBehavior(Behavior.EVADE)) {
			__behaviorFlag ^= Behavior.EVADE.get();
		}
	}

	public void setCohesionOff() {
		if (__isBehavior(Behavior.COHESION)) {
			__behaviorFlag ^= Behavior.COHESION.get();
		}
	}

	public void setSeparationOff() {
		if (__isBehavior(Behavior.SEPARATION)) {
			__behaviorFlag ^= Behavior.SEPARATION.get();
		}
	}

	public void setAlignmentOff() {
		if (__isBehavior(Behavior.ALLIGNMENT)) {
			__behaviorFlag ^= Behavior.ALLIGNMENT.get();
		}
	}

	public void setObstacleAvoidanceOff() {
		if (__isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
			__behaviorFlag ^= Behavior.OBSTACLE_AVOIDANCE.get();
		}
	}

	public void setWallAvoidanceOff() {
		if (__isBehavior(Behavior.WALL_AVOIDANCE)) {
			__behaviorFlag ^= Behavior.WALL_AVOIDANCE.get();
		}
	}

	public void setFollowPathOff() {
		if (__isBehavior(Behavior.FOLLOW_PATH)) {
			__behaviorFlag ^= Behavior.FOLLOW_PATH.get();
		}
	}

	public void setInterposeOff() {
		if (__isBehavior(Behavior.INTERPOSE)) {
			__behaviorFlag ^= Behavior.INTERPOSE.get();
		}
	}

	public void setHideOff() {
		if (__isBehavior(Behavior.HIDE)) {
			__behaviorFlag ^= Behavior.HIDE.get();
		}
	}

	public void setOffsetPursuitOff() {
		if (__isBehavior(Behavior.OFFSET_PURSUIT)) {
			__behaviorFlag ^= Behavior.OFFSET_PURSUIT.get();
		}
	}

	public void setFlockingOff() {
		setCohesionOff();
		setAlignmentOff();
		setSeparationOff();
		setWanderOff();
	}

	public boolean isFleeOn() {
		return __isBehavior(Behavior.FLEE);
	}

	public boolean isSeekOn() {
		return __isBehavior(Behavior.SEEK);
	}

	public boolean isArriveOn() {
		return __isBehavior(Behavior.ARRIVE);
	}

	public boolean isWanderOn() {
		return __isBehavior(Behavior.WANDER);
	}

	public boolean isPursuitOn() {
		return __isBehavior(Behavior.PURSUIT);
	}

	public boolean isEvadeOn() {
		return __isBehavior(Behavior.EVADE);
	}

	public boolean isCohesionOn() {
		return __isBehavior(Behavior.COHESION);
	}

	public boolean isSeparationOn() {
		return __isBehavior(Behavior.SEPARATION);
	}

	public boolean isAlignmentOn() {
		return __isBehavior(Behavior.ALLIGNMENT);
	}

	public boolean isObstacleAvoidanceOn() {
		return __isBehavior(Behavior.OBSTACLE_AVOIDANCE);
	}

	public boolean isWallAvoidanceOn() {
		return __isBehavior(Behavior.WALL_AVOIDANCE);
	}

	public boolean isFollowPathOn() {
		return __isBehavior(Behavior.FOLLOW_PATH);
	}

	public boolean isInterposeOn() {
		return __isBehavior(Behavior.INTERPOSE);
	}

	public boolean isHideOn() {
		return __isBehavior(Behavior.HIDE);
	}

	public boolean isOffsetPursuitOn() {
		return __isBehavior(Behavior.OFFSET_PURSUIT);
	}

	public float getDetectBoxLength() {
		return __detectBoxLength;
	}

	public List<Vector2> getSensors() {
		return __sensors;
	}

	public float getWanderJitter() {
		return __wanderJitter;
	}

	public float getWanderDistance() {
		return __wanderDistance;
	}

	public float getWanderRadius() {
		return __wanderRadius;
	}

	public float getSeparationWeight() {
		return __weightSeparation;
	}

	public float getAlignmentWeight() {
		return __weightAlignment;
	}

	public float getCohesionWeight() {
		return __weightCohesion;
	}

}
