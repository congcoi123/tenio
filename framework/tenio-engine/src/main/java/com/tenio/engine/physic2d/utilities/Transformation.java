package com.tenio.engine.physic2d.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tenio.engine.physic2d.math.Matrix3;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * Some functions for converting 2D vectors between World and Local space
 */
public final class Transformation {

	private static final Vector2 __temp1 = Vector2.newInstance();
	private static final Vector2 __temp2 = Vector2.newInstance();
	private static final Matrix3 __matrix = Matrix3.newInstance();

	// --------------------------- WorldTransform -------------------------------
	// Given a list of 2D vectors (points), a position, orientation and scale,
	// this function transforms the 2D vectors into the object's world space
	// --------------------------------------------------------------------------
	public static List<Vector2> pointsToWorldSpace(List<Vector2> points, Vector2 position, Vector2 forward,
			Vector2 side, Vector2 scale) {
		// copy the original vertices into the buffer about to be transformed
		var tranVector2Ds = __clone(points);

		// create a transformation matrix
		__matrix.initialize();

		// scale
		if ((scale.x != 1) || (scale.y != 1)) {
			__matrix.scale(scale.x, scale.y);
		}

		// rotate
		__matrix.rotate(forward, side);

		// and translate
		__matrix.translate(position.x, position.y);

		// now transform the object's vertices
		__matrix.transformVector2Ds(tranVector2Ds);

		return tranVector2Ds;
	}

	// --------------------------- WorldTransform -------------------------------
	// Given a list of 2D vectors (points), a position, orientation,
	// this function transforms the 2D vectors into the object's world space
	// --------------------------------------------------------------------------
	public static List<Vector2> pointsToWorldSpace(List<Vector2> points, Vector2 position, Vector2 forward,
			Vector2 side) {
		// copy the original vertices into the buffer about to be transformed
		var tranVector2Ds = __clone(points);

		// create a transformation matrix
		__matrix.initialize();

		// rotate
		__matrix.rotate(forward, side);

		// and translate
		__matrix.translate(position.x, position.y);

		// now transform the object's vertices
		__matrix.transformVector2Ds(tranVector2Ds);

		return tranVector2Ds;
	}

	// --------------------- PointToWorldSpace --------------------------------
	// Transforms a point from the agent's local space into world space
	// ------------------------------------------------------------------------
	public static Vector2 pointToWorldSpace(Vector2 point, Vector2 agentHeading, Vector2 agentSide,
			Vector2 agentPosition) {
		// make a copy of the point
		__temp1.set(point);

		// create a transformation matrix
		__matrix.initialize();

		// rotate
		__matrix.rotate(agentHeading, agentSide);

		// and translate
		__matrix.translate(agentPosition.x, agentPosition.y);

		// now transform the vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	// --------------------- VectorToWorldSpace -------------------------------
	// Transforms a vector from the agent's local space into world space
	// ------------------------------------------------------------------------
	public static Vector2 vectorToWorldSpace(Vector2 vector, Vector2 agentHeading, Vector2 agentSide) {
		// make a copy of the point
		__temp1.set(vector);

		// create a transformation matrix
		__matrix.initialize();

		// rotate
		__matrix.rotate(agentHeading, agentSide);

		// now transform the vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	// --------------------- PointToLocalSpace --------------------------------
	//
	// ------------------------------------------------------------------------
	public static Vector2 pointToLocalSpace(Vector2 point, Vector2 agentHeading, Vector2 agentSide,
			Vector2 agentPosition) {
		// make a copy of the point
		__temp1.set(point);

		// create a transformation matrix
		__matrix.initialize();

		float tX = -agentPosition.getDotProductValue(agentHeading);
		float tY = -agentPosition.getDotProductValue(agentSide);

		// create the transformation matrix
		__matrix._11(agentHeading.x);
		__matrix._12(agentSide.x);
		__matrix._21(agentHeading.y);
		__matrix._22(agentSide.y);
		__matrix._31(tX);
		__matrix._32(tY);

		// now transform the vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	// --------------------- VectorToLocalSpace -------------------------------
	//
	// ------------------------------------------------------------------------
	public static Vector2 vectorToLocalSpace(Vector2 vector, Vector2 agentHeading, Vector2 agentSide) {
		// make a copy of the point
		__temp1.set(vector);

		// create a transformation matrix
		__matrix.initialize();

		// create the transformation matrix
		__matrix._11(agentHeading.x);
		__matrix._12(agentSide.x);
		__matrix._21(agentHeading.y);
		__matrix._22(agentSide.y);

		// now transform the vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	// -------------------------- Vec2DRotateAroundOrigin --------------------------
	// Rotates a vector angle radians around the origin
	// -----------------------------------------------------------------------------
	public static Vector2 vec2DRotateAroundOrigin(Vector2 vector, float angle) {
		// make a copy of the point
		__temp1.set(vector);

		// create a transformation matrix
		__matrix.initialize();

		// rotate
		__matrix.rotate(angle);

		// now transform the object's vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	public static Vector2 vec2DRotateAroundOrigin(float x, float y, float angle) {
		// make a copy of the point
		__temp1.set(x, y);

		// create a transformation matrix
		__matrix.initialize();

		// rotate
		__matrix.rotate(angle);

		// now transform the object's vertices
		__matrix.transformVector2D(__temp1);

		return __temp1.clone();
	}

	// ------------------------ CreateWhiskers ------------------------------------
	// Given an origin, a facing direction, a 'field of view' describing the
	// limit of the outer whiskers, a whisker length and the number of whiskers
	// this method returns a vector containing the end positions of a series
	// of whiskers radiating away from the origin and with equal distance between
	// them. (like the spokes of a wheel clipped to a specific segment size)
	// ----------------------------------------------------------------------------
	public static List<Vector2> createWhiskers(int numWhiskers, float whiskerLength, float fov, Vector2 facing,
			Vector2 origin) {
		// this is the magnitude of the angle separating each whisker
		float sectorSize = fov / (float) (numWhiskers - 1);

		var whiskers = new ArrayList<Vector2>(numWhiskers);
		float angle = -fov * 0.5f;

		for (int w = 0; w < numWhiskers; ++w) {
			// create the whisker extending outwards at this angle
			__temp2.set(facing);
			var temp = vec2DRotateAroundOrigin(__temp2, angle);
			__temp1.set(temp).mul(whiskerLength).add(origin);
			whiskers.add(__temp1.clone());

			angle += sectorSize;
		}

		return whiskers;
	}

	public static Vector2 wrapAround(Vector2 position, int maxX, int maxY) {

		boolean clone = false;

		if (position.x > maxX) {
			position.x = 0;
			clone = true;
		}

		if (position.x < 0) {
			position.x = maxX;
			clone = true;
		}

		if (position.y < 0) {
			position.y = maxY;
			clone = true;
		}

		if (position.y > maxY) {
			position.y = 0;
			clone = true;
		}

		if (clone) {
			return __temp1.set(position).clone();
		}

		return position;
	}

	private static List<Vector2> __clone(List<Vector2> list) {
		return list.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

}
