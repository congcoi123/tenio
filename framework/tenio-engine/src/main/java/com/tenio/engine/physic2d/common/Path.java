/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.engine.physic2d.common;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.tenio.common.utilities.MathUtility;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;
import com.tenio.engine.physic2d.utilities.Transformation;

/**
 * This class is used to define, manage, and traverse a path (defined by a
 * series of 2D vectors)
 */
public final class Path implements Renderable {

	/**
	 * The list of way points
	 */
	private List<Vector2> __wayPoints;
	// points to the current way-points
	private ListIterator<Vector2> __currWayPoints;
	private Vector2 __currWayPoint;
	/**
	 * This flag is used to indicate if the path should be looped (the last
	 * way-points connected to the first)
	 */
	private boolean __looped;

	public Path() {
		__wayPoints = new LinkedList<Vector2>();
		__looped = false;
	}

	// This constructor for creating a path with initial random way-points
	public Path(int numWaypoints, float minX, float minY, float maxX, float maxY, boolean looped) {
		this();
		__looped = looped;
		createRandomPath(numWaypoints, minX, minY, maxX, maxY);
	}

	/**
	 * @return the current way-point, see {@link Vector2}
	 */
	public Vector2 getCurrentWayPoint() {
		return __currWayPoint;
	}

	/**
	 * @return <b>true</b> if the end of the list has been reached, <b>false</b>
	 *         otherwise
	 */
	public boolean isEndOfWayPoints() {
		return !(__currWayPoints.hasNext());
	}

	/**
	 * Moves the iterator on to the next way-point in the list
	 */
	public void setToNextWayPoint() {
		if (__wayPoints.isEmpty()) {
			return;
		}

		if (!__currWayPoints.hasNext()) {
			if (__looped) {
				__currWayPoints = __wayPoints.listIterator();
			}
		}
		if (__currWayPoints.hasNext()) {
			__currWayPoint = __currWayPoints.next();
		}
	}

	// Creates a random path which is bound by rectangle described by the <b>min or
	// max</b> values
	public List<Vector2> createRandomPath(int numWaypoints, float minX, float minY, float maxX, float maxY) {
		__wayPoints.clear();

		float midX = (maxX + minX) / 2;
		float midY = (maxY + minY) / 2;

		float smaller = MathUtility.minOf(midX, midY);

		float spacing = MathUtility.TWO_PI / numWaypoints;

		for (int i = 0; i < numWaypoints; ++i) {
			float radialDist = MathUtility.randInRange(smaller * 0.2f, smaller);

			var temp = Transformation.vec2DRotateAroundOrigin(radialDist, 0, i * spacing);

			temp.x += midX;
			temp.y += midY;

			__wayPoints.add(temp);

		}

		__currWayPoints = __wayPoints.listIterator();
		if (__currWayPoints.hasNext()) {
			__currWayPoint = __currWayPoints.next();
		}

		return __wayPoints;
	}

	public void enableLoop(boolean enabled) {
		__looped = enabled;
	}

	/**
	 * Adds a way-point to the end of the path methods for setting the path with
	 * either another path or a list of vectors
	 * 
	 * @param wayPoints list of way-points
	 */
	public void setWayPoints(List<Vector2> wayPoints) {
		__wayPoints = wayPoints;
		__currWayPoints = __wayPoints.listIterator();
		__currWayPoint = __currWayPoints.next();
	}

	public void setPath(Path path) {
		setWayPoints(path.getWayPoints());
	}

	public void clear() {
		__wayPoints.clear();
	}

	public List<Vector2> getWayPoints() {
		return __wayPoints;
	}

	@Override
	public void render(Paint paint) {
		paint.setPenColor(Color.ORANGE);

		var it = __wayPoints.listIterator();

		var wp = it.next();

		while (it.hasNext()) {
			var n = it.next();
			paint.drawLine(wp, n);

			wp = n;
		}

		if (__looped) {
			paint.drawLine(wp, __wayPoints.get(0));
		}
	}

}
