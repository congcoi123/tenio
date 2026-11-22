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
public class Smoother<T extends Number> {
  /**
   * This holds the history.
   */
  private final double[] histories;
  /**
   * an example of the 'zero' value of the type to be smoothed. This would be
   * something like Vector2D(0,0), see {@link Vector2}.
   */
  private final T zeroValue;
  private int nextUpdateSlot;
  private double sum;

  /**
   * To instantiate a Smoother, pass it the number of samples you want to use in the smoothing,
   * and an example of a 'zero' type.
   *
   * @param sampleSize the sample size
   * @param zeroValue  the zero value
   */
  public Smoother(int sampleSize, T zeroValue) {
    if (sampleSize <= 0) {
      throw new IllegalArgumentException("sampleSize must be positive");
    }
    this.zeroValue = Objects.requireNonNull(zeroValue, "zeroValue");
    histories = new double[sampleSize];
    nextUpdateSlot = 0;
  }

  /**
   * Each time you want to get a new average, feed it the most recent value and
   * this method will return an average over the last SampleSize updates.
   *
   * @param mostRecentValue the most recent value
   * @return an average over the last SampleSize updates
   */
  //
  public synchronized T update(T mostRecentValue) {
    Objects.requireNonNull(mostRecentValue, "mostRecentValue");
    sum -= histories[nextUpdateSlot];
    histories[nextUpdateSlot] = mostRecentValue.doubleValue();
    sum += histories[nextUpdateSlot];
    nextUpdateSlot = (nextUpdateSlot + 1) % histories.length;
    return convert(sum / histories.length, zeroValue);
  }

  @SuppressWarnings("unchecked")
  public <X extends Number> X add(X a, X b) {
    return (X) convert(a.doubleValue() + b.doubleValue(), a);
  }

  @SuppressWarnings("unchecked")
  public <X extends Number> X div(X a, float b) {
    if (b == 0) {
      throw new ArithmeticException("division by zero");
    }
    return (X) convert(a.doubleValue() / b, a);
  }

  @SuppressWarnings("unchecked")
  private static <X extends Number> X convert(double value, X template) {
    Number result;
    if (template instanceof Byte) {
      result = (byte) value;
    } else if (template instanceof Short) {
      result = (short) value;
    } else if (template instanceof Integer) {
      result = (int) value;
    } else if (template instanceof Long) {
      result = (long) value;
    } else if (template instanceof Float) {
      result = (float) value;
    } else if (template instanceof Double) {
      result = value;
    } else {
      throw new IllegalArgumentException("Unsupported number type: " + template.getClass());
    }
    return (X) result;
  }
}
