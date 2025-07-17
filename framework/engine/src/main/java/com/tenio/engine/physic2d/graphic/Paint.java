/*
The MIT License

Copyright (c) 2016-2025 kong <congcoi123@gmail.com>

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

import com.tenio.engine.physic2d.math.Vector2;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.List;

/**
 * This class provides some methods for painting objects to a screen.
 */
public final class Paint {

  private static final Paint instance = new Paint();

  // These objects for temporary calculations
  private final Vector2 temp1 = Vector2.newInstance();
  private final Vector2 temp2 = Vector2.newInstance();
  private final Vector2 temp3 = Vector2.newInstance();
  private final Vector2 temp4 = Vector2.newInstance();
  /**
   * It is used for rendering an object in shape.
   */
  private final Polygon polygon = new Polygon();
  private final Color bgTextColor;
  private Graphics brush;
  private Color penColor;
  private Color bgColor;
  /**
   * The drawing text has a background color or not.
   */
  private boolean bgTextOpaque;
  private Color textColor;

  private Paint() {
    if (instance != null) {
      throw new UnsupportedOperationException("Could not recreate this instance");
    }

    brush = null;
    penColor = Color.BLACK;
    bgColor = null;

    bgTextOpaque = false;
    textColor = Color.BLACK;
    bgTextColor = Color.WHITE;
  } // prevent creation manually

  // preventing Singleton object instantiation from outside
  // creates multiple instance if two thread access this method simultaneously
  public static Paint getInstance() {
    return instance;
  }

  /**
   * Call this before drawing.
   *
   * @param graphic see {@link Graphics}
   */
  public void startDrawing(Graphics graphic) {
    brush = graphic;
  }

  // ------------------ Draw Text ------------------
  // -----------------------------------------------

  /**
   * Draw a text at a position.
   *
   * @param x    position in x
   * @param y    position in y
   * @param text the text content
   */
  public void drawTextAtPosition(int x, int y, String text) {
    final var back = brush.getColor();
    y += getFontHeight() - 2;
    if (bgTextOpaque) {
      FontMetrics fm = brush.getFontMetrics();
      brush.setColor(bgTextColor);
      brush.fillRect(x, y - fm.getAscent() + fm.getDescent(), fm.stringWidth(text), fm.getAscent());
    }
    brush.setColor(textColor);
    brush.drawString(text, x, y);
    brush.setColor(back);
  }

  public void drawTextAtPosition(float x, float y, String text) {
    drawTextAtPosition((int) x, (int) y, text);
  }

  public void drawTextAtPosition(Vector2 position, String text) {
    drawTextAtPosition((int) position.x, (int) position.y, text);
  }

