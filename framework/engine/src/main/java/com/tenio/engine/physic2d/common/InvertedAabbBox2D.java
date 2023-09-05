/*
The MIT License

Copyright (c) 2016-2023 kong <congcoi123@gmail.com>

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

import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;

/**
 * AABB stands for <a href=
 * "https://en.wikipedia.org/wiki/Minimum_bounding_box#Axis-aligned_minimum_bounding_box">Axis-aligned
 * Bounding Box</a>. It is a fairly computationally- and memory-efficient way of
 * representing a volume, typically used to see if two objects might be
 * touching.
 */
public class InvertedAabbBox2D implements Renderable {

  private float left;
  private float top;
  private float right;
  private float bottom;

  private InvertedAabbBox2D(float left, float top, float right, float bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  public static InvertedAabbBox2D newInstance() {
    return valueOf(0, 0, 0, 0);
  }

  public static InvertedAabbBox2D valueOf(float left, float top, float right, float bottom) {
    var invert = new InvertedAabbBox2D(left, top, right, bottom);
    return invert;
  }

  /**
   * Check if two Box is overlapped.
   *
   * @param other the other BBox, see {@link InvertedAabbBox2D}
   * @return <b>true</b> if the <b>BBox</b> described by other intersects with this one
   */
  public boolean isOverlappedWith(InvertedAabbBox2D other) {
    return !((other.getTop() > getBottom())
        || (other.getBottom() < getTop())
        || (other.getLeft() > getRight())
        || (other.getRight() < getLeft()));
  }

  public float getTop() {
    return top;
  }

  public void setTop(float top) {
    this.top = top;
  }

  public float getLeft() {
    return left;
  }

  public void setLeft(float left) {
    this.left = left;
  }

  public float getBottom() {
    return bottom;
  }

  public void setBottom(float bottom) {
    this.bottom = bottom;
  }

  public float getRight() {
    return right;
  }

  public void setRight(float right) {
    this.right = right;
  }

  @Override
  public void render(Paint paint) {
    paint.drawLine(getLeft(), getTop(), getRight(), getTop());
    paint.drawLine(getLeft(), getBottom(), getRight(), getBottom());
    paint.drawLine(getLeft(), getTop(), getLeft(), getBottom());
    paint.drawLine(getRight(), getTop(), getRight(), getBottom());
  }
}
