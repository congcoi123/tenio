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

package com.tenio.examples.example4.entity;

import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * This class is used to create and render 2D walls. Defined as the two vectors
 * A - B with a perpendicular normal.
 */
public final class Wall implements Renderable {

  private float vax;
  private float vay;
  private Vector2 vectorA;
  private float vbx;
  private float vby;
  private Vector2 vectorB;
  private float vnx;
  private float vny;
  private Vector2 vectorN;
  private float vcx;
  private float vcy;
  private Vector2 vectorC;

  private boolean renderNormal = false;

  public Wall() {
  }

  public Wall(float aX, float aY, float bX, float bY) {
    vax = aX;
    vay = aY;
    vbx = bX;
    vby = bY;
    calculateNormal();
  }

  public Wall(float aX, float aY, float bX, float bY, float nX, float nY) {
    vax = aX;
    vay = aY;
    vbx = bX;
    vby = bY;
    vnx = nX;
    vny = nY;
  }

  private void calculateNormal() {
    var temp = Vector2.newInstance().set(vbx, vby).sub(vax, vay).normalize();
    vnx = -temp.y;
    vny = temp.x;

    temp.set(vax, vay).add(vbx, vby).div(2);
    vcx = temp.x;
    vcy = temp.y;
  }

  public float getFromX() {
    return vax;
  }

  public float getFromY() {
    return vay;
  }

  public Vector2 getFrom() {
    return vectorA.set(vax, vay);
  }

  public void setFrom(float x, float y) {
    vax = x;
    vay = y;
    calculateNormal();
  }

  public float getToX() {
    return vbx;
  }

  public float getToY() {
    return vby;
  }

  public Vector2 getTo() {
    return vectorB.set(vbx, vby);
  }

  public void setTo(float x, float y) {
    vbx = x;
    vby = y;
    calculateNormal();
  }

  public float getNormalX() {
    return vnx;
  }

  public float getNormalY() {
    return vny;
  }

  public Vector2 getNormal() {
    return vectorN.set(vnx, vny);
  }

  public void setNormal(float x, float y) {
    vnx = x;
    vny = y;
  }

  public Vector2 getCenter() {
    return vectorC.set(vcx, vcy);
  }

  public void enableRenderNormal(boolean enabled) {
    renderNormal = enabled;
  }

  @Override
  public void render(Paint paint) {
    paint.drawLine(vax, vay, vbx, vby);

    // render the normals
    if (renderNormal) {
      int midX = (int) ((vax + vbx) / 2);
      int midY = (int) ((vay + vby) / 2);

      paint.drawLine(midX, midY, (int) (midX + (vnx * 5)), (int) (midY + (vny * 5)));
    }
  }
}