  public void enableOpaqueText(boolean enabled) {
    bgTextOpaque = enabled;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setTextColor(int r, int g, int b) {
    textColor = new Color(r, g, b);
  }

  /**
   * Retrieves the height of font.
   *
   * @return the font's height
   */
  public int getFontHeight() {
    if (brush == null) {
      return 0;
    }
    return brush.getFontMetrics().getHeight();
  }

  // ------------------ Draw Pixels ----------------
  // -----------------------------------------------

  public void drawDot(Vector2 position, Color color) {
    drawDot((int) position.x, (int) position.y, color);
  }

  public void drawDot(int x, int y, Color color) {
    brush.setColor(bgColor);
    brush.fillRect(x, y, 0, 0);
  }

  // ------------------ Draw Line ------------------
  // -----------------------------------------------

  public void drawLine(Vector2 from, Vector2 to) {
    drawLine(from.x, from.y, to.x, to.y);
  }

  public void drawLine(int a, int b, int x, int y) {
    brush.setColor(penColor);
    brush.drawLine(a, b, x, y);
  }

  public void drawLine(float a, float b, float x, float y) {
    drawLine((int) a, (int) b, (int) x, (int) y);
  }

  /**
   * Draw a polygon line.
   *
   * @param points the list of points
   */
  public void drawPolyLine(List<Vector2> points) {
    // make sure we have at least 2 points
    if (points.size() < 2) {
      return;
    }

    polygon.reset();

    for (var v : points) {
      polygon.addPoint((int) v.x, (int) v.y);
    }
    brush.setColor(penColor);
    brush.drawPolygon(polygon);
  }

  /**
   * Draw a line with an arrow on its head.
   *
   * @param from the start point
   * @param to   the target point
   * @param size the size
   */
  public void drawLineWithArrow(Vector2 from, Vector2 to, float size) {
    temp1.set(to).sub(from).normalize();
    var norm = temp1;

    // calculate where the arrow is attached
    temp2.set(norm).mul(size);
    temp3.set(to).sub(temp2);
    var crossingPoint = temp3;

    // calculate the two extra points required to make the arrowhead
    temp4.set(norm.perpendicular()).mul(0.4f * size).add(crossingPoint);
    final var arrowPoint1 = temp4;
    final var arrowPoint2 = temp4;

    // draw the line
    brush.setColor(penColor);
    brush.drawLine((int) from.x, (int) from.y, (int) crossingPoint.x, (int) crossingPoint.y);

    // draw the arrowhead (filled with the currently selected brush)
    polygon.reset();

    polygon.addPoint((int) arrowPoint1.x, (int) arrowPoint1.y);
    polygon.addPoint((int) arrowPoint2.x, (int) arrowPoint2.y);
    polygon.addPoint((int) to.x, (int) to.y);

    if (bgColor != null) {
      brush.setColor(bgColor);
      brush.fillPolygon(polygon);
    }
  }

  /**
   * Draw a cross.
   *
   * @param position the position
   * @param diameter the diameter
   */
  public void drawCross(Vector2 position, int diameter) {
    drawLine((int) position.x - diameter, (int) position.y - diameter, (int) position.x + diameter,
        (int) position.y + diameter);
    drawLine((int) position.x - diameter, (int) position.y + diameter, (int) position.x + diameter,
        (int) position.y - diameter);
  }

  // ------------------ Draw Geometry ------------------
  // ---------------------------------------------------

  /**
   * Fill the rectangle with color.
   *
   * @param color  the color
   * @param left   left point
   * @param top    top point
   * @param width  width value
   * @param height height value
   */
  public void fillRect(Color color, int left, int top, int width, int height) {
    var old = brush.getColor();
    brush.setColor(color);
    brush.fillRect(left, top, width, height);
    brush.setColor(old);
  }

  /**
   * Draw a rectangle.
   *
   * @param left   the left point
   * @param top    the top point
   * @param right  the right point
   * @param bottom the bottom point
   */
  public void drawRect(int left, int top, int right, int bottom) {
    if (left > right) {
      int tmp = right;
      right = left;
      left = tmp;
    }
    brush.setColor(penColor);
    brush.drawRect(left, top, right - left, bottom - top);
    if (bgColor != null) {
      brush.setColor(bgColor);
      brush.fillRect(left, top, right - left, bottom - top);
    }
  }

  public void drawRect(float left, float top, float right, float bot) {
    drawRect((int) left, (int) top, (int) right, (int) bot);
  }

  /**
   * Draw a closed shape.
   *
   * @param points the list of points
   */
  public void drawClosedShape(List<Vector2> points) {

    polygon.reset();

    for (Vector2 p : points) {
      polygon.addPoint((int) p.x, (int) p.y);
    }
    brush.setColor(penColor);
    brush.drawPolygon(polygon);
    if (bgColor != null) {
      brush.fillPolygon(polygon);
    }
  }

  public void drawCircle(Vector2 position, float radius) {
    drawCircle(position.x, position.y, radius);
  }

  /**
   * Draw a circle.
   *
   * @param x      the x point
   * @param y      the y point
   * @param radius the radius
   */
  public void drawCircle(float x, float y, float radius) {
    brush.setColor(penColor);
    brush.drawOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
    if (bgColor != null) {
      brush.setColor(bgColor);
      brush.fillOval((int) (x - radius + 1), (int) (y - radius + 1), (int) (radius * 2 - 1), (int) (radius * 2 - 1));
    }
  }

  public void drawCircle(int x, int y, float radius) {
    drawCircle((float) x, (float) y, radius);
  }

  public void setPenColor(Color color) {
    penColor = color;
  }

  public void setBgColor(Color color) {
    bgColor = color;
  }
}
