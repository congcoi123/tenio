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
public class SmootherVector<T extends Vector2> {
	/**
	 * This holds the history
	 */
	private List<T> __histories;
	private int __nextUpdateSlot;
	/**
	 * An example of the 'zero' value of the type to be smoothed. This would be
	 * something like Vector2D(0,0)
	 */
	private T __zeroValue;

	// To instantiate a Smoother pass it the number of samples you want to use in
	// the smoothing, and an example of a 'zero' type
	public SmootherVector(int SampleSize, T ZeroValue) {
		__histories = new ArrayList<T>(SampleSize);
		for (int i = 0; i < SampleSize; i++)
			__histories.add(ZeroValue);
		__zeroValue = ZeroValue;
		__nextUpdateSlot = 0;
	}

	/**
	 * Each time you want to get a new average, feed it the most recent value and
	 * this method will return an average over the last SampleSize updates
	 * 
	 * @param mostRecentValue the most recent value
	 * @return an average over the last SampleSize updates
	 */
	public T update(T mostRecentValue) {
		// overwrite the oldest value with the newest
		__histories.set(__nextUpdateSlot++, mostRecentValue);

		// make sure m_iNextUpdateSlot wraps around.
		if (__nextUpdateSlot == __histories.size())
			__nextUpdateSlot = 0;

		// now to calculate the average of the history list
		// c++ code make a copy here, I use Zero method instead.
		// Another approach could be creating public clone() method in Vector2D ...
		var sum = __zeroValue;
		sum.zero();

		var it = __histories.listIterator();

		while (it.hasNext()) {
			sum.add(it.next());
		}

		sum.div(__histories.size());
		return sum;
	}

}
