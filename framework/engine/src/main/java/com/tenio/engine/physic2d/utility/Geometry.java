package com.tenio.engine.physic2d.utility;

import com.tenio.common.utility.MathUtility;
import com.tenio.engine.physic2d.math.Vector2;
import java.util.List;
import java.util.Objects;

/**
 * Some useful 2D geometry functions.
 */
public final class Geometry {

  /**
   * Given a plane and a ray. This function determine how far along the ray an
   * interaction occurs. Returns negative if the ray is parallel.
   *
   * @param rayOrigin   the ray origin
   * @param rayHeading  the ray heading
   * @param planePoint  the plane point
   * @param planeNormal the plane normal
   * @return the distance ray plane intersection
   */
  public static float getDistanceRayPlaneIntersection(Vector2 rayOrigin, Vector2 rayHeading,
                                                      Vector2 planePoint,
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

  /**
   * Retrieves the span type.
   *
   * @param point        the point
   * @param pointOnPlane the point on plane
   * @param planeNormal  the plan normal
   * @return the span type
   */
  public static SpanType getWhereIsPoint(Vector2 point, Vector2 pointOnPlane, Vector2 planeNormal) {
    var temp = Vector2.newInstance().set(pointOnPlane).sub(point);

    float d = temp.getDotProductValue(planeNormal);

    if (d < -0.000001) {
      return SpanType.PLANE_FRONT;
    } else if (d > 0.000001) {
      return SpanType.PLANE_BACKSIDE;
    }

    return SpanType.ON_PLANE;
  }

  /**
   * Retrieves the distance ray circle intersection.
   *
   * @param rayOrigin    the ray origin
   * @param rayHeading   the ray heading
   * @param circleOrigin the circle origin
   * @param radius       the radius
   * @return the distance ray circle intersection
   */
  public static float getDistanceRayCircleIntersect(Vector2 rayOrigin, Vector2 rayHeading,
                                                    Vector2 circleOrigin,
                                                    float radius) {
    var temp = Vector2.newInstance().set(circleOrigin).sub(rayOrigin);

    float length = temp.getLength();
    float v = temp.getDotProductValue(rayHeading);
    float d = radius * radius - (length * length - v * v);

    // If there was no intersection, return -1
    if (d < 0) {
      return -1;
    }

    // Return the distance to the [first] intersecting point
    return (float) (v - Math.sqrt(d));
  }

  /**
   * Check if the ray intersect with the circle.
   *
   * @param rayOrigin    the ray origin
   * @param rayHeading   the ray heading
   * @param circleOrigin the circle origin
   * @param radius       the circle's radius
   * @return <b>true</b> if intersected, <b>false</b> otherwise
   */
  public static boolean isRayCircleIntersect(Vector2 rayOrigin, Vector2 rayHeading,
                                             Vector2 circleOrigin,
                                             float radius) {
    var temp = Vector2.newInstance().set(circleOrigin).sub(rayOrigin);

    float length = temp.getLength();
    float v = temp.getDotProductValue(rayHeading);
    float d = radius * radius - (length * length - v * v);

    // If there was no intersection: d < 0
    return !(d < 0);
  }

  /**
   * Check if the vector is within the circle.
   *
   * @param center the circle origin
   * @param radius the circle radius
   * @param vector the vector
   * @return <b>false</b> if vector is within the circle, <b>true</b> otherwise
   */
  public static float[] getTangentPoints(Vector2 center, float radius, Vector2 vector) {
    var temp = Vector2.newInstance().set(vector).sub(center);

    float sqrLen = temp.getLengthSqr();
    float rsqr = radius * radius;
    if (sqrLen <= rsqr) {
      // vector is inside or on the circle
      return null;
    }

    float invSqrLen = 1 / sqrLen;
    float root = (float) Math.sqrt(Math.abs(sqrLen - rsqr));
    float[] points = new float[4];

    points[0] = center.x + radius * (radius * temp.x - temp.y * root) * invSqrLen;
    points[1] = center.y + radius * (radius * temp.y + temp.x * root) * invSqrLen;
    points[2] = center.x + radius * (radius * temp.x + temp.y * root) * invSqrLen;
    points[3] = center.y + radius * (radius * temp.y - temp.x * root) * invSqrLen;

    return points;
  }

  /**
   * Given a point vectorP and a circle of radius R centered at C This function
   * determines the two points on the circle that intersect with the tangents from
   * vectorP to the circle.
   *
   * @param vectorA vector A
   * @param vectorB vector B
   * @param vectorP vector P
   * @return the distance point segment
   */
  public static float getDistancePointSegment(Vector2 vectorA, Vector2 vectorB, Vector2 vectorP) {
    // if the angle is obtuse between PA and AB is obtuse then the closest
    // vertex must be vectorA
    float dotA = (vectorP.x - vectorA.x) * (vectorB.x - vectorA.x)
        + (vectorP.y - vectorA.y) * (vectorB.y - vectorA.y);

    if (dotA <= 0) {
      return Vector2.newInstance().set(vectorA).getDistanceValue(vectorP);
    }

    // if the angle is obtuse between PB and AB is obtuse then the closest
    // vertex must be vectorB
    float dotB = (vectorP.x - vectorB.x) * (vectorA.x - vectorB.x)
        + (vectorP.y - vectorB.y) * (vectorA.y - vectorB.y);

    if (dotB <= 0) {
      return Vector2.newInstance().set(vectorB).getDistanceValue(vectorP);
    }

    // calculate the point along AB that is the closest to vectorP
    // Vector2D Point = vectorA + ((vectorB - vectorA) * dotA)/(dotA + dotB);
    var temp =
        Vector2.newInstance().set(vectorB).sub(vectorA).mul(dotA).div(dotA + dotB).add(vectorA);

    // calculate the distance vectorP-Point
    return temp.getDistanceValue(vectorP);
  }

  /**
   * Given a line segment AB and a point P This function calculates the
   * perpendicular distance between them.
   *
   * @param vectorA vector A
   * @param vectorB vector B
   * @param vectorP vector P
   * @return the distance point segment sqr
   */
  public static float getDistancePointSegmentSqr(Vector2 vectorA, Vector2 vectorB,
                                                 Vector2 vectorP) {
    // if the angle is obtuse between PA and AB is obtuse then the closest
    // vertex must be vectorA
    float dotA = (vectorP.x - vectorA.x) * (vectorB.x - vectorA.x)
        + (vectorP.y - vectorA.y) * (vectorB.y - vectorA.y);

    if (dotA <= 0) {
      return Vector2.newInstance().set(vectorA).getDistanceSqrValue(vectorP);
    }

    // if the angle is obtuse between PB and AB is obtuse then the closest
    // vertex must be vectorB
    float dotB = (vectorP.x - vectorB.x) * (vectorA.x - vectorB.x)
        + (vectorP.y - vectorB.y) * (vectorA.y - vectorB.y);

    if (dotB <= 0) {
      return Vector2.newInstance().set(vectorB).getDistanceSqrValue(vectorP);
    }

    // calculate the point along AB that is the closest to vectorP
    // Vector2D Point = vectorA + ((vectorB - vectorA) * dotA)/(dotA + dotB);
    var temp =
        Vector2.newInstance().set(vectorB).sub(vectorA).mul(dotA).div(dotA + dotB).add(vectorA);

    // calculate the distance vectorP-Point
    return temp.getDistanceSqrValue(vectorP);
  }

  /**
   * Determine if two segments are intersected.
   *
   * @param vectorA the vector A
   * @param vectorB the vector B
   * @param vectorC the vector C
   * @param vectorD the vector D
   * @return <b>true</b> if they are intersected, <b>false</b> otherwise
   */
  public static boolean isTwoSegmentIntersect(Vector2 vectorA, Vector2 vectorB, Vector2 vectorC,
                                              Vector2 vectorD) {
    float rtop = (vectorA.y - vectorC.y) * (vectorD.x - vectorC.x)
        - (vectorA.x - vectorC.x) * (vectorD.y - vectorC.y);
    float stop = (vectorA.y - vectorC.y) * (vectorB.x - vectorA.x)
        - (vectorA.x - vectorC.x) * (vectorB.y - vectorA.y);

    float bot = (vectorB.x - vectorA.x) * (vectorD.y - vectorC.y)
        - (vectorB.y - vectorA.y) * (vectorD.x - vectorC.x);

    // parallel
    if (bot == 0) {
      return false;
    }

    float invBot = 1.0f / bot;
    float r = rtop * invBot;
    float s = stop * invBot;

    // lines intersect
    return (r > 0) && (r < 1) && (s > 0) && (s < 1);

    // lines do not intersect
  }

  /**
   * Given 2 lines segment in 2D space AB, CD this returns true if an intersection
   * occurs and sets dist as parameter to the distance the intersection occurs
   * along AB. Also sets the 2d vector point to the point of intersection.
   *
   * @param vectorA the vector A
   * @param vectorB the vector B
   * @param vectorC the vector C
   * @param vectorD the vector D
   * @return the point where two segments are intersected
   */
  public static Vector2 getPointTwoSegmentIntersect(Vector2 vectorA, Vector2 vectorB,
                                                    Vector2 vectorC, Vector2 vectorD) {

    float rtop = (vectorA.y - vectorC.y) * (vectorD.x - vectorC.x)
        - (vectorA.x - vectorC.x) * (vectorD.y - vectorC.y);
    float rbot = (vectorB.x - vectorA.x) * (vectorD.y - vectorC.y)
        - (vectorB.y - vectorA.y) * (vectorD.x - vectorC.x);

    float stop = (vectorA.y - vectorC.y) * (vectorB.x - vectorA.x)
        - (vectorA.x - vectorC.x) * (vectorB.y - vectorA.y);
    float sbot = (vectorB.x - vectorA.x) * (vectorD.y - vectorC.y)
        - (vectorB.y - vectorA.y) * (vectorD.x - vectorC.x);

    if ((rbot == 0) || (sbot == 0)) {
      // lines are parallel
      return null;
    }

    float r = rtop / rbot;
    float s = stop / sbot;

    if ((r > 0) && (r < 1) && (s > 0) && (s < 1)) {
      // Point = vectorA + (r * (vectorB - vectorA))
      return Vector2.valueOf(vectorB).sub(vectorA).mul(r).add(vectorA);
    }

    return null;
  }

  /**
   * Given 2 lines segment in 2D space AB, CD this returns true if an intersection
   * occurs and sets dist as parameter to the distance the intersection occurs
   * along AB.
   *
   * @param vectorA the vector A
   * @param vectorB the vector B
   * @param vectorC the vector C
   * @param vectorD the vector D
   * @return the distance the intersection occurs along AB
   */
  public static float getDistanceTwoSegmentIntersect(Vector2 vectorA, Vector2 vectorB,
                                                     Vector2 vectorC, Vector2 vectorD) {

    float rtop = (vectorA.y - vectorC.y) * (vectorD.x - vectorC.x)
        - (vectorA.x - vectorC.x) * (vectorD.y - vectorC.y);
    float rbot = (vectorB.x - vectorA.x) * (vectorD.y - vectorC.y)
        - (vectorB.y - vectorA.y) * (vectorD.x - vectorC.x);

    float stop = (vectorA.y - vectorC.y) * (vectorB.x - vectorA.x)
        - (vectorA.x - vectorC.x) * (vectorB.y - vectorA.y);
    float sbot = (vectorB.x - vectorA.x) * (vectorD.y - vectorC.y)
        - (vectorB.y - vectorA.y) * (vectorD.x - vectorC.x);

    if ((rbot == 0) || (sbot == 0)) {
      // lines are parallel
      return -1;
    }

    float r = rtop / rbot;
    float s = stop / sbot;

    if ((r > 0) && (r < 1) && (s > 0) && (s < 1)) {
      return Vector2.newInstance().set(vectorA).getDistanceValue(vectorB) * r;
    }

    return -1;
  }

  /**
   * Tests two polygons for intersection. Does not check for enclosure!
   *
   * @param object1 object 1
   * @param object2 object 2
   * @return <b>true</b> if two polygons are intersected, <b>false</b> otherwise
   */
  public static boolean isTwoObjectsIntersect(List<Vector2> object1, List<Vector2> object2) {
    // test each line segment of object1 against each segment of object2
    for (int r = 0; r < object1.size() - 1; ++r) {
      for (int t = 0; t < object2.size() - 1; ++t) {
        if (isTwoSegmentIntersect(object2.get(t), object2.get(t + 1), object1.get(r),
            object1.get(r + 1))) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Tests a line segment against a polygon for intersection Does not check for
   * enclosure.
   *
   * @param vectorA the vector A
   * @param vectorB the vector B
   * @param object  the object
   * @return <b>true</b> if there is intersection between a line and a polygon
   */
  public static boolean isSegmentObjectIntersect(final Vector2 vectorA, final Vector2 vectorB,
                                                 final List<Vector2> object) {
    // test AB against each segment of object
    for (int r = 0; r < object.size() - 1; ++r) {
      if (isTwoSegmentIntersect(vectorA, vectorB, object.get(r), object.get(r + 1))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns true if the two circles overlap {center (x,y) round r} Include
   * enclosed case.
   *
   * @param x1 circle 1 center x
   * @param y1 circle 1 center y
   * @param r1 circle 1 radius
   * @param x2 circle 2 center x
   * @param y2 circle 2 center y
   * @param r2 circle 2 radius
   * @return <b>true</b> if two circles are overlapped, <b>false</b> otherwise
   */
  public static boolean isTwoCirclesOverlapped(float x1, float y1, float r1, float x2, float y2,
                                               float r2) {
    float distBetweenCenters = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

    return (distBetweenCenters < (r1 + r2)) || (distBetweenCenters < Math.abs(r1 - r2));
  }

  /**
   * Returns true if the two circles overlap {center (x,y) round r} Include
   * enclosed case.
   *
   * @param circle1 circle 1 origin
   * @param radius1 circle 1 radius
   * @param circle2 circle 2 origin
   * @param radius2 circle 2 radius
   * @return <b>true</b> if two circles are overlapped, <b>false</b> otherwise
   */
  public static boolean isTwoCirclesOverlapped(Vector2 circle1, float radius1, Vector2 circle2,
                                               float radius2) {
    float distBetweenCenters =
        (float) Math.sqrt((circle1.x - circle2.x) * (circle1.x - circle2.x)
            + (circle1.y - circle2.y) * (circle1.y - circle2.y));

    return (distBetweenCenters < (radius1 + radius2))
        || (distBetweenCenters < Math.abs(radius1 - radius2));
  }

  /**
   * Determines if one circle encloses the other.
   *
   * @param x1 circle 1 center x
   * @param y1 circle 1 center y
   * @param r1 circle 1 radius
   * @param x2 circle 2 center x
   * @param y2 circle 2 center y
   * @param r2 circle 2 radius
   * @return <b>true</b> if one circle encloses the other, <b>false</b> otherwise
   */
  public static boolean isTwoCirclesEnclosed(float x1, float y1, float r1, float x2, float y2,
                                             float r2) {
    float distBetweenCenters = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

    return distBetweenCenters < Math.abs(r1 - r2);
  }

  /**
   * Given two circles, this function calculates the intersection points of any overlap.
   * see http://astronomy.swin.edu.au/~pbourke/geometry/2circle/
   *
   * @param x1 circle 1 center x
   * @param y1 circle 1 center y
   * @param r1 circle 1 radius
   * @param x2 circle 2 center x
   * @param y2 circle 2 center y
   * @param r2 circle 2 radius
   * @return a list of intersection points
   */
  public static float[] getTwoCirclesIntersectionPoints(float x1, float y1, float r1, float x2,
                                                        float y2, float r2) {
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
    float p2X;
    float p2Y;

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

  /**
   * Tests to see if two circles overlap and if so calculates the area defined by the union.
   * see http://mathforum.org/library/drmath/view/54785.html
   *
   * @param x1 circle 1 center x
   * @param y1 circle 1 center y
   * @param r1 circle 1 radius
   * @param x2 circle 2 center x
   * @param y2 circle 2 center y
   * @param r2 circle 2 radius
   * @return the union if two circles are overlap
   */
  public static float getTwoCirclesIntersectionArea(float x1, float y1, float r1, float x2,
                                                    float y2, float r2) {
    // first calculate the intersection points
    if (Objects.isNull(getTwoCirclesIntersectionPoints(x1, y1, r1, x2, y2, r2))) {
      return 0; // no overlap
    }

    // calculate the distance between the circle centers
    float d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

    // find the angles given that A and B are the two circle centers
    // and C and D are the intersection points
    float cbd = (float) (2 * Math.acos((r2 * r2 + d * d - r1 * r1) / (r2 * d * 2)));

    float cad = (float) (2 * Math.acos((r1 * r1 + d * d - r2 * r2) / (r1 * d * 2)));

    // Then we find the segment of each of the circles cut off by the
    // chord CD, by taking the area of the sector of the circle BCD and
    // subtracting the area of triangle BCD. Similarly, we find the area
    // of the sector ACD and subtract the area of triangle ACD.
    return (float) (0.5f * cbd * r2 * r2 - 0.5f * r2 * r2 * Math.sin(cbd) + 0.5f * cad * r1 * r1
        - 0.5f * r1 * r1 * Math.sin(cad));
  }

  // Given the radius, calculates the area of a circle
  public static float getCircleArea(float radius) {
    return MathUtility.PI * radius * radius;
  }

  /**
   * Check if the point p is within the radius of the given circle.
   *
   * @param center the circle origin
   * @param radius the circle radius
   * @param point  the point
   * @return <b>true</b> if the point is within the radius of the circle, <b>false</b> otherwise
   */
  public static boolean isPointInCircle(Vector2 center, float radius, Vector2 point) {
    var temp = Vector2.newInstance().set(point).sub(center);
    float distFromCenterSquared = temp.getLengthSqr();

    return distFromCenterSquared < (radius * radius);
  }

  /**
   * Determines if the line segment AB intersects with a circle at position vectorC with radius.
   *
   * @param vectorA vector A
   * @param vectorB vector B
   * @param vectorC circle origin
   * @param radius  circle radius
   * @return <b>true</b> or <b>false</b>
   */
  public static boolean isSegmentCircleIntersectAtPoint(Vector2 vectorA, Vector2 vectorB,
                                                        Vector2 vectorC,
                                                        float radius) {
    // first determine the distance from the center of the circle to
    // the line segment (working in distance squared space)
    float distToLineSqr = getDistancePointSegmentSqr(vectorA, vectorB, vectorC);

    return distToLineSqr < radius * radius;
  }

  /**
   * Given a line segment AB and a circle position and radius, This function determines if there
   * is an intersection and stores the position of the closest intersection in the reference
   * IntersectionPoint returns false if no intersection point is found.
   *
   * @param vectorA           vector A
   * @param vectorB           vector B
   * @param vectorC           circle origin
   * @param radius            circle radius
   * @param intersectionPoint intersection point
   * @return <b>true</b> or <b>false</b>
   */
  public static boolean isSegmentCircleClosestIntersectPoint(Vector2 vectorA, Vector2 vectorB,
                                                             Vector2 vectorC,
                                                             float radius,
                                                             Vector2 intersectionPoint) {
    var temp1 = Vector2.newInstance().set(vectorB).sub(vectorA).normalize();
    var temp2 = Vector2.newInstance().set(temp1).perpendicular();

    // move the circle into the local space defined by the vector vectorB-vectorA with origin at
    // vectorA
    var localPos = Transformation.pointToLocalSpace(vectorC, temp1, temp2, vectorA);

    boolean ipFound = false;

    // if the local position + the radius is negative then the circle lays behind
    // point vectorA so there is no intersection possible. If the local x pos minus the
    // radius is greater than length vectorA-vectorB then the circle cannot intersect the
    // line segment
    if ((localPos.x + radius >= 0)
        &&
        ((localPos.x - radius) * (localPos.x - radius)
            <= temp2.set(vectorB).getDistanceSqrValue(vectorA))) {
      // if the distance from the x-axis to the object's position is less
      // than its radius then there is a potential intersection.
      if (Math.abs(localPos.y) < radius) {
        // now to do a line/circle intersection test. The center of the
        // circle is represented by vectorA, vectorB. The intersection points are
        // given by the formulae x = vectorA +/-sqrt(r^2-vectorB^2), y=0. We only
        // need to look at the smallest positive value of x.
        float a = localPos.x;
        float b = localPos.y;

        double sqrt = Math.sqrt(radius * radius - b * b);
        float ip = (float) (a - sqrt);

        if (ip <= 0) {
          ip = (float) (a + sqrt);
        }

        ipFound = true;

        intersectionPoint.set(temp1.mul(ip).add(vectorA));
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
    return !((point.x < topLeft.x) || (point.x > botRight.x) || (point.y < topLeft.y)
        || (point.y > botRight.y));
  }

  public static boolean insideRegion(Vector2 point, int left, int top, int right, int bottom) {
    return !((point.x < left) || (point.x > right) || (point.y < top) || (point.y > bottom));
  }

  // Returns true if the target position is in the field of view of the entity
  // positioned at posFirst facing in facingFirst
  public static boolean isSecondInFovoFirst(Vector2 posFirst, Vector2 facingFirst,
                                            Vector2 posSecond, float fov) {
    var temp = Vector2.newInstance().set(posSecond).sub(posFirst).normalize();
    return facingFirst.getDotProductValue(temp) >= Math.cos(fov / 2);
  }

  // ------------------------- WhereIsPoint ------------------------
  // ---------------------------------------------------------------

  /**
   * Span Type.
   */
  public enum SpanType {
    PLANE_BACKSIDE, PLANE_FRONT, ON_PLANE
  }
}
