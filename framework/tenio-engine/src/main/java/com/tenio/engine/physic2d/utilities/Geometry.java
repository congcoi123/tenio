package com.tenio.engine.physic2d.utilities;

import java.util.List;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * Some useful 2D geometry functions
 */
public final class Geometry {

	private static final Vector2 __temp1 = Vector2.newInstance();
	private static final Vector2 __temp2 = Vector2.newInstance();

	// ------------------------- WhereIsPoint ------------------------
	// ---------------------------------------------------------------
	public enum SpanType {
		PLANE_BACKSIDE, PLANE_FRONT, ON_PLANE
	}

	// Given a plane and a ray. This function determine how far along the ray an
	// interaction occurs. Returns negative if the ray is parallel
	public static float getDistanceRayPlaneIntersection(Vector2 rayOrigin, Vector2 rayHeading, Vector2 planePoint,
			Vector2 planeNormal) {

		float d = -planeNormal.getDotProductValue(planePoint);
		float numer = planeNormal.getDotProductValue(rayOrigin) + d;
		float denom = planeNormal.getDotProductValue(rayHeading);

		// normal is parallel to vector
		if ((denom < 0.000001) && (denom > -0.000001)) {
			return (-1);
		}

		return -(numer / denom);
	}

	public static SpanType getWhereIsPoint(Vector2 point, Vector2 pointOnPlane, Vector2 planeNormal) {
		__temp1.set(pointOnPlane).sub(point);

		float d = __temp1.getDotProductValue(planeNormal);

		if (d < -0.000001) {
			return SpanType.PLANE_FRONT;
		} else if (d > 0.000001) {
			return SpanType.PLANE_BACKSIDE;
		}

		return SpanType.ON_PLANE;
	}

	public static float getDistanceRayCircleIntersect(Vector2 rayOrigin, Vector2 rayHeading, Vector2 circleOrigin,
			float radius) {
		__temp1.set(circleOrigin).sub(rayOrigin);

		float length = __temp1.getLength();
		float v = __temp1.getDotProductValue(rayHeading);
		float d = radius * radius - (length * length - v * v);

		// If there was no intersection, return -1
		if (d < 0) {
			return -1;
		}

		// Return the distance to the [first] intersecting point
		return (float) (v - Math.sqrt(d));
	}

	public static boolean isRayCircleIntersect(Vector2 rayOrigin, Vector2 rayHeading, Vector2 circleOrigin,
			float radius) {
		__temp1.set(circleOrigin).sub(rayOrigin);

		float length = __temp1.getLength();
		float v = __temp1.getDotProductValue(rayHeading);
		float d = radius * radius - (length * length - v * v);

		// If there was no intersection: d < 0
		return !(d < 0);
	}

	// Given a point P and a circle of radius R centered at C This function
	// determines the two points on the circle that intersect with the tangents from
	// P to the circle.

	// Returns false if P is within the circle.
	//
	// Thanks to Dave Eberly for this one.
	public static float[] getTangentPoints(Vector2 C, float r, Vector2 P) {
		__temp1.set(P).sub(C);

		float sqrLen = __temp1.getLengthSqr();
		float rSqr = r * r;
		if (sqrLen <= rSqr) {
			// P is inside or on the circle
			return null;
		}

		float invSqrLen = 1 / sqrLen;
		float root = (float) Math.sqrt(Math.abs(sqrLen - rSqr));
		float[] points = new float[4];

		points[0] = C.x + r * (r * __temp1.x - __temp1.y * root) * invSqrLen;
		points[1] = C.y + r * (r * __temp1.y + __temp1.x * root) * invSqrLen;
		points[2] = C.x + r * (r * __temp1.x + __temp1.y * root) * invSqrLen;
		points[3] = C.y + r * (r * __temp1.y - __temp1.x * root) * invSqrLen;

		return points;
	}

	// Given a line segment AB and a point P This function calculates the
	// perpendicular distance between them

