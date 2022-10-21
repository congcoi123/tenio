package com.tenio.engine.physic2d.utility;

import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;

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
  private final List<T> histories;
  /**
   * An example of the 'zero' value of the type to be smoothed. This would be
   * something like Vector2D(0,0).
   */
  private final T zeroValue;
  private int nextUpdateSlot;

  /**
   * To instantiate a Smoother, pass it the number of samples you want to use in the smoothing,
   * and an example of a 'zero' type.
   *
   * @param sampleSize the sample size
   * @param zeroValue  the zero value
   */
  public SmootherVector(int sampleSize, T zeroValue) {
    histories = new ArrayList<>(sampleSize);
    for (int i = 0; i < sampleSize; i++) {
      histories.add(zeroValue);
    }
    this.zeroValue = zeroValue;
    nextUpdateSlot = 0;
  }

  /**
   * Each time you want to get a new average, feed it the most recent value and
   * this method will return an average over the last SampleSize updates.
   *
   * @param mostRecentValue the most recent value
   * @return an average over the last SampleSize updates
   */
  public T update(T mostRecentValue) {
    // overwrite the oldest value with the newest
    histories.set(nextUpdateSlot++, mostRecentValue);

    // make sure m_iNextUpdateSlot wraps around.
    if (nextUpdateSlot == histories.size()) {
      nextUpdateSlot = 0;
    }

    // now to calculate the average of the history list
    // c++ code make a copy here, I use Zero method instead.
    // Another approach could be creating public clone() method in Vector2D ...
    var sum = zeroValue;
    sum.zero();

    var it = histories.listIterator();

    while (it.hasNext()) {
      sum.add(it.next());
    }

    sum.div(histories.size());
    return sum;
  }
}
