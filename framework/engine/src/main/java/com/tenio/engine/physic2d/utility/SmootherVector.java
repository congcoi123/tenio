package com.tenio.engine.physic2d.utility;

import com.tenio.engine.physic2d.math.Vector2;
import java.util.Objects;

/**
 * Template class to help calculate the average value of a history of values.
 * This can only be used with types that have a 'zero' value and that have the
 * += and / operators overloaded.
 * <br>
 * Example: Used to smooth frame rate calculations.
 */
public class SmootherVector<T extends Vector2> {
  /**
   * This holds the history.
   */
  private final Vector2[] histories;
  /**
   * An example of the 'zero' value of the type to be smoothed. This would be
   * something like Vector2D(0,0).
   */
  private int nextUpdateSlot;
  private float sumX;
  private float sumY;

  /**
   * To instantiate a Smoother, pass it the number of samples you want to use in the smoothing,
   * and an example of a 'zero' type.
   *
   * @param sampleSize the sample size
   * @param zeroValue  the zero value
   */
  public SmootherVector(int sampleSize, T zeroValue) {
    if (sampleSize <= 0) {
      throw new IllegalArgumentException("sampleSize must be positive");
    }
    Objects.requireNonNull(zeroValue, "zeroValue");
    histories = new Vector2[sampleSize];
    for (int i = 0; i < sampleSize; i++) {
      histories[i] = Vector2.newInstance();
    }
    nextUpdateSlot = 0;
  }

  /**
   * Each time you want to get a new average, feed it the most recent value and
   * this method will return an average over the last SampleSize updates.
   *
   * @param mostRecentValue the most recent value
   * @return an average over the last SampleSize updates
   */
  @SuppressWarnings("unchecked")
  public synchronized T update(T mostRecentValue) {
    Objects.requireNonNull(mostRecentValue, "mostRecentValue");
    var oldest = histories[nextUpdateSlot];
    sumX -= oldest.x;
    sumY -= oldest.y;
    oldest.set(mostRecentValue);
    sumX += oldest.x;
    sumY += oldest.y;
    nextUpdateSlot = (nextUpdateSlot + 1) % histories.length;
    return (T) Vector2.valueOf(sumX / histories.length, sumY / histories.length);
  }
}