	public static float getDistancePointSegment(Vector2 A, Vector2 B, Vector2 P) {
		// if the angle is obtuse between PA and AB is obtuse then the closest
		// vertex must be A
		float dotA = (P.x - A.x) * (B.x - A.x) + (P.y - A.y) * (B.y - A.y);

		if (dotA <= 0) {
			return __temp1.set(A).getDistanceValue(P);
		}

		// if the angle is obtuse between PB and AB is obtuse then the closest
		// vertex must be B
		float dotB = (P.x - B.x) * (A.x - B.x) + (P.y - B.y) * (A.y - B.y);

		if (dotB <= 0) {
			return __temp1.set(B).getDistanceValue(P);
		}

		// calculate the point along AB that is the closest to P
		// Vector2D Point = A + ((B - A) * dotA)/(dotA + dotB);
		__temp1.set(B).sub(A).mul(dotA).div(dotA + dotB).add(A);

		// calculate the distance P-Point
		return __temp1.getDistanceValue(P);
	}

	public static float getDistancePointSegmentSqr(Vector2 A, Vector2 B, Vector2 P) {
		// if the angle is obtuse between PA and AB is obtuse then the closest
		// vertex must be A
		float dotA = (P.x - A.x) * (B.x - A.x) + (P.y - A.y) * (B.y - A.y);

		if (dotA <= 0) {
			return __temp1.set(A).getDistanceSqrValue(P);
		}

		// if the angle is obtuse between PB and AB is obtuse then the closest
		// vertex must be B
		float dotB = (P.x - B.x) * (A.x - B.x) + (P.y - B.y) * (A.y - B.y);

		if (dotB <= 0) {
			return __temp1.set(B).getDistanceSqrValue(P);
		}

		// calculate the point along AB that is the closest to P
		// Vector2D Point = A + ((B - A) * dotA)/(dotA + dotB);
		__temp1.set(B).sub(A).mul(dotA).div(dotA + dotB).add(A);

		// calculate the distance P-Point
		return __temp1.getDistanceSqrValue(P);
	}

	// Given 2 lines segment in 2D space AB, CD This returns true if an intersection
	// occurs.

	public static boolean isTwoSegmentIntersect(Vector2 A, Vector2 B, Vector2 C, Vector2 D) {
		float rTop = (A.y - C.y) * (D.x - C.x) - (A.x - C.x) * (D.y - C.y);
		float sTop = (A.y - C.y) * (B.x - A.x) - (A.x - C.x) * (B.y - A.y);

		float bot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		// parallel
		if (bot == 0) {
			return false;
		}

		float invBot = 1.0f / bot;
		float r = rTop * invBot;
		float s = sTop * invBot;

		if ((r > 0) && (r < 1) && (s > 0) && (s < 1)) {
			// lines intersect
			return true;
		}

		// lines do not intersect
		return false;
	}

	// Given 2 lines segment in 2D space AB, CD this returns true if an intersection
	// occurs and sets dist as parameter to the distance the intersection occurs
	// along AB. Also sets the 2d vector point to the point of intersection
	public static Vector2 getPointTwoSegmentIntersect(Vector2 A, Vector2 B, Vector2 C, Vector2 D) {

		float rTop = (A.y - C.y) * (D.x - C.x) - (A.x - C.x) * (D.y - C.y);
		float rBot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		float sTop = (A.y - C.y) * (B.x - A.x) - (A.x - C.x) * (B.y - A.y);
		float sBot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		if ((rBot == 0) || (sBot == 0)) {
			// lines are parallel
			return null;
		}

		float r = rTop / rBot;
		float s = sTop / sBot;

		if ((r > 0) && (r < 1) && (s > 0) && (s < 1)) {
			// Point = A + (r * (B - A))
			var point = Vector2.valueOf(B).sub(A).mul(r).add(A);
			return point;
		}

		return null;
	}

	// Given 2 lines segment in 2D space AB, CD this returns true if an intersection
	// occurs and sets dist as parameter to the distance the intersection occurs
	// along AB
	public static float getDistanceTwoSegmentIntersect(Vector2 A, Vector2 B, Vector2 C, Vector2 D) {

		float rTop = (A.y - C.y) * (D.x - C.x) - (A.x - C.x) * (D.y - C.y);
		float rBot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		float sTop = (A.y - C.y) * (B.x - A.x) - (A.x - C.x) * (B.y - A.y);
		float sBot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		if ((rBot == 0) || (sBot == 0)) {
			// lines are parallel
			return -1;
		}

		float r = rTop / rBot;
		float s = sTop / sBot;

		if ((r > 0) && (r < 1) && (s > 0) && (s < 1)) {
			float distance = __temp1.set(A).getDistanceValue(B) * r;
			return distance;
		}

		return -1;
	}

