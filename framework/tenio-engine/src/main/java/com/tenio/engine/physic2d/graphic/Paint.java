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
package com.tenio.engine.physic2d.graphic;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.List;

import com.tenio.engine.physic2d.math.Vector2;

/**
 * This class provides some methods for painting objects to a screen
 * 
 * @author kong
 *
 */
public final class Paint {

	// These objects for temporary calculations
	private final Vector2 __temp1 = Vector2.newInstance();
	private final Vector2 __temp2 = Vector2.newInstance();
	private final Vector2 __temp3 = Vector2.newInstance();
	private final Vector2 __temp4 = Vector2.newInstance();

	/**
	 * It is used for rendering an object in shape
	 */
	private final Polygon __polygon = new Polygon();

	private Graphics __brush;
	private Color __penColor;
	private Color __bgColor;
	/**
	 * The drawing text has a background color or not
	 */
	private boolean __bgTextOpaque;
	private Color __textColor;
	private Color __bgTextColor;

	private static volatile Paint __instance;

	private Paint() {
		__brush = null;
		__penColor = Color.BLACK;
		__bgColor = null;

		__bgTextOpaque = false;
		__textColor = Color.BLACK;
		__bgTextColor = Color.WHITE;

	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Paint getInstance() {
		if (__instance == null) {
			__instance = new Paint();
		}
		return __instance;
	}

	/**
	 * Call this before drawing
	 * 
	 * @param graphic see {@link Graphics}
	 */
	public void startDrawing(Graphics graphic) {
		__brush = graphic;
	}

	// ------------------ Draw Text ------------------
	// -----------------------------------------------
	public void drawTextAtPosition(int x, int y, String s) {
		var back = __brush.getColor();
		y += getFontHeight() - 2;
		if (__bgTextOpaque) {
			FontMetrics fm = __brush.getFontMetrics();
			__brush.setColor(__bgTextColor);
			__brush.fillRect(x, y - fm.getAscent() + fm.getDescent(), fm.stringWidth(s), fm.getAscent());
		}
		__brush.setColor(__textColor);
		__brush.drawString(s, x, y);
		__brush.setColor(back);
	}

	public void drawTextAtPosition(float x, float y, String s) {
		drawTextAtPosition((int) x, (int) y, s);
	}

	public void drawTextAtPosition(Vector2 position, String s) {
		drawTextAtPosition((int) position.x, (int) position.y, s);
	}

	public void enableOpaqueText(boolean enabled) {
		__bgTextOpaque = enabled;
	}

	public void setTextColor(Color color) {
		__textColor = color;
	}

	public void setTextColor(int r, int g, int b) {
		__textColor = new Color(r, g, b);
	}

	public int getFontHeight() {
		if (__brush == null) {
			return 0;
		}
		return __brush.getFontMetrics().getHeight();
	}

	// ------------------ Draw Pixels ----------------
	// -----------------------------------------------
	public void drawDot(Vector2 position, Color color) {
		drawDot((int) position.x, (int) position.y, color);
	}

	public void drawDot(int x, int y, Color color) {
		__brush.setColor(__bgColor);
		__brush.fillRect(x, y, 0, 0);
	}

	// ------------------ Draw Line ------------------
	// -----------------------------------------------
	public void drawLine(Vector2 from, Vector2 to) {
		drawLine(from.x, from.y, to.x, to.y);
	}

	public void drawLine(int a, int b, int x, int y) {
		__brush.setColor(__penColor);
		__brush.drawLine(a, b, x, y);
	}

	public void drawLine(float a, float b, float x, float y) {
		drawLine((int) a, (int) b, (int) x, (int) y);
	}

	public void drawPolyLine(List<Vector2> points) {
		// make sure we have at least 2 points
		if (points.size() < 2) {
			return;
		}

		__polygon.reset();

		for (var v : points) {
			__polygon.addPoint((int) v.x, (int) v.y);
		}
		__brush.setColor(__penColor);
		__brush.drawPolygon(__polygon);
	}

	public void drawLineWithArrow(Vector2 from, Vector2 to, float size) {
		__temp1.set(to).sub(from).normalize();
		var norm = __temp1;

		// calculate where the arrow is attached
		__temp2.set(norm).mul(size);
		__temp3.set(to).sub(__temp2);
		var crossingPoint = __temp3;

		// calculate the two extra points required to make the arrowhead
		__temp4.set(norm.perpendicular()).mul(0.4f * size).add(crossingPoint);
		var arrowPoint1 = __temp4;
		var arrowPoint2 = __temp4;

		// draw the line
		__brush.setColor(__penColor);
		__brush.drawLine((int) from.x, (int) from.y, (int) crossingPoint.x, (int) crossingPoint.y);

		// draw the arrowhead (filled with the currently selected brush)
		__polygon.reset();

		__polygon.addPoint((int) arrowPoint1.x, (int) arrowPoint1.y);
		__polygon.addPoint((int) arrowPoint2.x, (int) arrowPoint2.y);
		__polygon.addPoint((int) to.x, (int) to.y);

		if (__bgColor != null) {
			__brush.setColor(__bgColor);
			__brush.fillPolygon(__polygon);
		}
	}

	public void drawCross(Vector2 position, int diameter) {
		drawLine((int) position.x - diameter, (int) position.y - diameter, (int) position.x + diameter,
				(int) position.y + diameter);
		drawLine((int) position.x - diameter, (int) position.y + diameter, (int) position.x + diameter,
				(int) position.y - diameter);
	}

	// ------------------ Draw Geometry ------------------
	// ---------------------------------------------------
	public void fillRect(Color c, int left, int top, int width, int height) {
		var old = __brush.getColor();
		__brush.setColor(c);
		__brush.fillRect(left, top, width, height);
		__brush.setColor(old);
	}

	public void drawRect(int left, int top, int right, int bot) {
		if (left > right) {
			int tmp = right;
			right = left;
			left = tmp;
		}
		__brush.setColor(__penColor);
		__brush.drawRect(left, top, right - left, bot - top);
		if (__bgColor != null) {
			__brush.setColor(__bgColor);
			__brush.fillRect(left, top, right - left, bot - top);
		}

	}

	public void drawRect(float left, float top, float right, float bot) {
		drawRect((int) left, (int) top, (int) right, (int) bot);
	}

	public void drawClosedShape(List<Vector2> points) {

		__polygon.reset();

		for (Vector2 p : points) {
			__polygon.addPoint((int) p.x, (int) p.y);
		}
		__brush.setColor(__penColor);
		__brush.drawPolygon(__polygon);
		if (__bgColor != null) {
			__brush.fillPolygon(__polygon);
		}
	}

	public void drawCircle(Vector2 position, float radius) {
		drawCircle(position.x, position.y, radius);
	}

	public void drawCircle(float x, float y, float radius) {
		__brush.setColor(__penColor);
		__brush.drawOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
		if (__bgColor != null) {
			__brush.setColor(__bgColor);
			__brush.fillOval((int) (x - radius + 1), (int) (y - radius + 1), (int) (radius * 2 - 1),
					(int) (radius * 2 - 1));
		}
	}

	public void drawCircle(int x, int y, float radius) {
		drawCircle((float) x, (float) y, radius);
	}

	public void setPenColor(Color color) {
		__penColor = color;
	}

	public void setBgColor(Color color) {
		__bgColor = color;
	}

}
