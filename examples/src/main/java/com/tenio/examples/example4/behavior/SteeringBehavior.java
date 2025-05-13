package com.tenio.examples.example4.behavior;

import com.tenio.common.utility.MathUtility;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.Path;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utility.Geometry;
import com.tenio.engine.physic2d.utility.Transformation;
import com.tenio.examples.example4.configuration.ParamLoader;
import com.tenio.examples.example4.constant.Behavior;
import com.tenio.examples.example4.constant.Deceleration;
import com.tenio.examples.example4.constant.SummingMethod;
import com.tenio.examples.example4.entity.Vehicle;
import com.tenio.examples.example4.entity.Wall;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to encapsulate steering behaviors for a vehicle.
 *
 * @see Vehicle
 */
public final class SteeringBehavior implements Renderable {

  // the radius of the constraining circle for to wander behavior
  public static final float WANDER_RADIUS = 1.2f;
  // distance to wander circle is projected in front of the agent
  public static final float WANDER_DISTANCE = 2;
  // the maximum amount of displacement along the circle each frame
  public static final float WANDER_JITTER_PER_SECOND = 80;
  // used in path following
  public static final float WAYPOINT_SEEK_DISTANCE = 20;

  private final ParamLoader paramLoader = ParamLoader.getInstance();

  // a pointer to the owner of this instance
  private final Vehicle vehicle;
  // a vertex buffer to contain the feelers for wall avoidance
  private final List<Vector2> sensors;
  // the length of the 'feeler/s' used in wall detection
  private final float wallDetectionSensorLength;
  // the current position on to wander circle the agent is
  // attempting to steer towards
  private final Vector2 wanderTarget;
  // explained above
  private final float wanderJitter;
  private final float wanderRadius;
  private final float wanderDistance;
  // multipliers. These can be adjusted to effect strength of the
  // appropriate behavior. Useful to get flocking the way you require
  // for example.
  private final float weightSeparation;
  private final float weightCohesion;
  private final float weightAlignment;
  private final float weightWander;
  private final float weightObstacleAvoidance;
  private final float weightWallAvoidance;
  private final float weightSeek;
  private final float weightFlee;
  private final float weightArrive;
  private final float weightPursuit;
  private final float weightOffsetPursuit;
  private final float weightInterpose;
  private final float weightHide;
  private final float weightEvade;
  private final float weightFollowPath;
  // pointer to any current path
  private final Path path;
  // the distance (squared) a vehicle has to be from a path way-point before
  // it starts seeking to the next way-point
  private final float waypointSeekDistanceSqr;
  // default
  private final Deceleration deceleration;
  /**
   * This function calculates how much of its max steering force the vehicle has
   * left to apply and then applies that amount of the force to add.
   */
  private final Vector2 vAccumulateForce = Vector2.newInstance();
  // the steering force created by the combined effect of all
  // the selected behaviors
  private Vector2 steeringForce = Vector2.newInstance();
  // these can be used to keep track of friends, pursuers, or prey
  private Vehicle targetAgent1;
  private Vehicle targetAgent2;
  // length of the 'detection box' utilized in obstacle avoidance
  private float detectBoxLength;
  // any offset used for formations or offset pursuit
  private Vector2 offset;
  // binary flags to indicate whether a behavior should be active
  private int behaviorFlag;
  // is cell space partitioning to be used or not?
  private boolean enableCellSpace;
  // what type of method is used to sum any active behavior
  private SummingMethod summingMethod;
  // a vertex buffer rqd for drawing the detection box
  private List<Vector2> detectBox = new ArrayList<>(4);

  public SteeringBehavior(Vehicle agent) {

    vehicle = agent;
    behaviorFlag = 0;
    detectBoxLength = paramLoader.MIN_DETECTION_BOX_LENGTH;
    weightCohesion = paramLoader.COHESION_WEIGHT;
    weightAlignment = paramLoader.ALIGNMENT_WEIGHT;
    weightSeparation = paramLoader.SEPARATION_WEIGHT;
    weightObstacleAvoidance = paramLoader.OBSTACLE_AVOIDANCE_WEIGHT;
    weightWander = paramLoader.WANDER_WEIGHT;
    weightWallAvoidance = paramLoader.WALL_AVOIDANCE_WEIGHT;
    wallDetectionSensorLength = paramLoader.WALL_DETECTION_FEELER_LENGTH;
    sensors = new ArrayList<>(3);
    deceleration = Deceleration.NORMAL;
    targetAgent1 = null;
    targetAgent2 = null;
    wanderDistance = WANDER_DISTANCE;
    wanderJitter = WANDER_JITTER_PER_SECOND;
    wanderRadius = WANDER_RADIUS;
    waypointSeekDistanceSqr = WAYPOINT_SEEK_DISTANCE * WAYPOINT_SEEK_DISTANCE;
    weightSeek = paramLoader.SEEK_WEIGHT;
    weightFlee = paramLoader.FLEE_WEIGHT;
    weightArrive = paramLoader.ARRIVE_WEIGHT;
    weightPursuit = paramLoader.PURSUIT_WEIGHT;
    weightOffsetPursuit = paramLoader.OFFSET_PURSUIT_WEIGHT;
    weightInterpose = paramLoader.INTERPOSE_WEIGHT;
    weightHide = paramLoader.HIDE_WEIGHT;
    weightEvade = paramLoader.EVADE_WEIGHT;
    weightFollowPath = paramLoader.FOLLOW_PATH_WEIGHT;
    enableCellSpace = false;
    summingMethod = SummingMethod.PRIORITIZED;

    // stuff for to wander behavior
    float theta = MathUtility.randFloat() * MathUtility.TWO_PI;

    // create a vector to a target position on to wander circle
    wanderTarget = Vector2.valueOf((float) (wanderRadius * Math.cos(theta)),
        (float) (wanderRadius * Math.sin(theta)));

    // create a Path
    path = new Path();
    path.enableLoop(true);

  }

  // this function tests if a specific bit of m_iFlags is set
  private boolean isBehavior(Behavior behavior) {
    return (behaviorFlag & behavior.get()) == behavior.get();
  }

  private void accumulateForce(Vector2 forceToAdd) {

    // calculate how much steering force the vehicle has used so far
    float magnitudeSoFar = steeringForce.getLength();

    // calculate how much steering force remains to be used by this vehicle
    float magnitudeRemaining = vehicle.getMaxForce() - magnitudeSoFar;

    // return false if there is no more force left to use
    if (magnitudeRemaining <= 0) {
      return;
    }

    // calculate the magnitude of the force we want to add
    float magnitudeToAdd = forceToAdd.getLength();

    // if the magnitude of the sum of ForceToAdd and the running total
    // does not exceed the maximum force available to this vehicle, just
    // add together. Otherwise, add as much of the ForceToAdd vector is
    // possible without going over the max.
    if (magnitudeToAdd < magnitudeRemaining) {
      steeringForce.add(forceToAdd);
    } else {
      // add it to the steering force
      vAccumulateForce.set(forceToAdd).normalize().mul(magnitudeRemaining);
      steeringForce.add(vAccumulateForce);
    }

  }