	// Tests two polygons for intersection. Does not check for enclosure!
	public static boolean isTwoObjectsIntersect(List<Vector2> object1, List<Vector2> object2) {
		// test each line segment of object1 against each segment of object2
		for (int r = 0; r < object1.size() - 1; ++r) {
			for (int t = 0; t < object2.size() - 1; ++t) {
				if (isTwoSegmentIntersect(object2.get(t), object2.get(t + 1), object1.get(r), object1.get(r + 1))) {
					return true;
				}
			}
		}

		return false;
	}

	// Tests a line segment against a polygon for intersection Does not check for
	// enclosure!
	public static boolean isSegmentObjectIntersect(final Vector2 A, final Vector2 B, final List<Vector2> object) {
		// test AB against each segment of object
		for (int r = 0; r < object.size() - 1; ++r) {
			if (isTwoSegmentIntersect(A, B, object.get(r), object.get(r + 1))) {
				return true;
			}
		}

		return false;
	}

	// Returns true if the two circles overlap {center (x,y) round r} Include
	// enclosed case
	public static boolean isTwoCirclesOverlapped(float x1, float y1, float r1, float x2, float y2, float r2) {
		float distBetweenCenters = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		if ((distBetweenCenters < (r1 + r2)) || (distBetweenCenters < Math.abs(r1 - r2))) {
			return true;
		}

		return false;
	}

	// Returns true if the two circles overlap {center (x,y) round r} Include
	// enclosed case
	public static boolean isTwoCirclesOverlapped(Vector2 C1, float r1, Vector2 C2, float r2) {
		float distBetweenCenters = (float) Math.sqrt((C1.x - C2.x) * (C1.x - C2.x) + (C1.y - C2.y) * (C1.y - C2.y));

		if ((distBetweenCenters < (r1 + r2)) || (distBetweenCenters < Math.abs(r1 - r2))) {
			return true;
		}

		return false;
	}

	// Returns true if one circle encloses the other {center (x,y) round r}
	public static boolean isTwoCirclesEnclosed(float x1, float y1, float r1, float x2, float y2, float r2) {
		float distBetweenCenters = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		if (distBetweenCenters < Math.abs(r1 - r2)) {
			return true;
		}

		return false;
	}

	// Given two circles This function calculates the intersection points of any
	// overlap.
	// returns false if no overlap found
	// see http://astronomy.swin.edu.au/~pbourke/geometry/2circle/
	public static float[] getTwoCirclesIntersectionPoints(float x1, float y1, float r1, float x2, float y2, float r2) {
		// first check to see if they overlap
		if (!isTwoCirclesOverlapped(x1, y1, r1, x2, y2, r2)) {
			return null;
		}

		// calculate the distance between the circle centers
		float d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		// Now calculate the distance from the center of each circle to the center
		// of the line which connects the intersection points.
		float a = (r1 - r2 + (d * d)) / (2 * d);

		// MAYBE A TEST FOR EXACT OVERLAP?

		// calculate the point P2 which is the center of the line which
		// connects the intersection points
		float p2X, p2Y;

		p2X = x1 + a * (x2 - x1) / d;
		p2Y = y1 + a * (y2 - y1) / d;

		// calculate first point
		float h1 = (float) Math.sqrt((r1 * r1) - (a * a));
		float[] points = new float[4];

		points[0] = p2X - h1 * (y2 - y1) / d;
		points[1] = p2Y + h1 * (x2 - x1) / d;

		// calculate second point
		float h2 = (float) Math.sqrt((r2 * r2) - (a * a));

		points[2] = p2X + h2 * (y2 - y1) / d;
		points[3] = p2Y - h2 * (x2 - x1) / d;

		return points;
	}

	// Tests to see if two circles overlap and if so calculates the area defined by
	// the union
	// see http://mathforum.org/library/drmath/view/54785.html
	public static float getTwoCirclesIntersectionArea(float x1, float y1, float r1, float x2, float y2, float r2) {
		// first calculate the intersection points
		if (getTwoCirclesIntersectionPoints(x1, y1, r1, x2, y2, r2) == null) {
			return 0; // no overlap
		}

		// calculate the distance between the circle centers
		float d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		// find the angles given that A and B are the two circle centers
		// and C and D are the intersection points
		float CBD = (float) (2 * Math.acos((r2 * r2 + d * d - r1 * r1) / (r2 * d * 2)));

		float CAD = (float) (2 * Math.acos((r1 * r1 + d * d - r2 * r2) / (r1 * d * 2)));

		// Then we find the segment of each of the circles cut off by the
		// chord CD, by taking the area of the sector of the circle BCD and
		// subtracting the area of triangle BCD. Similarly we find the area
		// of the sector ACD and subtract the area of triangle ACD.

		float area = (float) (0.5f * CBD * r2 * r2 - 0.5f * r2 * r2 * Math.sin(CBD) + 0.5f * CAD * r1 * r1
				- 0.5f * r1 * r1 * Math.sin(CAD));

		return area;
	}

