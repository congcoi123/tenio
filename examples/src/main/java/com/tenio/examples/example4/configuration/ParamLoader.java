package com.tenio.examples.example4.configuration;

import com.tenio.common.utility.MathUtility;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Class to configuration.
 */
public final class ParamLoader extends FileLoaderBase {

  private static ParamLoader instance = null;

  static {
    try {
      instance = new ParamLoader();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int NUM_AGENTS;
  public int NUM_OBSTACLES;
  public float MIN_OBSTACLE_RADIUS;
  public float MAX_OBSTACLE_RADIUS;
  // number of horizontal cells used for spatial partitioning
  public int NUM_CELLS_X;
  // number of vertical cells used for spatial partitioning
  public int NUM_CELLS_Y;
  // how many samples the smoother will use to average a value
  public int NUM_SAMPLES_FOR_SMOOTHING;
  // used to tweak the combined steering force (simply altering the
  // MaxSteeringForce
  // will NOT work! This tweaker affects all the steering force multipliers too)
  public float STEERING_FORCE_TWEAKER;
  public float MAX_STEERING_FORCE;
  public float MAX_SPEED;
  public float VEHICLE_MASS;
  public float VEHICLE_SCALE;
  public float MAX_TURN_RATE_PER_SECOND;
  public float SEPARATION_WEIGHT;
  public float ALIGNMENT_WEIGHT;
  public float COHESION_WEIGHT;
  public float OBSTACLE_AVOIDANCE_WEIGHT;
  public float WALL_AVOIDANCE_WEIGHT;
  public float WANDER_WEIGHT;
  public float SEEK_WEIGHT;
  public float FLEE_WEIGHT;
  public float ARRIVE_WEIGHT;
  public float PURSUIT_WEIGHT;
  public float OFFSET_PURSUIT_WEIGHT;
  public float INTERPOSE_WEIGHT;
  public float HIDE_WEIGHT;
  public float EVADE_WEIGHT;
  public float FOLLOW_PATH_WEIGHT;
  // how close a neighbor must be before an agent perceives it (considers it
  // to be within its neighborhood)
  public float VIEW_DISTANCE;
  // used in obstacle avoidance
  public float MIN_DETECTION_BOX_LENGTH;
  // used in wall avoidance
  public float WALL_DETECTION_FEELER_LENGTH;
  // these are the probabilities that a steering behavior will be used
  // when the prioritized dither calculate method is used
  public float PR_WALL_AVOIDANCE;
  public float PR_OBSTACLE_AVOIDANCE;
  public float PR_SEPARATION;
  public float PR_ALIGNMENT;
  public float PR_COHESION;
  public float PR_WANDER;
  public float PR_SEEK;
  public float PR_FLEE;
  public float PR_EVADE;
  public float PR_HIDE;
  public float PR_ARRIVE;

  private ParamLoader() throws IOException {
    super("src/main/resources/params.ini");

    NUM_AGENTS = getNextParameterInt();
    NUM_OBSTACLES = getNextParameterInt();
    MIN_OBSTACLE_RADIUS = getNextParameterFloat();
    MAX_OBSTACLE_RADIUS = getNextParameterFloat();

    NUM_CELLS_X = getNextParameterInt();
    NUM_CELLS_Y = getNextParameterInt();

    NUM_SAMPLES_FOR_SMOOTHING = getNextParameterInt();

    STEERING_FORCE_TWEAKER = getNextParameterFloat();
    MAX_STEERING_FORCE = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    MAX_SPEED = getNextParameterFloat();
    VEHICLE_MASS = getNextParameterFloat();
    VEHICLE_SCALE = getNextParameterFloat();

    SEPARATION_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    ALIGNMENT_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    COHESION_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    OBSTACLE_AVOIDANCE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    WALL_AVOIDANCE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    WANDER_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    SEEK_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    FLEE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    ARRIVE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    PURSUIT_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    OFFSET_PURSUIT_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    INTERPOSE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    HIDE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    EVADE_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;
    FOLLOW_PATH_WEIGHT = getNextParameterFloat() * STEERING_FORCE_TWEAKER;

    VIEW_DISTANCE = getNextParameterFloat();
    MIN_DETECTION_BOX_LENGTH = getNextParameterFloat();
    WALL_DETECTION_FEELER_LENGTH = getNextParameterFloat();

    PR_WALL_AVOIDANCE = getNextParameterFloat();
    PR_OBSTACLE_AVOIDANCE = getNextParameterFloat();
    PR_SEPARATION = getNextParameterFloat();
    PR_ALIGNMENT = getNextParameterFloat();
    PR_COHESION = getNextParameterFloat();
    PR_WANDER = getNextParameterFloat();
    PR_SEEK = getNextParameterFloat();
    PR_FLEE = getNextParameterFloat();
    PR_EVADE = getNextParameterFloat();
    PR_HIDE = getNextParameterFloat();
    PR_ARRIVE = getNextParameterFloat();

    MAX_TURN_RATE_PER_SECOND = MathUtility.PI;
  }

  public static ParamLoader getInstance() {
    return instance;
  }
}
