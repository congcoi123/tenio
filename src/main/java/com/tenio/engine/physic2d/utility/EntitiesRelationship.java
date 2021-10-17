package com.tenio.engine.physic2d.utility;

import com.tenio.common.utility.MathUtility;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.math.Vector2;
import java.util.LinkedList;
import java.util.List;

/**
 * Some useful entities functions.
 * <br>
 * <a
 * href=https://medium.com/swlh/understanding-3d-matrix-transforms-with-pixijs-c76da3f8bd8>3D
 * Matrix Transforms</a>
 */
public final class EntitiesRelationship {

  // Tests to see if an entity is overlapping any of a number of entities stored
  // in a list container
  public static <T extends BaseGameEntity, CT extends List<? extends T>> boolean isOverlapped(
      T ob, CT conOb) {
    return isOverlapped(ob, conOb, 40);
  }

  /**
   * Check if there is overlap between entities.
   *
   * @param ob                      the ob
   * @param conOb                   the conOb
   * @param minDistBetweenObstacles the min distance between obstacles
   * @param <T>                     the T class
   * @param <CT>                    the CT class
   * @return return <b>true</b> if there is overlap, <b>false</b> otherwise
   */
  public static <T extends BaseGameEntity, CT extends List<? extends T>> boolean isOverlapped(
      T ob, CT conOb,
      float minDistBetweenObstacles) {
    var it = conOb.listIterator();
    while (it.hasNext()) {
      var tmp = it.next();
      if (Geometry.isTwoCirclesOverlapped(ob.getPosition(),
          ob.getBoundingRadius() + minDistBetweenObstacles,
          tmp.getPosition(), tmp.getBoundingRadius())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Tags any entities contained in a list container that are within the radius of
   * the single entity parameter.
   *
   * @param entity              the entity
   * @param containerOfEntities the container of entities
   * @param radius              the radius
   * @param <T>                 the T class
   * @param <CT>                the CT class
   */
  public static <T extends BaseGameEntity, CT extends List<? extends T>> void tagNeighbors(
      final T entity, CT containerOfEntities, float radius) {
    // iterate through all entities checking for range
    containerOfEntities.forEach(neighborEntity -> {
      if (neighborEntity != entity) { // compare pointers
        // first clear any current tag
        neighborEntity.enableTag(false);

        var temp =
            Vector2.newInstance().set(neighborEntity.getPosition()).sub(entity.getPosition());

        // the bounding radius of the other is taken into account by adding it
        // to the range
        float range = radius + neighborEntity.getBoundingRadius();

        // if entity within range, tag for further consideration. (working in
        // distance-squared space to avoid sqrts)
        if ((temp.getLengthSqr() < range * range)) {
          neighborEntity.enableTag(true);
        }
      }
    });
  }

  /**
   * Given a pointer to an entity and a list container of pointers to nearby
   * entities, this function checks to see if there is an overlap between
   * entities. If there is, then the entities are moved away from each other.
   *
   * @param entity              the entity
   * @param containerOfEntities the container of entities
   * @param <T>                 the T class
   * @param <CT>                the CT class
   */
  public static <T extends BaseGameEntity,
      CT extends List<T>> void enforceNonPenetrationConstraint(
      final T entity, final CT containerOfEntities) {
    // iterate through all entities checking for any overlap of bounding radius
    var it = containerOfEntities.listIterator();
    while (it.hasNext()) {
      var curEntity = it.next();
      // make sure we don't check against the individual
      if (curEntity == entity) {
        continue;
      }

      // calculate the distance between the positions of the entities
      var temp = Vector2.newInstance().set(entity.getPosition()).sub(curEntity.getPosition());

      float distFromEachOther = temp.getLength();

      // if this distance is smaller than the sum of their radius then this
      // entity must be moved away in the direction parallel to the
      // ToEntity vector
      float amountOfOverLap =
          curEntity.getBoundingRadius() + entity.getBoundingRadius() - distFromEachOther;

      if (amountOfOverLap >= 0) {
        // move the entity a distance away equivalent to the amount of overlap.
        /*
         * Temp = (EntityPosition - CurrentEntityPosition); Distance = Temp->getLength;
         * NewPosition = EntityPosition + ((Temp / Distance) * Amount)
         */
        temp.div(distFromEachOther).mul(amountOfOverLap).add(entity.getPosition());
        entity.setPosition(temp);
      }
    } // next entity
  }

  // Tests a line segment AB against a container of entities. First, a test
  // is made to confirm that the entity is within a specified range of the
  // one_to_ignore (positioned at vectorA). If within range the intersection test is
  // made.
  //
  // returns a list of all the entities that tested positive for intersection
  public static <T extends BaseGameEntity,
      CT extends List<T>> List<T> getEntityLineSegmentIntersections(
      final CT entities, String theOneToIgnore, Vector2 vectorA, Vector2 vectorB) {
    return getGetEntityLineSegmentIntersections(entities, theOneToIgnore, vectorA, vectorB,
        MathUtility.MAX_FLOAT);
  }

  /**
   * Retrieves the entity line segment intersections.
   *
   * @param entities       the list of entities
   * @param theOneToIgnore the one to ignore
   * @param vectorA        the vector A
   * @param vectorB        the vector B
   * @param range          the range
   * @param <T>            the T class
   * @param <CT>           the CT class
   * @return the list of hit entities
   */
  public static <T extends BaseGameEntity, CT extends
      List<T>> List<T> getGetEntityLineSegmentIntersections(
      final CT entities, String theOneToIgnore, Vector2 vectorA, Vector2 vectorB, float range) {
    var it = entities.listIterator();
    var hits = new LinkedList<T>();

    // iterate through all entities checking against the line segment AB
    while (it.hasNext()) {
      var curEntity = it.next();
      // if not within range or the entity being checked is the_one_to_ignore
      // just continue with the next entity
      float distance = Vector2.newInstance().set(curEntity.getPosition())
          .getDistanceSqrValue(vectorA);
      if ((curEntity.getId().equals(theOneToIgnore)) || distance > range * range) {
        continue;
      }

      // if the distance to AB is less than the entities bounding radius then
      // there is an intersection so add it to hits
      if (Geometry.getDistancePointSegment(vectorA, vectorB, curEntity.getPosition())
          < curEntity.getBoundingRadius()) {
        hits.add(curEntity);
      }
    }

    return hits;
  }

  // Tests a line segment AB against a container of entities. First, a test
  // is made to confirm that the entity is within a specified range of the
  // one_to_ignore (positioned at vectorA). If within range the intersection test is
  // made.
  // returns the closest entity that tested positive for intersection or NULL if
  // none found
  public static <T extends BaseGameEntity, CT extends
      List<T>> T getClosestEntityLineSegmentIntersection(
      final CT entities, String theOneToIgnore, Vector2 vectorA, Vector2 vectorB) {
    return getClosestEntityLineSegmentIntersection(entities, theOneToIgnore, vectorA, vectorB,
        MathUtility.MAX_FLOAT);
  }

  /**
   * Retrieves the closest entity line segment intersections.
   *
   * @param entities       the list of entities
   * @param theOneToIgnore the one to ignore
   * @param vectorA        the vector A
   * @param vectorB        the vector B
   * @param range          the range
   * @param <T>            the T class
   * @param <CT>           the CT class
   * @return the closest entity
   */
  public static <T extends BaseGameEntity, CT extends
      List<T>> T getClosestEntityLineSegmentIntersection(
      final CT entities, String theOneToIgnore, Vector2 vectorA, Vector2 vectorB, float range) {
    var it = entities.listIterator();

    T closestEntity = null;

    float closestDist = MathUtility.MAX_FLOAT;

    // iterate through all entities checking against the line segment AB
    while (it.hasNext()) {
      T curEntity = it.next();

      float distSq = Vector2.newInstance().set(curEntity.getPosition())
          .getDistanceSqrValue(vectorA);

      // if not within range or the entity being checked is the_one_to_ignore
      // just continue with the next entity
      if ((curEntity.getId().equals(theOneToIgnore)) || (distSq > range * range)) {
        continue;
      }

      // if the distance to AB is less than the entities bounding radius then
      // there is an intersection so add it to hits
      if (Geometry.getDistancePointSegment(vectorA, vectorB, curEntity.getPosition())
          < curEntity.getBoundingRadius()) {
        if (distSq < closestDist) {
          closestDist = distSq;

          closestEntity = curEntity;
        }
      }

    }
    return closestEntity;
  }
}