	// Given the radius, calculates the area of a circle
	public static float getCircleArea(float radius) {
		return (float) (MathUtility.PI * radius * radius);
	}

	// Returns true if the point p is within the radius of the given circle
	public static boolean isPointInCircle(Vector2 C, float radius, Vector2 P) {
		__temp1.set(P).sub(C);
		float distFromCenterSquared = __temp1.getLengthSqr();

		if (distFromCenterSquared < (radius * radius)) {
			return true;
		}

		return false;
	}

	// Returns true if the line segment AB intersects with a circle at position P
	// with radius radius
	public static boolean isSegmentCircleIntersectAtPoint(Vector2 A, Vector2 B, Vector2 P, float radius) {
		// first determine the distance from the center of the circle to
		// the line segment (working in distance squared space)
		float distToLineSqr = getDistancePointSegmentSqr(A, B, P);

		if (distToLineSqr < radius * radius) {
			return true;
		} else {
			return false;
		}
	}

	// Given a line segment AB and a circle position and radius, This function
	// determines if there is an intersection and stores the position of the closest
	// intersection in the reference IntersectionPoint
	// returns false if no intersection point is found
	public static boolean isSegmentCircleClosestIntersectPoint(Vector2 A, Vector2 B, Vector2 C, float radius,
			Vector2 intersectionPoint) {
		__temp1.set(B).sub(A).normalize();
		__temp2.set(__temp1).perpendicular();

		// move the circle into the local space defined by the vector B-A with origin at
		// A
		var localPos = Transformation.pointToLocalSpace(C, __temp1, __temp2, A);

		boolean ipFound = false;

		// if the local position + the radius is negative then the circle lays behind
		// point A so there is no intersection possible. If the local x pos minus the
		// radius is greater than length A-B then the circle cannot intersect the
		// line segment
		if ((localPos.x + radius >= 0)
				&& ((localPos.x - radius) * (localPos.x - radius) <= __temp2.set(B).getDistanceSqrValue(A))) {
			// if the distance from the x axis to the object's position is less
			// than its radius then there is a potential intersection.
			if (Math.abs(localPos.y) < radius) {
				// now to do a line/circle intersection test. The center of the
				// circle is represented by A, B. The intersection points are
				// given by the formulae x = A +/-sqrt(r^2-B^2), y=0. We only
				// need to look at the smallest positive value of x.
				float a = localPos.x;
				float b = localPos.y;

				float ip = (float) (a - Math.sqrt(radius * radius - b * b));

				if (ip <= 0) {
					ip = (float) (a + Math.sqrt(radius * radius - b * b));
				}

				ipFound = true;

				intersectionPoint.set(__temp1.mul(ip).add(A));
			}
		}

		return ipFound;
	}

	// Returns true if the point p is not inside the region defined by top left and
	// bottom right
	public static boolean notInsideRegion(Vector2 point, Vector2 topLeft, Vector2 botRight) {
		return !insideRegion(point, topLeft, botRight);
	}

	public static boolean insideRegion(Vector2 point, Vector2 topLeft, Vector2 botRight) {
		return !((point.x < topLeft.x) || (point.x > botRight.x) || (point.y < topLeft.y) || (point.y > botRight.y));
	}

	public static boolean insideRegion(Vector2 point, int left, int top, int right, int bottom) {
		return !((point.x < left) || (point.x > right) || (point.y < top) || (point.y > bottom));
	}

	// Returns true if the target position is in the field of view of the entity
	// positioned at posFirst facing in facingFirst
	public static boolean isSecondInFOVOfFirst(Vector2 posFirst, Vector2 facingFirst, Vector2 posSecond, float fov) {
		__temp1.set(posSecond).sub(posFirst).normalize();
		return facingFirst.getDotProductValue(__temp1) >= Math.cos(fov / 2);
	}

}
