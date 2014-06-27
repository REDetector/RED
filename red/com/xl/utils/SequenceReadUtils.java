package com.xl.utils;

import com.xl.datatypes.sequence.Location;

/**
 * This is a collection of methods which allow you to use a primitive long as a
 * genome location (start,end and strand) without the considerable overhead of
 * having to create objects for each of the instances.
 * 
 * Make no mistake this is an UGLY HACK which we've only done reluctantly since
 * there seems to be no other way to reduce the overhead of creating hundreds of
 * millions of objects for sequence reads, which is what we'd otherwise be
 * required to do.
 * 
 */
public class SequenceReadUtils {

	/**
	 * Provides the mid-point of this position range.
	 * 
	 * @return
	 */
	public static int midPoint(int start, int end) {
		return (start + end) / 2;
	}

	/**
	 * Gets the total distance encompassed by two locations on the same
	 * chromosome.
	 */

	public static int fragmentLength(Location value1, Location value2) {
		return Math.max(value1.getEnd(), value2.getEnd())
				- Math.min(value1.getStart(), value2.getStart());
	}

	/**
	 * Says whether two reads which must be known to be on the same chromosome
	 * overlap with each other.
	 *
     * @param value1
     * @param value2
     * @return true if they overlap, otherwise false
	 */
	public static boolean overlaps(Location value1, Location value2) {
		return (value1.getStart() <= value2.getEnd() && value1.getEnd() >= value2
				.getStart());
	}
	
	public static boolean duplicate(Location value1, Location value2) {
		return value1.getStart() == value2.getStart()
				&& value1.getEnd() == value2.getEnd();
	}

	public static int compare(Location o1, Location o2) {
		if (o1.getStart() != o2.getStart())
			return (int) (o1.getStart() - o2.getStart());
		else if (o1.getEnd() != o2.getEnd())
			return (int) (o1.getEnd() - o2.getEnd());
		else
			return 0;
	}

	public static void sort(Location[] values) {

		if (values == null || values.length == 0)
			return;

		quicksort(values, 0, values.length - 1);
	}

	/*
	 * Quicksort implementation adapted from
	 * http://www.inf.fh-flensburg.de/lang/
	 * algorithmen/sortieren/quick/quicken.htm
	 */

	private static void quicksort(Location[] a, int low, int high) {
		// low is the lower index, high is the upper index
		// of the region of array a that is to be sorted
		int i = low, j = high;
		Location h;

		// comparison element x
		Location x = a[(low + high) / 2];

		// partition
		do {
			while (compare(a[i], x) < 0)
				i++;
			while (compare(a[j], x) > 0)
				j--;
			if (i <= j) {
				h = a[i];
				a[i] = a[j];
				a[j] = h;
				i++;
				j--;
			}
		} while (i <= j);

		// recursion
		if (low < j)
			quicksort(a, low, j);
		if (i < high)
			quicksort(a, i, high);
	}

}
