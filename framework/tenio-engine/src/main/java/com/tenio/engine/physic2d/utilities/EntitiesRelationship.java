package com.tenio.engine.physic2d.utilities;

import java.util.LinkedList;
import java.util.List;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * Some useful entities functions
 * 
 * <a
 * href=https://medium.com/swlh/understanding-3d-matrix-transforms-with-pixijs-c76da3f8bd8>3D
 * Matrix Transforms</a>
 */
public final class EntitiesRelationship {

	private static final Vector2 __temp1 = Vector2.newInstance();

	// Tests to see if an entity is overlapping any of a number of entities stored
	// in a list container
	public static <T extends BaseGameEntity, conT extends List<? extends T>> boolean isOverlapped(T ob, conT conOb) {
		return isOverlapped(ob, conOb, 40);
	}

	public static <T extends BaseGameEntity, conT extends List<? extends T>> boolean isOverlapped(T ob, conT conOb,
			float minDistBetweenObstacles) {
		var it = conOb.listIterator();
		while (it.hasNext()) {
			var tmp = it.next();
			if (Geometry.isTwoCirclesOverlapped(ob.getPosition(), ob.getBoundingRadius() + minDistBetweenObstacles,
					tmp.getPosition(), tmp.getBoundingRadius())) {
				return true;
			}
		}

		return false;
	}

	// Tags any entities contained in a list container that are within the radius of
	// the single entity parameter
	public static <T extends BaseGameEntity, conT extends List<? extends T>> void tagNeighbors(final T entity,
			conT containerOfEntities, float radius) {
		// iterate through all entities checking for range
		containerOfEntities.forEach(neighborEntity -> {
			if (neighborEntity != entity) { // compare pointers
				// first clear any current tag
				neighborEntity.enableTag(false);

				__temp1.set(neighborEntity.getPosition()).sub(entity.getPosition());

				// the bounding radius of the other is taken into account by adding it
				// to the range
				float range = radius + neighborEntity.getBoundingRadius();

				// if entity within range, tag for further consideration. (working in
				// distance-squared space to avoid sqrts)
				if ((__temp1.getLengthSqr() < range * range)) {
					neighborEntity.enableTag(true);
				}
			}
		});
	}

	// Given a pointer to an entity and a list container of pointers to nearby
	// entities, this function checks to see if there is an overlap between
	// entities. If there is, then the entities are moved away from each other
	public static <T extends BaseGameEntity, conT extends List<T>> void enforceNonPenetrationConstraint(final T entity,
			final conT containerOfEntities) {
		// iterate through all entities checking for any overlap of bounding radius
		var it = containerOfEntities.listIterator();
		while (it.hasNext()) {
			var curEntity = it.next();
			// make sure we don't check against the individual
			if (curEntity == entity) {
				continue;
			}

			// calculate the distance between the positions of the entities
			__temp1.set(entity.getPosition()).sub(curEntity.getPosition());

			float distFromEachOther = __temp1.getLength();

			// if this distance is smaller than the sum of their radius then this
			// entity must be moved away in the direction parallel to the
			// ToEntity vector
			float amountOfOverLap = curEntity.getBoundingRadius() + entity.getBoundingRadius() - distFromEachOther;

			if (amountOfOverLap >= 0) {
				// move the entity a distance away equivalent to the amount of overlap.
				/*
				 * Temp = (EntityPosition - CurrentEntityPosition); Distance = Temp->getLength;
				 * NewPosition = EntityPosition + ((Temp / Distance) * Amount)
				 */
				__temp1.div(distFromEachOther).mul(amountOfOverLap).add(entity.getPosition());
				entity.setPosition(__temp1);
			}
		} // next entity
	}

	// Tests a line segment AB against a container of entities. First of all a test
	// is made to confirm that the entity is within a specified range of the
	// one_to_ignore (positioned at A). If within range the intersection test is
	// made.
	//
	// returns a list of all the entities that tested positive for intersection
	public static <T extends BaseGameEntity, conT extends List<T>> List<T> getEntityLineSegmentIntersections(
			final conT entities, String theOneToIgnore, Vector2 A, Vector2 B) {
		return getGetEntityLineSegmentIntersections(entities, theOneToIgnore, A, B, MathUtility.MAX_FLOAT);
	}

	public static <T extends BaseGameEntity, conT extends List<T>> List<T> getGetEntityLineSegmentIntersections(
			final conT entities, String theOneToIgnore, Vector2 A, Vector2 B, float range) {
		var it = entities.listIterator();
		var hits = new LinkedList<T>();

		// iterate through all entities checking against the line segment AB
		while (it.hasNext()) {
			var curEntity = it.next();
			// if not within range or the entity being checked is the_one_to_ignore
			// just continue with the next entity
			float distance = __temp1.set(curEntity.getPosition()).getDistanceSqrValue(A);
			if ((curEntity.getId().equals(theOneToIgnore)) || distance > range * range) {
				continue;
			}

			// if the distance to AB is less than the entities bounding radius then
			// there is an intersection so add it to hits
			if (Geometry.getDistancePointSegment(A, B, curEntity.getPosition()) < curEntity.getBoundingRadius()) {
				hits.add(curEntity);
			}
		}

		return hits;
	}

	// Tests a line segment AB against a container of entities. First of all a test
	// is made to confirm that the entity is within a specified range of the
	// one_to_ignore (positioned at A). If within range the intersection test is
	// made.
	// returns the closest entity that tested positive for intersection or NULL if
	// none found
	public static <T extends BaseGameEntity, conT extends List<T>> T getClosestEntityLineSegmentIntersection(
			final conT entities, String theOneToIgnore, Vector2 A, Vector2 B) {
		return getClosestEntityLineSegmentIntersection(entities, theOneToIgnore, A, B, MathUtility.MAX_FLOAT);
	}

	public static <T extends BaseGameEntity, conT extends List<T>> T getClosestEntityLineSegmentIntersection(
			final conT entities, String theOneToIgnore, Vector2 A, Vector2 B, float range) {
		var it = entities.listIterator();

		T closestEntity = null;

		float closestDist = MathUtility.MAX_FLOAT;

		// iterate through all entities checking against the line segment AB
		while (it.hasNext()) {
			T curEntity = it.next();

			float distSq = __temp1.set(curEntity.getPosition()).getDistanceSqrValue(A);

			// if not within range or the entity being checked is the_one_to_ignore
			// just continue with the next entity
			if ((curEntity.getId().equals(theOneToIgnore)) || (distSq > range * range)) {
				continue;
			}

			// if the distance to AB is less than the entities bounding radius then
			// there is an intersection so add it to hits
			if (Geometry.getDistancePointSegment(A, B, curEntity.getPosition()) < curEntity.getBoundingRadius()) {
				if (distSq < closestDist) {
					closestDist = distSq;

					closestEntity = curEntity;
				}
			}

		}
		return closestEntity;
	}

}