  /**
   * Creates the antenna utilized by WallAvoidance.
   */
  private void createSensors() {
    sensors.clear();
    // feeler pointing straight in front
    var front = Vector2.valueOf(vehicle.getHeading()).mul(wallDetectionSensorLength)
        .add(vehicle.getPosition());
    sensors.add(front);

    // feeler to left
    var left = Transformation.vec2dRotateAroundOrigin(
        vehicle.getHeading(), MathUtility.HALF_PI * 3.5f);
    left.mul(wallDetectionSensorLength / 2.0f).add(vehicle.getPosition());
    sensors.add(left);

    // feeler to right
    var right = Transformation.vec2dRotateAroundOrigin(
        vehicle.getHeading(), MathUtility.HALF_PI * 0.5f);
    right.mul(wallDetectionSensorLength / 2.0f).add(vehicle.getPosition());
    sensors.add(right);
  }

  // ---------------------------- START OF BEHAVIORS ----------------------------
  //
  // ----------------------------------------------------------------------------

  /**
   * Given a target, this behavior returns a steering force which will direct the
   * agent towards the target.
   */
  private Vector2 doSeek(Vector2 targetPos) {
    var desiredVelocity = Vector2.valueOf(targetPos).sub(vehicle.getPosition()).normalize()
        .mul(vehicle.getMaxSpeed());
    return desiredVelocity.sub(vehicle.getVelocity());
  }

  /**
   * Does the opposite of Seek.
   */
  private Vector2 doFlee(Vector2 targetPos) {
    // only flee if the target is within 'panic distance'. Work in distance squared
    // space.
    var desiredVelocity = Vector2.valueOf(vehicle.getPosition()).sub(targetPos).normalize()
        .mul(vehicle.getMaxSpeed());
    return desiredVelocity.sub(vehicle.getVelocity());
  }

  /**
   * This behavior is similar to seek, but it attempts to arrive at the target with
   * a zero velocity.
   */
  private Vector2 doArrive(Vector2 targetPos, Deceleration deceleration) {
    var toTarget = Vector2.valueOf(targetPos).sub(vehicle.getPosition());

    // calculate the distance to the target
    float dist = toTarget.getLength();

    if (dist > 0) {
      // because Deceleration is enumerated as an integer, this value is required
      // to provide fine tweaking of the deceleration.
      final float decelerationTweaker = 0.3f;

      // calculate the speed required to reach the target given the desired
      // deceleration
      float speed = dist / ((float) deceleration.get() * decelerationTweaker);

      // make sure the velocity does not exceed the max
      speed = Math.min(speed, vehicle.getMaxSpeed());

      // from here proceed just like Seek except we don't need to normalize
      // the ToTarget vector because we have already gone to the trouble
      // of calculating its length: dist.
      var desiredVelocity = toTarget.mul(speed / dist);

      return desiredVelocity.sub(vehicle.getVelocity());
    }

    return toTarget.zero();
  }

  /**
   * This behavior creates a force that steers the agent towards the evader.
   */
  private Vector2 doPursuit(Vehicle evader) {
    // if the evader is ahead and facing the agent then we can just seek
    // for the evader's current position.
    var toEvader = evader.getPosition().sub(vehicle.getPosition());

    float relativeHeading = vehicle.getHeading().getDotProductValue(evader.getHeading());

    if ((toEvader.getDotProductValue(vehicle.getHeading()) > 0) &&
        (relativeHeading < -0.95)) // acos(0.95)=18degs
    {
      return doSeek(evader.getPosition());
    }

    // Not considered ahead so we predict where the evader will be.

    // the lookahead time is proportional to the distance between the evader
    // and the pursuer; and is inversely proportional to the sum of the
    // agent's velocities
    float lookAheadTime = toEvader.getLength() / (vehicle.getMaxSpeed() + evader.getSpeed());

    // now seek to the predicted future position of the evader
    return doSeek(evader.getVelocity().mul(lookAheadTime).add(evader.getPosition()));
  }

  /**
   * Similar to pursuit except the agent Flees from the estimated future position
   * of the pursuer.
   */
  private Vector2 doEvade(final Vehicle pursuer) {
    // Not necessary to include the check for facing direction this time
    var toPursuer = pursuer.getPosition().sub(vehicle.getPosition());

    // uncomment the following two lines to have Evaded only consider pursuers
    // within a 'threat range'
    final float threatRange = 100;
    if (toPursuer.getLengthSqr() > threatRange * threatRange) {
      return Vector2.newInstance();
    }

    // the lookahead time is proportional to the distance between the pursuer
    // and the pursuer; and is inversely proportional to the sum of the
    // agents' velocities
    float lookAheadTime = toPursuer.getLength() / (vehicle.getMaxSpeed() + pursuer.getSpeed());

    // now flee away from predicted future position of the pursuer
    return doFlee(pursuer.getVelocity().mul(lookAheadTime).add(pursuer.getPosition()));
  }

  /**
   * This behavior makes the agent wander about randomly
   */
  private Vector2 doWander() {
    // this behavior is dependent on the update rate, so this line must
    // be included when using time independent frame rate.
    float jitterThisTimeSlice = wanderJitter * vehicle.getTimeElapsed();

    // first, add a small random vector to the target's position
    var temp2 = Vector2.newInstance().set(wanderTarget)
        .add(MathUtility.randomClamped() * jitterThisTimeSlice,
            MathUtility.randomClamped() * jitterThisTimeSlice);

    // re-project this new vector back on to a unit circle
    temp2.normalize();

    // increase the length of the vector to the same as the radius
    // of to wander circle
    temp2.mul(wanderRadius);

    // move the target into a position WanderDist in front of the agent
    var target = Vector2.newInstance().set(wanderDistance, 0).add(temp2);

    // project the target into world space
    var targetToWorldSpace =
        Transformation.pointToWorldSpace(target, vehicle.getHeading(), vehicle.getSide(),
            vehicle.getPosition());

    // and steer towards it
    return targetToWorldSpace.sub(vehicle.getPosition());
  }

