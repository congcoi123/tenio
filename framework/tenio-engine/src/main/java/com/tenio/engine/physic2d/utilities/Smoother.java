package com.tenio.engine.physic2d.utilities;

import java.util.ArrayList;
import java.util.List;

import com.tenio.engine.physic2d.math.Vector2;

/**
 * Template class to help calculate the average value of a history of values.
 * This can only be used with types that have a 'zero' value and that have the
 * += and / operators overloaded.
 *
 * Example: Used to smooth frame rate calculations.
 */
public class Smoother<T extends Number> {
	/**
	 * This holds the history
	 */
	private final List<T> __histories;
	private int __nextUpdateSlot;
	/**
	 * an example of the 'zero' value of the type to be smoothed. This would be
	 * something like Vector2D(0,0), see {@link Vector2}
	 */
	private T __zeroValue;

	// To instantiate a Smoother pass it the number of samples you want to use in
	// the smoothing, and an example of a 'zero' type
	public Smoother(int sampleSize, T zeroValue) {
		__histories = new ArrayList<T>(sampleSize);
		for (int i = 0; i < sampleSize; i++)
			__histories.add(zeroValue);
		__zeroValue = zeroValue;
		__nextUpdateSlot = 0;
	}

	/**
	 * Each time you want to get a new average, feed it the most recent value and
	 * this method will return an average over the last SampleSize updates
	 * 
	 * @param mostRecentValue the most recent value
	 * @return an average over the last SampleSize updates
	 */
	//
	@SuppressWarnings("unchecked")
	public T update(T mostRecentValue) {
		// overwrite the oldest value with the newest
		__histories.set(__nextUpdateSlot++, mostRecentValue);

		// make sure m_iNextUpdateSlot wraps around.
		if (__nextUpdateSlot == __histories.size()) {
			__nextUpdateSlot = 0;
		}

		// now to calculate the average of the history list
		T sum = null;
		try {
			sum = (T) __zeroValue.getClass().getConstructor(Float.TYPE).newInstance(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		var it = __histories.listIterator();

		while (it.hasNext()) {
			sum = add(sum, it.next());
		}

		sum = div(sum, (float) __histories.size());
		return sum;
	}

	@SuppressWarnings("unchecked")
	public <X extends Number> X add(X a, X b) {
		return (X) Float.valueOf(a.floatValue() + b.floatValue());
	}

	@SuppressWarnings("unchecked")
	public <X extends Number> X div(X a, float b) {
		return (X) Float.valueOf(a.floatValue() / b);
	}

}
