/*
The MIT License

Copyright (c) 2016-2022 kong <congcoi123@gmail.com>

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

package com.tenio.engine.physic2d.graphic.window;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;

/**
 * Supported methods for windows.
 */
public final class Windows {

  public static final long MF_CHECKED = 0x00000008L;
  public static final long MF_UNCHECKED = 0x00000000L;
  public static final long MFS_CHECKED = MF_CHECKED;
  public static final long MFS_UNCHECKED = MF_UNCHECKED;
  public static final int FOREGROUND_BLUE = 0x0001;
  public static final int FOREGROUND_GREEN = 0x0002;
  public static final int FOREGROUND_RED = 0x0004;
  public static final int FOREGROUND_INTENSITY = 0x0008;
  public static final int BACKGROUND_RED = 0x0040;

  /**
   * Load the windows icon.
   *
   * @param file the icon file
   * @return the image
   */
  public static Image loadIcon(String file) {
    var iconUrl = Windows.class.getResource(file);
    return Toolkit.getDefaultToolkit().createImage(iconUrl);
  }

  /**
   * Point in 2D.
   */
  public static class D2Point extends Point2D.Float {

    private static final long serialVersionUID = -1196038548500345000L;

    @Override
    public void setLocation(float x, float y) {
      super.setLocation(Math.round(x), Math.round(y));
    }
  }

  /**
   * Point in 2D.
   */
  public static class P2Point extends Point {

    private static final long serialVersionUID = 8698839709008819029L;

    public P2Point() {
      this(0, 0);
    }

    public P2Point(int x, int y) {
      super(x, y);
    }

    public P2Point(Point point) {
      super(point);
    }
  }
}