  /**
   * Given a vector of obstacles, this method returns a steering force that will
   * prevent the agent colliding with the closest obstacle.
   */
  private Vector2 doObstacleAvoidance(List<BaseGameEntity> obstacles) {
    // the detection box length is proportional to the agent's velocity
    detectBoxLength = paramLoader.MIN_DETECTION_BOX_LENGTH
        + (vehicle.getSpeed() / vehicle.getMaxSpeed()) * paramLoader.MIN_DETECTION_BOX_LENGTH;

    // tag all obstacles within range of the box for processing
    vehicle.getWorld().tagObstaclesWithinViewRange(vehicle, detectBoxLength);

    // this will keep track of the closest intersecting obstacle (CIB)
    BaseGameEntity closestIntersectingObstacle = null;

    // this will be used to track the distance to the CIB
    float distToClosestIP = MathUtility.MAX_FLOAT;

    // this will record the transformed local coordinates of the CIB
    var localPosOfClosestObstacle = Vector2.newInstance().zero();

    var it = obstacles.listIterator();

    while (it.hasNext()) {
      // if the obstacle has been tagged within range proceed
      var curOb = it.next();
      if (curOb.isTagged()) {
        // calculate this obstacle's position in local space
        var localPos = Transformation.pointToLocalSpace(curOb.getPosition(), vehicle.getHeading(),
            vehicle.getSide(), vehicle.getPosition());

        // if the local position has a negative x value then it must lay
        // behind the agent. (in which case it can be ignored)
        if (localPos.x >= 0) {
          // if the distance from the x-axis to the object's position is less
          // than its radius + half the width of the detection box then there
          // is a potential intersection.
          float expandedRadius = curOb.getBoundingRadius() + vehicle.getBoundingRadius();

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
    var steeringForce = Vector2.newInstance().zero();

    if (closestIntersectingObstacle != null) {
      // the closer the agent is to an object, the stronger the
      // steering force should be
      float multiplier = 1 + (detectBoxLength - localPosOfClosestObstacle.x) /
          detectBoxLength;

      // calculate the lateral force
      steeringForce.y =
          (closestIntersectingObstacle.getBoundingRadius() - localPosOfClosestObstacle.y)
              * multiplier;

      // apply a braking force proportional to the obstacles distance from
      // the vehicle.
      final float brakingWeight = 0.2f;

      steeringForce.x =
          (closestIntersectingObstacle.getBoundingRadius() - localPosOfClosestObstacle.x)
              * brakingWeight;
    }

    // finally, convert the steering vector from local to world space
    return Transformation.vectorToWorldSpace(steeringForce, vehicle.getHeading(),
        vehicle.getSide());
  }

  /**
   * This returns a steering force that will keep the agent away from any walls it
   * may encounter.
   */
  private Vector2 doWallAvoidance(List<Wall> walls) {
    // the feelers are contained in a list, m_Feelers
    createSensors();

    float distToThisIP = 0;
    float distToClosestIP = MathUtility.MAX_FLOAT;

    // this will hold an index into the vector of walls
    int closestWall = -1;

    var steeringForce = Vector2.newInstance().zero();
    var closestPoint = Vector2.newInstance().zero(); // holds the closest intersection point

    // examine each feeler in turn
    for (Vector2 sensor : sensors) {
      // run through each wall checking for any intersection points
      for (int w = 0; w < walls.size(); ++w) {
        distToThisIP =
            Geometry.getDistanceTwoSegmentIntersect(vehicle.getPosition(), sensor,
                walls.get(w).getFrom(), walls.get(w).getTo());
        if (distToThisIP != -1) {
          // is this the closest found so far? If so keep a record
          if (distToThisIP < distToClosestIP) {
            distToClosestIP = distToThisIP;

            closestWall = w;

            closestPoint =
                Geometry.getPointTwoSegmentIntersect(vehicle.getPosition(), sensor,
                    walls.get(w).getFrom(), walls.get(w).getTo());
          }
        }
      } // next wall

      // if an intersection point has been detected, calculate a force
      // that will direct the agent away
      if (closestWall >= 0) {
        // calculate by what distance the projected position of the agent
        // will overshoot the wall
        var overShoot = sensor.sub(closestPoint);

        // create a force in the direction of the wall normal, with a
        // magnitude of the overshoot
        steeringForce =
            Vector2.newInstance().set(walls.get(closestWall).getNormal())
                .mul(overShoot.getLength());
      }

    } // next feeler

    return steeringForce.clone();
  }

  /**
   * This calculates a force re-pelling from the other neighbors.
   */
  private Vector2 doSeparation(List<Vehicle> neighbors) {
    var steeringForce = Vector2.newInstance().zero();

    for (Vehicle neighbor : neighbors) {
      // make sure this agent isn't included in the calculations and that
      // the agent being examined is close enough. ***also make sure it doesn't
      // include to evade target ***
      if ((neighbor != vehicle) && neighbor.isTagged()
          && (neighbor != targetAgent1)) {
        var toAgent =
            Vector2.newInstance().set(vehicle.getPosition()).sub(neighbor.getPosition());

        // scale the force inversely proportional to the agents distance
        // from its neighbor.
        steeringForce.add(toAgent.normalize().div(toAgent.getLength()));
      }
    }

    return steeringForce.clone();
  }

  /**
   * Returns a force that attempts to align this agents heading with that of its
   * neighbors.
   */
  private Vector2 doAlignment(List<Vehicle> neighbors) {
    // used to record the average heading of the neighbors
    var averageHeading = Vector2.newInstance().zero();

    // used to count the number of vehicles in the neighborhood
    int neighborCount = 0;

    // iterate through all the tagged vehicles and sum their heading vectors
    for (Vehicle neighbor : neighbors) {
      // make sure *this* agent isn't included in the calculations and that
      // the agent being examined is close enough ***also make sure it doesn't
      // include any evade target ***
      if ((neighbor != vehicle) && neighbor.isTagged()
          && (neighbor != targetAgent1)) {
        averageHeading.add(neighbor.getHeading());

        ++neighborCount;
      }
    }

    // if the neighborhood contained one or more vehicles, average their
    // heading vectors.
    if (neighborCount > 0) {
      averageHeading.div((float) neighborCount);
      averageHeading.sub(vehicle.getHeading());
    }

    return averageHeading.clone();
  }

  /**
   * Returns a steering force that attempts to move the agent towards the center
   * of mass of the agents in its immediate area.
   */
  private Vector2 doCohesion(final List<Vehicle> neighbors) {
    // first find the center of mass of all the agents
    var centerOfMass = Vector2.newInstance().zero();
    var steeringForce = Vector2.newInstance().zero();

    int neighborCount = 0;

    // iterate through the neighbors and sum up all the position vectors
    for (Vehicle neighbor : neighbors) {
      // make sure *this* agent isn't included in the calculations and that
      // the agent being examined is close enough ***also make sure it doesn't
      // include the evade target ***
      if ((neighbor != vehicle) && neighbor.isTagged()
          && (neighbor != targetAgent1)) {
        centerOfMass.add(neighbor.getPosition());

        ++neighborCount;
      }
    }

    if (neighborCount > 0) {
      // the center of mass is the average of the sum of positions
      centerOfMass.div((float) neighborCount);

      // now seek towards that position
      steeringForce = doSeek(centerOfMass);
    }

    // the magnitude of cohesion is usually much larger than separation or
    // alignment, so it usually helps to normalize it.
    return steeringForce.normalize();
  }

  /*
   * NOTE: the next three behaviors are the same as the above three, except that
   * they use a cell-space partition to find the neighbors
   */

  /**
   * This calculates a force re-pelling from the other neighbors.
   * <br>
   * USES SPACIAL PARTITIONING
   */
  private Vector2 doSeparationPlus(List<Vehicle> neighbors) {
    var steeringForce = Vector2.newInstance().zero();

    // iterate through the neighbors and sum up all the position vectors
    for (var pV = vehicle.getWorld().getCellSpace().getFrontOfNeighbor();
         !vehicle.getWorld().getCellSpace()
             .isEndOfNeighbors(); pV = vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
      // make sure this agent isn't included in the calculations and that
      // the agent being examined is close enough
      if (pV != vehicle) {
        var toAgent = Vector2.newInstance().set(vehicle.getPosition()).sub(pV.getPosition());

        // scale the force inversely proportional to the agents distance
        // from its neighbor.
        steeringForce.add(toAgent.normalize().div(toAgent.getLength()));
      }

    }

    return steeringForce.clone();
  }

  /**
   * Returns a force that attempts to align this agents heading with that of its
   * neighbors.
   * <br>
   * USES SPACIAL PARTITIONING
   */
  private Vector2 doAlignmentPlus(List<Vehicle> neighbors) {
    // This will record the average heading of the neighbors
    var averageHeading = Vector2.newInstance().zero();

    // This count the number of vehicles in the neighborhood
    float neighborCount = 0;

    // iterate through the neighbors and sum up all the position vectors
    for (var pV = vehicle.getWorld().getCellSpace().getFrontOfNeighbor();
         !vehicle.getWorld().getCellSpace()
             .isEndOfNeighbors(); pV = vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
      // make sure *this* agent isn't included in the calculations and that
      // the agent being examined is close enough
      if (pV != vehicle) {
        averageHeading.add(pV.getHeading());
        ++neighborCount;
      }
    }

    // if the neighborhood contained one or more vehicles, average their
    // heading vectors.
    if (neighborCount > 0) {
      averageHeading.div(neighborCount);
      averageHeading.sub(vehicle.getHeading());
    }

    return averageHeading.clone();
  }

  /**
   * Returns a steering force that attempts to move the agent towards the center
   * of mass of the agents in its immediate area.
   * <br>
   * USES SPACIAL PARTITIONING
   */
  private Vector2 doCohesionPlus(final List<Vehicle> neighbors) {
    // first find the center of mass of all the agents
    var centerOfMass = Vector2.newInstance().zero();
    var steeringForce = Vector2.newInstance().zero();

    int neighborCount = 0;

    // iterate through the neighbors and sum up all the position vectors
    for (var pV = vehicle.getWorld().getCellSpace().getFrontOfNeighbor();
         !vehicle.getWorld().getCellSpace()
             .isEndOfNeighbors(); pV = vehicle.getWorld().getCellSpace().getNextOfNeighbor()) {
      // make sure *this* agent isn't included in the calculations and that
      // the agent being examined is close enough
      if (pV != vehicle) {
        centerOfMass.add(pV.getPosition());

        ++neighborCount;
      }
    }

    if (neighborCount > 0) {
      // the center of mass is the average of the sum of positions
      centerOfMass.div((float) neighborCount);

      // now seek towards that position
      steeringForce = doSeek(centerOfMass);
    }

    // the magnitude of cohesion is usually much larger than separation or
    // alignment, so it usually helps to normalize it.
    return steeringForce.normalize();
  }

  /**
   * Given two agents, this method returns a force that attempts to position the
   * vehicle between them.
   */
  private Vector2 doInterpose(final Vehicle agentA, final Vehicle agentB) {
    // first we need to figure out where the two agents are going to be at
    // time T in the future. This is approximated by determining the time
    // taken to reach the midway point at the current time at max speed.
    var midPoint = Vector2.newInstance().set(agentA.getPosition()).add(agentB.getPosition()).div(2);

    float timeToReachMidPoint =
        vehicle.getPosition().getDistanceValue(midPoint) / vehicle.getMaxSpeed();

    // now we have T, we assume that agent A and agent B will continue on a
    // straight trajectory and extrapolate to get their future positions
    var aPos = Vector2.newInstance().set(agentA.getVelocity()).mul(timeToReachMidPoint)
        .add(agentA.getPosition());
    var bPos = Vector2.newInstance().set(agentB.getVelocity()).mul(timeToReachMidPoint)
        .add(agentB.getPosition());

    // calculate the mid-point of these predicted positions
    midPoint = Vector2.newInstance().set(aPos).add(bPos).div(2);

    // then steer to Arrive at it
    return doArrive(midPoint, Deceleration.FAST);
  }

  private Vector2 doHide(final Vehicle hunter, final List<BaseGameEntity> obstacles) {
    float distToClosest = MathUtility.MAX_FLOAT;
    var bestHidingSpot = Vector2.newInstance().zero();

    var it = obstacles.listIterator();

    while (it.hasNext()) {
      var curOb = it.next();
      // calculate the position of the hiding spot for this obstacle
      var hidingSpot =
          getHidingPosition(curOb.getPosition(), curOb.getBoundingRadius(), hunter.getPosition());

      // work in distance-squared space to find the closest hiding
      // spot to the agent
      float dist = hidingSpot.getDistanceSqrValue(vehicle.getPosition());

      if (dist < distToClosest) {
        distToClosest = dist;
        bestHidingSpot = hidingSpot;
      }
    } // end while

    // if no suitable obstacles found then Evade the hunter
    if (distToClosest == MathUtility.MAX_FLOAT) {
      return doEvade(hunter);
    }

    // else use Arrive at the hiding spot
    return doArrive(bestHidingSpot, Deceleration.FAST);
  }

  /**
   * Given the position of a hunter, and the position and radius of an obstacle,
   * this method calculates a position DistanceFromBoundary away from its bounding
   * radius and directly opposite the hunter.
   */
  private Vector2 getHidingPosition(final Vector2 posOb, final float radiusOb,
                                    final Vector2 posHunter) {
    // calculate how far away the agent is to be from the chosen obstacle's
    // bounding radius
    final float distanceFromBoundary = 30;
    float distAway = radiusOb + distanceFromBoundary;

    // calculate the heading toward the object from the hunter
    var toOb = Vector2.newInstance().set(posOb).sub(posHunter).normalize();

    // scale it to size and add to the obstacles position to get
    // the hiding spot.
    return toOb.mul(distAway).add(posOb).clone();
  }

  /**
   * Given a series of Vector2Ds, this method produces a force that will move the
   * agent along the way-points in order. The agent uses the 'Seek' behavior to
   * move to the next way-point - unless it is the last way-point, in which case
   * it 'Arrives'.
   */
  private Vector2 doFollowPath() {
    // move to next target if close enough to current target (working in
    // distance squared space)
    if (path.getCurrentWayPoint().getDistanceSqrValue(vehicle.getPosition()) <
        waypointSeekDistanceSqr) {
      path.setToNextWayPoint();
    }

    if (!path.isEndOfWayPoints()) {
      return doSeek(path.getCurrentWayPoint());
    } else {
      return doArrive(path.getCurrentWayPoint(), Deceleration.NORMAL);
    }
  }

  /**
   * Produces a steering force that keeps a vehicle at a specified offset from a
   * leader vehicle.
   */
  private Vector2 doOffsetPursuit(final Vehicle leader, final Vector2 offset) {
    // calculate the offset's position in world space
    var worldOffsetPos =
        Transformation.pointToWorldSpace(offset, leader.getHeading(), leader.getSide(),
            leader.getPosition());

    var toOffset = worldOffsetPos.sub(vehicle.getPosition());

    // the lookahead time is proportional to the distance between the leader
    // and the pursuer; and is inversely proportional to the sum of both
    // agent's velocities
    float lookAheadTime = toOffset.getLength() / (vehicle.getMaxSpeed() + leader.getSpeed());

    // now Arrive at the predicted future position of the offset
    return doArrive(leader.getVelocity().mul(lookAheadTime).add(worldOffsetPos),
        Deceleration.FAST);
  }

  /**
   * Renders visual aids and info for seeing how each behavior is calculated.
   */
  @Override
  public void render(Paint paint) {
    paint.enableOpaqueText(false);
    paint.setTextColor(Color.GRAY);

    int nextSlot = paint.getFontHeight();
    int slotSize = 20;

    if (vehicle.getMaxForce() < 0) {
      vehicle.setMaxForce(0.0f);
    }
    if (vehicle.getMaxSpeed() < 0) {
      vehicle.setMaxSpeed(0.0f);
    }

    if (Objects.equals(vehicle.getId(), "dragon")) {
      paint.drawTextAtPosition(5, nextSlot, "MaxForce(Ins/Del):");
      paint.drawTextAtPosition(160, nextSlot,
          String.valueOf(vehicle.getMaxForce() / paramLoader.STEERING_FORCE_TWEAKER));
      nextSlot += slotSize;
    }
    if (Objects.equals(vehicle.getId(), "dragon")) {
      paint.drawTextAtPosition(5, nextSlot, "MaxSpeed(Home/End):");
      paint.drawTextAtPosition(160, nextSlot, String.valueOf(vehicle.getMaxSpeed()));
      nextSlot += slotSize;
    }

    // render the steering force
    if (vehicle.getWorld().isRenderSteeringForce()) {
      paint.setPenColor(Color.RED);
      var F = Vector2.newInstance().set(steeringForce).div(paramLoader.STEERING_FORCE_TWEAKER)
          .mul(paramLoader.VEHICLE_SCALE);
      paint.drawLine(vehicle.getPosition(),
          Vector2.newInstance().set(vehicle.getPosition().add(F)));
    }

    // render wander stuff if relevant
    if (isBehavior(Behavior.WANDER) && vehicle.getWorld().isRenderWanderCircle()) {

      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Jitter(F/V): ");
        paint.drawTextAtPosition(160, nextSlot, String.valueOf(wanderJitter));
        nextSlot += slotSize;
      }
      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Distance(G/B): ");
        paint.drawTextAtPosition(160, nextSlot, String.valueOf(wanderDistance));
        nextSlot += slotSize;
      }
      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Radius(H/N): ");
        paint.drawTextAtPosition(160, nextSlot, String.valueOf(wanderRadius));
        nextSlot += slotSize;
      }

      // calculate the center of to wander circle
      var vTCC = Transformation.pointToWorldSpace(
          Vector2.newInstance().set(wanderDistance * vehicle.getBoundingRadius(), 0),
          vehicle.getHeading(),
          vehicle.getSide(), vehicle.getPosition());
      // draw to wander circle
      paint.setPenColor(Color.GREEN);
      paint.setBgColor(null);
      paint.drawCircle(vTCC, wanderRadius * vehicle.getBoundingRadius());

      // draw to wander target
      paint.setPenColor(Color.RED);
      paint.drawCircle(Transformation.pointToWorldSpace(
          Vector2.newInstance().set(wanderTarget).add(wanderDistance, 0)
              .mul(vehicle.getBoundingRadius()),
          vehicle.getHeading(), vehicle.getSide(), vehicle.getPosition()), 3);
    }

    // render the detection box if relevant
    if (vehicle.getWorld().isRenderDetectionBox()) {

      paint.setPenColor(Color.GRAY);

      float length = paramLoader.MIN_DETECTION_BOX_LENGTH
          + (vehicle.getSpeed() / vehicle.getMaxSpeed()) * paramLoader.MIN_DETECTION_BOX_LENGTH;

      // vets for the detection box buffer
      detectBox.clear();
      detectBox.add(Vector2.newInstance().set(0, vehicle.getBoundingRadius()));
      detectBox.add(Vector2.newInstance().set(length, vehicle.getBoundingRadius()));
      detectBox.add(Vector2.newInstance().set(length, -vehicle.getBoundingRadius()));
      detectBox.add(Vector2.newInstance().set(0, -vehicle.getBoundingRadius()));

      if (!vehicle.isSmoothing()) {
        detectBox = Transformation.pointsToWorldSpace(detectBox, vehicle.getPosition(),
            vehicle.getHeading(), vehicle.getSide());
        paint.drawClosedShape(detectBox);
      } else {
        detectBox = Transformation.pointsToWorldSpace(detectBox, vehicle.getPosition(),
            vehicle.getSmoothedHeading(),
            Vector2.newInstance().set(vehicle.getSmoothedHeading()).perpendicular());
        paint.drawClosedShape(detectBox);
      }

      // the detection box length is proportional to the agent's velocity
      detectBoxLength = paramLoader.MIN_DETECTION_BOX_LENGTH
          + (vehicle.getSpeed() / vehicle.getMaxSpeed()) * paramLoader.MIN_DETECTION_BOX_LENGTH;

      // tag all obstacles within range of the box for processing
      vehicle.getWorld().tagObstaclesWithinViewRange(vehicle, detectBoxLength);

      var it = vehicle.getWorld().getObstacles().listIterator();

      while (it.hasNext()) {
        var curOb = it.next();
        // if the obstacle has been tagged within range proceed
        if (curOb.isTagged()) {
          // calculate this obstacle's position in local space
          var localPos = Transformation.pointToLocalSpace(curOb.getPosition(), vehicle.getHeading(),
              vehicle.getSide(), vehicle.getPosition());

          // if the local position has a negative x value then it must lay
          // behind the agent. (in which case it can be ignored)
          if (localPos.x >= 0) {
            // if the distance from the x-axis to the object's position is less
            // than its radius + half the width of the detection box then there
            // is a potential intersection.
            if (Math.abs(localPos.y) < (curOb.getBoundingRadius() + vehicle.getBoundingRadius())) {
              paint.setPenColor(Color.RED);
              paint.drawClosedShape(detectBox);
            }
          }
        }
      }
    }

    // render the wall avoidance feelers
    if (isBehavior(Behavior.WALL_AVOIDANCE) && vehicle.getWorld().isRenderSensors()) {
      paint.setPenColor(Color.ORANGE);
      for (Vector2 sensor : sensors) {
        paint.drawLine(vehicle.getPosition(), sensor);
      }
    }

    // render path info
    if (isBehavior(Behavior.FOLLOW_PATH) && vehicle.getWorld().isRenderPath()) {
      path.render(paint);
    }

    if (isBehavior(Behavior.SEPARATION)) {
      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Separation(S/X):");
        paint.drawTextAtPosition(160, nextSlot,
            String.valueOf(weightSeparation / paramLoader.STEERING_FORCE_TWEAKER));
        nextSlot += slotSize;
      }
    }

    if (isBehavior(Behavior.ALIGNMENT)) {
      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Alignment(A/Z):");
        paint.drawTextAtPosition(160, nextSlot,
            String.valueOf(weightAlignment / paramLoader.STEERING_FORCE_TWEAKER));
        nextSlot += slotSize;
      }
    }

    if (isBehavior(Behavior.COHESION)) {
      if (Objects.equals(vehicle.getId(), "dragon")) {
        paint.drawTextAtPosition(5, nextSlot, "Cohesion(D/C):");
        paint.drawTextAtPosition(160, nextSlot,
            String.valueOf(weightCohesion / paramLoader.STEERING_FORCE_TWEAKER));
        nextSlot += slotSize;
      }
    }

    if (isBehavior(Behavior.FOLLOW_PATH)) {
      float sd = (float) Math.sqrt(waypointSeekDistanceSqr);
      if (Objects.equals(vehicle.getId(), "dragon")) {
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
   * m_SummingMethod.
   */
  public Vector2 calculateAccumulate() {
    // reset the steering force
    steeringForce.zero();

    // use space partitioning to calculate the neighbors of this vehicle
    // if switched on. If not, use the standard tagging system
    if (!isSpacePartitioning()) {
      // tag neighbors if any of the following 3 group behaviors are switched on
      if (isBehavior(Behavior.SEPARATION) || isBehavior(Behavior.ALIGNMENT)
          || isBehavior(Behavior.COHESION)) {
        // vehicle.getWorld().tagVehiclesWithinViewRange(vehicle, 0.01f);
      }
    } else {
      // calculate neighbors in cell-space if any of the following 3 group
      // behaviors are switched on
      if (isBehavior(Behavior.SEPARATION) || isBehavior(Behavior.ALIGNMENT)
          || isBehavior(Behavior.COHESION)) {
        vehicle.getWorld().getCellSpace().calculateNeighbors(vehicle.getPosition(),
            10);
      }
    }

    switch (summingMethod) {
      case WEIGHTED_AVERAGE:
        steeringForce = getCalculateWeightedSum();
        break;

      case PRIORITIZED:
        calculatePrioritized();
        break;

      case DITHERED:
        steeringForce = getCalculateDithered();
        break;

      default:
        steeringForce = Vector2.newInstance();
        break;
    }

    return steeringForce;
  }

  /**
   * Returns the forward component of the steering force.
   */
  public float getForwardComponent() {
    return vehicle.getHeading().getDotProductValue(steeringForce);
  }

  /**
   * Returns the side component of the steering force.
   */
  public float getSideComponent() {
    return vehicle.getSide().getDotProductValue(steeringForce);
  }

  /**
   * This method calls each active steering behavior in order of priority and
   * accumulates their forces until the max steering force magnitude is reached,
   * at which time the function returns the steering force accumulated to that
   * point.
   */
  private void calculatePrioritized() {

    if (isBehavior(Behavior.WALL_AVOIDANCE)) {
      var force = doWallAvoidance(vehicle.getWorld().getWalls()).mul(weightWallAvoidance);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
      var force =
          doObstacleAvoidance(vehicle.getWorld().getObstacles()).mul(weightObstacleAvoidance);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.EVADE)) {
      var force = doEvade(targetAgent1).mul(weightEvade);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.FLEE)) {
      var force = doFlee(vehicle.getWorld().getCrosshair()).mul(weightFlee);
      accumulateForce(force);
    }

    // these next three can be combined for flocking behavior (wander is
    // also a good behavior to add into this mix)
    if (!isSpacePartitioning()) {
      if (isBehavior(Behavior.SEPARATION)) {
        var force = doSeparation(vehicle.getWorld().getAgents()).mul(weightSeparation);
        accumulateForce(force);
      }

      if (isBehavior(Behavior.ALIGNMENT)) {
        var force = doAlignment(vehicle.getWorld().getAgents()).mul(weightAlignment);
        accumulateForce(force);
      }

      if (isBehavior(Behavior.COHESION)) {
        var force = doCohesion(vehicle.getWorld().getAgents()).mul(weightCohesion);
        accumulateForce(force);
      }
    } else {
      if (isBehavior(Behavior.SEPARATION)) {
        var force = doSeparationPlus(vehicle.getWorld().getAgents()).mul(weightSeparation);
        accumulateForce(force);
      }

      if (isBehavior(Behavior.ALIGNMENT)) {
        var force = doAlignmentPlus(vehicle.getWorld().getAgents()).mul(weightAlignment);
        accumulateForce(force);
      }

      if (isBehavior(Behavior.COHESION)) {
        var force = doCohesionPlus(vehicle.getWorld().getAgents()).mul(weightCohesion);
        accumulateForce(force);
      }
    }

    if (isBehavior(Behavior.SEEK)) {
      var force = doSeek(vehicle.getWorld().getCrosshair()).mul(weightSeek);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.ARRIVE)) {
      var force = doArrive(vehicle.getWorld().getCrosshair(), deceleration).mul(weightArrive);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.WANDER)) {
      var force = doWander().mul(weightWander);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.PURSUIT)) {
      var force = doPursuit(targetAgent1).mul(weightPursuit);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.OFFSET_PURSUIT)) {
      var force = doOffsetPursuit(targetAgent1, offset);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.INTERPOSE)) {
      var force = doInterpose(targetAgent1, targetAgent2).mul(weightInterpose);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.HIDE)) {
      var force = doHide(targetAgent1, vehicle.getWorld().getObstacles()).mul(weightHide);
      accumulateForce(force);
    }

    if (isBehavior(Behavior.FOLLOW_PATH)) {
      var force = doFollowPath().mul(weightFollowPath);
      accumulateForce(force);
    }

  }

  /**
   * This simply sums up all the active behaviors X their weights and truncates
   * the result to the max available steering force before returning.
   */
  private Vector2 getCalculateWeightedSum() {
    if (isBehavior(Behavior.WALL_AVOIDANCE)) {
      steeringForce.add(doWallAvoidance(vehicle.getWorld().getWalls()))
          .mul(weightWallAvoidance);
    }

    if (isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
      steeringForce.add(doObstacleAvoidance(vehicle.getWorld().getObstacles()))
          .mul(weightObstacleAvoidance);
    }

    if (isBehavior(Behavior.EVADE)) {
      steeringForce.add(doEvade(targetAgent1)).mul(weightEvade);
    }

    // these next three can be combined for flocking behavior (wander is
    // also a good behavior to add into this mix)
    if (!isSpacePartitioning()) {
      if (isBehavior(Behavior.SEPARATION)) {
        steeringForce.add(doSeparation(vehicle.getWorld().getAgents())).mul(weightSeparation);
      }

      if (isBehavior(Behavior.ALIGNMENT)) {
        steeringForce.add(doAlignment(vehicle.getWorld().getAgents())).mul(weightAlignment);
      }

      if (isBehavior(Behavior.COHESION)) {
        steeringForce.add(doCohesion(vehicle.getWorld().getAgents())).mul(weightCohesion);
      }
    } else {
      if (isBehavior(Behavior.SEPARATION)) {
        steeringForce.add(doSeparationPlus(vehicle.getWorld().getAgents()))
            .mul(weightSeparation);
      }

      if (isBehavior(Behavior.ALIGNMENT)) {
        steeringForce.add(doAlignmentPlus(vehicle.getWorld().getAgents())).mul(weightAlignment);
      }

      if (isBehavior(Behavior.COHESION)) {
        steeringForce.add(doCohesionPlus(vehicle.getWorld().getAgents())).mul(weightCohesion);
      }
    }

    if (isBehavior(Behavior.WANDER)) {
      steeringForce.add(doWander()).mul(weightWander);
    }

    if (isBehavior(Behavior.SEEK)) {
      steeringForce.add(doSeek(vehicle.getWorld().getCrosshair())).mul(weightSeek);
    }

    if (isBehavior(Behavior.FLEE)) {
      steeringForce.add(doFlee(vehicle.getWorld().getCrosshair())).mul(weightFlee);
    }

    if (isBehavior(Behavior.ARRIVE)) {
      steeringForce.add(doArrive(vehicle.getWorld().getCrosshair(), deceleration))
          .mul(weightArrive);
    }

    if (isBehavior(Behavior.PURSUIT)) {
      steeringForce.add(doPursuit(targetAgent1)).mul(weightPursuit);
    }

    if (isBehavior(Behavior.OFFSET_PURSUIT)) {
      steeringForce.add(doOffsetPursuit(targetAgent1, offset)).mul(weightOffsetPursuit);
    }

    if (isBehavior(Behavior.INTERPOSE)) {
      steeringForce.add(doInterpose(targetAgent1, targetAgent2)).mul(weightInterpose);
    }

    if (isBehavior(Behavior.HIDE)) {
      steeringForce.add(doHide(targetAgent1, vehicle.getWorld().getObstacles()))
          .mul(weightHide);
    }

    if (isBehavior(Behavior.FOLLOW_PATH)) {
      steeringForce.add(doFollowPath()).mul(weightFollowPath);
    }

    steeringForce.truncate(vehicle.getMaxForce());

    return steeringForce;
  }

  /**
   * This method sums up the active behaviors by assigning a probability of being
   * calculated to each behavior. It then tests the first priority to see if it
   * should be calculated this simulation-step. If so, it calculates the steering
   * force resulting from this behavior. If it is more than zero it returns the
   * force. If zero, or if the behavior is skipped it continues onto the next
   * priority, and so on.
   * <p>
   * NOTE: Not all the behaviors have been implemented in this method, just a
   * few, so you get the general idea
   */
  private Vector2 getCalculateDithered() {
    // reset the steering force
    steeringForce.zero();

    if (isBehavior(Behavior.WALL_AVOIDANCE) &&
        MathUtility.randFloat() < paramLoader.PR_WALL_AVOIDANCE) {
      steeringForce = doWallAvoidance(vehicle.getWorld().getWalls())
          .mul(weightWallAvoidance / paramLoader.PR_WALL_AVOIDANCE);

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (isBehavior(Behavior.OBSTACLE_AVOIDANCE)
        && MathUtility.randFloat() < paramLoader.PR_OBSTACLE_AVOIDANCE) {
      steeringForce.add(doObstacleAvoidance(vehicle.getWorld().getObstacles())
          .mul(weightObstacleAvoidance / paramLoader.PR_OBSTACLE_AVOIDANCE));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (!isSpacePartitioning()) {
      if (isBehavior(Behavior.SEPARATION) &&
          MathUtility.randFloat() < paramLoader.PR_SEPARATION) {
        steeringForce.add(doSeparation(vehicle.getWorld().getAgents())
            .mul(weightSeparation / paramLoader.PR_SEPARATION));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }
    } else {
      if (isBehavior(Behavior.SEPARATION) &&
          MathUtility.randFloat() < paramLoader.PR_SEPARATION) {
        steeringForce.add(doSeparationPlus(vehicle.getWorld().getAgents())
            .mul(weightSeparation / paramLoader.PR_SEPARATION));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }
    }

    if (isBehavior(Behavior.FLEE) && MathUtility.randFloat() < paramLoader.PR_FLEE) {
      steeringForce
          .add(doFlee(vehicle.getWorld().getCrosshair()).mul(weightFlee / paramLoader.PR_FLEE));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (isBehavior(Behavior.EVADE) && MathUtility.randFloat() < paramLoader.PR_EVADE) {
      steeringForce.add(doEvade(targetAgent1).mul(weightEvade / paramLoader.PR_EVADE));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (!isSpacePartitioning()) {
      if (isBehavior(Behavior.ALIGNMENT) && MathUtility.randFloat() < paramLoader.PR_ALIGNMENT) {
        steeringForce.add(doAlignment(vehicle.getWorld().getAgents())
            .mul(weightAlignment / paramLoader.PR_ALIGNMENT));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }

      if (isBehavior(Behavior.COHESION) && MathUtility.randFloat() < paramLoader.PR_COHESION) {
        steeringForce.add(doCohesion(vehicle.getWorld().getAgents())
            .mul(weightCohesion / paramLoader.PR_COHESION));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }
    } else {
      if (isBehavior(Behavior.ALIGNMENT) && MathUtility.randFloat() < paramLoader.PR_ALIGNMENT) {
        steeringForce.add(doAlignmentPlus(vehicle.getWorld().getAgents())
            .mul(weightAlignment / paramLoader.PR_ALIGNMENT));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }

      if (isBehavior(Behavior.COHESION) && MathUtility.randFloat() < paramLoader.PR_COHESION) {
        steeringForce.add(doCohesionPlus(vehicle.getWorld().getAgents())
            .mul(weightCohesion / paramLoader.PR_COHESION));

        if (!steeringForce.isZero()) {
          steeringForce.truncate(vehicle.getMaxForce());

          return steeringForce;
        }
      }
    }

    if (isBehavior(Behavior.WANDER) && MathUtility.randFloat() < paramLoader.PR_WANDER) {
      steeringForce.add(doWander().mul(weightWander / paramLoader.PR_WANDER));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (isBehavior(Behavior.SEEK) && MathUtility.randFloat() < paramLoader.PR_SEEK) {
      steeringForce
          .add(doSeek(vehicle.getWorld().getCrosshair()).mul(weightSeek / paramLoader.PR_SEEK));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    if (isBehavior(Behavior.ARRIVE) && MathUtility.randFloat() < paramLoader.PR_ARRIVE) {
      steeringForce.add(doArrive(vehicle.getWorld().getCrosshair(), deceleration)
          .mul(weightArrive / paramLoader.PR_ARRIVE));

      if (!steeringForce.isZero()) {
        steeringForce.truncate(vehicle.getMaxForce());

        return steeringForce;
      }
    }

    return steeringForce;
  }

  public void setTargetAgent1(Vehicle Agent) {
    targetAgent1 = Agent;
  }

  public void setTargetAgent2(Vehicle Agent) {
    targetAgent2 = Agent;
  }

  public Vector2 getOffset() {
    return offset;
  }

  public void setOffset(final Vector2 offset) {
    this.offset = offset;
  }

  public void setPath(List<Vector2> path) {
    this.path.setWayPoints(path);
  }

  public void createRandomPath(int numWaypoints, int mx, int my, int cx, int cy) {
    path.createRandomPath(numWaypoints, mx, my, cx, cy);
  }

  public Vector2 getForce() {
    return steeringForce;
  }

  public void toggleSpacePartitioning() {
    enableCellSpace = !enableCellSpace;
  }

  public boolean isSpacePartitioning() {
    return enableCellSpace;
  }

  public void setSummingMethod(SummingMethod sm) {
    summingMethod = sm;
  }

  public void setFleeOn() {
    behaviorFlag |= Behavior.FLEE.get();
  }

  public void setSeekOn() {
    behaviorFlag |= Behavior.SEEK.get();
  }

  public void setArriveOn() {
    behaviorFlag |= Behavior.ARRIVE.get();
  }

  public void setWanderOn() {
    behaviorFlag |= Behavior.WANDER.get();
  }

  public void setCohesionOn() {
    behaviorFlag |= Behavior.COHESION.get();
  }

  public void setSeparationOn() {
    behaviorFlag |= Behavior.SEPARATION.get();
  }

  public void setAlignmentOn() {
    behaviorFlag |= Behavior.ALIGNMENT.get();
  }

  public void setObstacleAvoidanceOn() {
    behaviorFlag |= Behavior.OBSTACLE_AVOIDANCE.get();
  }

  public void setWallAvoidanceOn() {
    behaviorFlag |= Behavior.WALL_AVOIDANCE.get();
  }

  public void setFollowPathOn() {
    behaviorFlag |= Behavior.FOLLOW_PATH.get();
  }

  public void setInterposeOn(Vehicle v1, Vehicle v2) {
    behaviorFlag |= Behavior.INTERPOSE.get();
    targetAgent1 = v1;
    targetAgent2 = v2;
  }

  public void setOffsetPursuitOn(Vehicle vehicle, final Vector2 offset) {
    behaviorFlag |= Behavior.OFFSET_PURSUIT.get();
    this.offset = offset;
    targetAgent1 = vehicle;
  }

  public void setFlockingOn() {
    setCohesionOn();
    setAlignmentOn();
    setSeparationOn();
    setWanderOn();
  }

  public void setFleeOff() {
    if (isBehavior(Behavior.FLEE)) {
      behaviorFlag ^= Behavior.FLEE.get();
    }
  }

  public void setSeekOff() {
    if (isBehavior(Behavior.SEEK)) {
      behaviorFlag ^= Behavior.SEEK.get();
    }
  }

  public void setArriveOff() {
    if (isBehavior(Behavior.ARRIVE)) {
      behaviorFlag ^= Behavior.ARRIVE.get();
    }
  }

  public void setWanderOff() {
    if (isBehavior(Behavior.WANDER)) {
      behaviorFlag ^= Behavior.WANDER.get();
    }
  }

  public void setPursuitOff() {
    if (isBehavior(Behavior.PURSUIT)) {
      behaviorFlag ^= Behavior.PURSUIT.get();
    }
  }

  public void setEvadeOff() {
    if (isBehavior(Behavior.EVADE)) {
      behaviorFlag ^= Behavior.EVADE.get();
    }
  }

  public void setCohesionOff() {
    if (isBehavior(Behavior.COHESION)) {
      behaviorFlag ^= Behavior.COHESION.get();
    }
  }

  public void setSeparationOff() {
    if (isBehavior(Behavior.SEPARATION)) {
      behaviorFlag ^= Behavior.SEPARATION.get();
    }
  }

  public void setAlignmentOff() {
    if (isBehavior(Behavior.ALIGNMENT)) {
      behaviorFlag ^= Behavior.ALIGNMENT.get();
    }
  }

  public void setObstacleAvoidanceOff() {
    if (isBehavior(Behavior.OBSTACLE_AVOIDANCE)) {
      behaviorFlag ^= Behavior.OBSTACLE_AVOIDANCE.get();
    }
  }

  public void setWallAvoidanceOff() {
    if (isBehavior(Behavior.WALL_AVOIDANCE)) {
      behaviorFlag ^= Behavior.WALL_AVOIDANCE.get();
    }
  }

  public void setFollowPathOff() {
    if (isBehavior(Behavior.FOLLOW_PATH)) {
      behaviorFlag ^= Behavior.FOLLOW_PATH.get();
    }
  }

  public void setInterposeOff() {
    if (isBehavior(Behavior.INTERPOSE)) {
      behaviorFlag ^= Behavior.INTERPOSE.get();
    }
  }

  public void setHideOff() {
    if (isBehavior(Behavior.HIDE)) {
      behaviorFlag ^= Behavior.HIDE.get();
    }
  }

  public void setOffsetPursuitOff() {
    if (isBehavior(Behavior.OFFSET_PURSUIT)) {
      behaviorFlag ^= Behavior.OFFSET_PURSUIT.get();
    }
  }

  public void setFlockingOff() {
    setCohesionOff();
    setAlignmentOff();
    setSeparationOff();
    setWanderOff();
  }

  public boolean isFleeOn() {
    return isBehavior(Behavior.FLEE);
  }

  public boolean isSeekOn() {
    return isBehavior(Behavior.SEEK);
  }

  public boolean isArriveOn() {
    return isBehavior(Behavior.ARRIVE);
  }

  public boolean isWanderOn() {
    return isBehavior(Behavior.WANDER);
  }

  public boolean isPursuitOn() {
    return isBehavior(Behavior.PURSUIT);
  }

  public void setPursuitOn(Vehicle vehicle) {
    behaviorFlag |= Behavior.PURSUIT.get();
    targetAgent1 = vehicle;
  }

  public boolean isEvadeOn() {
    return isBehavior(Behavior.EVADE);
  }

  public void setEvadeOn(Vehicle vehicle) {
    behaviorFlag |= Behavior.EVADE.get();
    targetAgent1 = vehicle;
  }

  public boolean isCohesionOn() {
    return isBehavior(Behavior.COHESION);
  }

  public boolean isSeparationOn() {
    return isBehavior(Behavior.SEPARATION);
  }

  public boolean isAlignmentOn() {
    return isBehavior(Behavior.ALIGNMENT);
  }

  public boolean isObstacleAvoidanceOn() {
    return isBehavior(Behavior.OBSTACLE_AVOIDANCE);
  }

  public boolean isWallAvoidanceOn() {
    return isBehavior(Behavior.WALL_AVOIDANCE);
  }

  public boolean isFollowPathOn() {
    return isBehavior(Behavior.FOLLOW_PATH);
  }

  public boolean isInterposeOn() {
    return isBehavior(Behavior.INTERPOSE);
  }

  public boolean isHideOn() {
    return isBehavior(Behavior.HIDE);
  }

  public void setHideOn(Vehicle vehicle) {
    behaviorFlag |= Behavior.HIDE.get();
    targetAgent1 = vehicle;
  }

  public boolean isOffsetPursuitOn() {
    return isBehavior(Behavior.OFFSET_PURSUIT);
  }

  public float getDetectBoxLength() {
    return detectBoxLength;
  }

  public List<Vector2> getSensors() {
    return sensors;
  }

  public float getWanderJitter() {
    return wanderJitter;
  }

  public float getWanderDistance() {
    return wanderDistance;
  }

  public float getWanderRadius() {
    return wanderRadius;
  }

  public float getSeparationWeight() {
    return weightSeparation;
  }

  public float getAlignmentWeight() {
    return weightAlignment;
  }

  public float getCohesionWeight() {
    return weightCohesion;
  }
}
